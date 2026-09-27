package dev.jingfan.jchat.client.util;

import dev.jingfan.jchat.client.DataBuffer;
import dev.jingfan.jchat.client.ui.ChatFrame;
import dev.jingfan.jchat.common.model.entity.Request;
import dev.jingfan.jchat.common.model.entity.Response;
import dev.jingfan.jchat.common.protocol.ProtocolActions;

import java.io.IOException;

/** Sends protocol requests and appends messages to the client UI. */
public final class ClientUtil {

    private ClientUtil() {
    }

    public static Response sendTextRequest(Request request) throws IOException {
        Response response = null;
        try {

            synchronized (DataBuffer.oos) {
                DataBuffer.oos.writeObject(request);
                DataBuffer.oos.flush();
            }
            System.out.println("Client sent request: " + request.getAction());

            if(!ProtocolActions.EXIT.equals(request.getAction())){

                response = (Response) DataBuffer.ois.readObject();
                System.out.println("Client received response: " + response.getStatus());
            }else{
                System.out.println("Client disconnected.");
            }
        } catch (IOException e) {
            throw e;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static void sendTextRequest2(Request request) throws IOException {
        try {
            synchronized (DataBuffer.oos) {
                DataBuffer.oos.writeObject(request);
                DataBuffer.oos.flush();
            }
            System.out.println("Client sent request: " + request.getAction());
        } catch (IOException e) {
            throw e;
        }
    }

    public static void appendTxt2MsgListArea(String txt) {
        Runnable append = () -> {
            ChatFrame.msgListArea.append(txt);
            ChatFrame.msgListArea.setCaretPosition(
                    ChatFrame.msgListArea.getDocument().getLength());
        };
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            append.run();
        } else {
            javax.swing.SwingUtilities.invokeLater(append);
        }
    }
}
