package dev.jingfan.jchat.client;
import dev.jingfan.jchat.client.ui.ChatFrame;
import dev.jingfan.jchat.client.util.ClientUtil;
import dev.jingfan.jchat.client.util.JFrameShaker;
import dev.jingfan.jchat.common.model.entity.*;
import dev.jingfan.jchat.common.protocol.ProtocolActions;
import dev.jingfan.jchat.common.util.IOUtil;
import dev.jingfan.jchat.common.util.SocketUtil;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/** Listens for asynchronous server events after the user signs in. */
public class ClientThread extends Thread {
    private final JFrame currentFrame;

    public ClientThread(JFrame frame){
        currentFrame = frame;
    }

    public void run() {
        try {
            while (DataBuffer.clientSocket.isConnected()) {
                Response response = (Response) DataBuffer.ois.readObject();
                ResponseType type = response.getType();

                System.out.println("Client received event: " + type);
                if (type == ResponseType.LOGIN) {
                    User newUser = (User)response.getData("loginUser");
                    SwingUtilities.invokeLater(() -> {
                        DataBuffer.onlineUserListModel.addElement(newUser);
                        ChatFrame.onlineCountLbl.setText(
                                "Online users (" + DataBuffer.onlineUserListModel.getSize() + ")");
                    });
                    ClientUtil.appendTxt2MsgListArea(
                            "[System] " + newUser.getNickname() + " signed in.\n");
                }else if(type == ResponseType.LOGOUT){
                    User newUser = (User)response.getData("logoutUser");
                    SwingUtilities.invokeLater(() -> {
                        DataBuffer.onlineUserListModel.removeElement(newUser);
                        ChatFrame.onlineCountLbl.setText(
                                "Online users (" + DataBuffer.onlineUserListModel.getSize() + ")");
                    });
                    ClientUtil.appendTxt2MsgListArea(
                            "[System] " + newUser.getNickname() + " signed out.\n");

                }else if(type == ResponseType.CHAT){
                    Message msg = (Message)response.getData("txtMsg");
                    ClientUtil.appendTxt2MsgListArea(msg.getMessage());
                }else if(type == ResponseType.SHAKE){
                    Message msg = (Message)response.getData("ShakeMsg");
                    ClientUtil.appendTxt2MsgListArea(msg.getMessage());
                    new JFrameShaker(this.currentFrame).startShake();
                }else if(type == ResponseType.TOSENDFILE){
                    SwingUtilities.invokeLater(() -> toSendFile(response));
                }else if(type == ResponseType.AGREERECEIVEFILE){
                    new Thread(() -> sendFile(response), "file-sender").start();
                }else if(type == ResponseType.REFUSERECEIVEFILE){
                    ClientUtil.appendTxt2MsgListArea(
                            "[File] The recipient declined the transfer.\n");
                }else if(type == ResponseType.RECEIVEFILE){
                    new Thread(() -> receiveFile(response), "file-receiver").start();
                }else if(type == ResponseType.BOARD){
                    Message msg = (Message)response.getData("txtMsg");
                    ClientUtil.appendTxt2MsgListArea(msg.getMessage());
                }else if(type == ResponseType.REMOVE){
                    SwingUtilities.invokeLater(ChatFrame::remove);
                }
            }
        } catch (IOException e) {
            System.out.println("Server connection closed: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendFile(Response response) {
        final FileInfo sendFile = (FileInfo)response.getData("sendFile");

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        Socket socket = null;
        try {
            socket = new Socket(sendFile.getDestIp(),sendFile.getDestPort());
            bis = new BufferedInputStream(new FileInputStream(sendFile.getSrcName()));
            bos = new BufferedOutputStream(socket.getOutputStream());

            byte[] buffer = new byte[1024];
            int n = -1;
            while ((n = bis.read(buffer)) != -1){
                bos.write(buffer, 0, n);
            }
            bos.flush();
            synchronized (this) {
                ClientUtil.appendTxt2MsgListArea("[File] Transfer complete.\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            IOUtil.close(bis,bos);
            SocketUtil.close(socket);
        }
    }

    private void receiveFile(Response response) {
        final FileInfo sendFile = (FileInfo)response.getData("sendFile");

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(sendFile.getDestPort());
            socket = serverSocket.accept();
            bis = new BufferedInputStream(socket.getInputStream());
            bos = new BufferedOutputStream(new FileOutputStream(sendFile.getDestName()));

            byte[] buffer = new byte[1024];
            int n = -1;
            while ((n = bis.read(buffer)) != -1){
                bos.write(buffer, 0, n);
            }
            bos.flush();
            synchronized (this) {
                ClientUtil.appendTxt2MsgListArea(
                        "[File] Download complete: " + sendFile.getDestName() + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            IOUtil.close(bis,bos);
            SocketUtil.close(socket);
            SocketUtil.close(serverSocket);
        }
    }

    private void toSendFile(Response response) {
        FileInfo sendFile = (FileInfo)response.getData("sendFile");

        String fromName = sendFile.getFromUser().getNickname()
                + "(" + sendFile.getFromUser().getId() + ")";
        String fileName = sendFile.getSrcName()
                .substring(sendFile.getSrcName().lastIndexOf(File.separator)+1);

        int select = JOptionPane.showConfirmDialog(this.currentFrame,
                fromName + " wants to send [" + fileName + "].\nAccept the file?",
                "Incoming file", JOptionPane.YES_NO_OPTION);
        try {
            Request request = new Request();
            request.setAttribute("sendFile", sendFile);

            if (select == JOptionPane.YES_OPTION) {
                JFileChooser jfc = new JFileChooser();
                jfc.setSelectedFile(new File(fileName));
                int result = jfc.showSaveDialog(this.currentFrame);

                if (result == JFileChooser.APPROVE_OPTION){

                    sendFile.setDestName(jfc.getSelectedFile().getCanonicalPath());

                    sendFile.setDestIp(DataBuffer.ip);
                    sendFile.setDestPort(DataBuffer.RECEIVE_FILE_PORT);

                    request.setAction(ProtocolActions.FILE_ACCEPT);
                    ClientUtil.appendTxt2MsgListArea(
                            "[File] Accepted transfer from " + fromName + ".\n");
                } else {
                    request.setAction(ProtocolActions.FILE_REFUSE);
                    ClientUtil.appendTxt2MsgListArea(
                            "[File] Declined transfer from " + fromName + ".\n");
                }
            } else {
                request.setAction(ProtocolActions.FILE_REFUSE);
                ClientUtil.appendTxt2MsgListArea(
                        "[File] Declined transfer from " + fromName + ".\n");
            }

            ClientUtil.sendTextRequest2(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
