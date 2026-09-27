package dev.jingfan.jchat.client.ui;
import dev.jingfan.jchat.client.ClientThread;
import dev.jingfan.jchat.client.DataBuffer;
import dev.jingfan.jchat.client.model.entity.MyCellRenderer;
import dev.jingfan.jchat.client.model.entity.OnlineUserListModel;
import dev.jingfan.jchat.client.util.ClientUtil;
import dev.jingfan.jchat.client.util.JFrameShaker;
import dev.jingfan.jchat.common.model.entity.FileInfo;
import dev.jingfan.jchat.common.model.entity.Message;
import dev.jingfan.jchat.common.model.entity.Request;
import dev.jingfan.jchat.common.model.entity.User;
import dev.jingfan.jchat.common.protocol.ProtocolActions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Main chat window for public chat, private chat, nudges, and file offers. */
public final class ChatFrame extends JFrame {
    private static final long serialVersionUID = -2310785591507878535L;

    private JLabel otherInfoLbl;

    private JLabel currentUserLbl;

    public static JTextArea msgListArea;

    public static JTextArea sendArea;

    public static JList<User> onlineList;

    public static JLabel onlineCountLbl;

    public static FileInfo sendFile;

    public JCheckBox privateChatCheckBox;

    public ChatFrame(){
        this.init();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    private void init(){
        this.setTitle("JChat");
        this.setSize(550, 500);
        this.setResizable(false);

        int x = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int y = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        this.setLocation((x - this.getWidth()) / 2, (y-this.getHeight())/ 2);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                mainPanel, userPanel);
        splitPane.setDividerLocation(380);
        splitPane.setDividerSize(10);
        splitPane.setOneTouchExpandable(true);
        this.add(splitPane, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());

        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BorderLayout());

        JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                infoPanel, sendPanel);
        splitPane2.setDividerLocation(300);
        splitPane2.setDividerSize(1);
        mainPanel.add(splitPane2, BorderLayout.CENTER);

        otherInfoLbl = new JLabel("Status: public chat");
        infoPanel.add(otherInfoLbl, BorderLayout.NORTH);

        msgListArea = new JTextArea();
        msgListArea.setLineWrap(true);
        infoPanel.add(new JScrollPane(msgListArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new BorderLayout());
        sendPanel.add(tempPanel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        tempPanel.add(btnPanel, BorderLayout.CENTER);

        JButton shakeBtn = new JButton("Nudge");
        shakeBtn.setToolTipText("Send a window nudge to the selected user");
        btnPanel.add(shakeBtn);

        JButton sendFileBtn = new JButton("Send file");
        sendFileBtn.setToolTipText("Offer a file to the selected user");
        btnPanel.add(sendFileBtn);

        privateChatCheckBox = new JCheckBox("Private chat");
        tempPanel.add(privateChatCheckBox, BorderLayout.EAST);

        sendArea = new JTextArea();
        sendArea.setLineWrap(true);
        sendPanel.add(new JScrollPane(sendArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        JPanel btn2Panel = new JPanel();
        btn2Panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        this.add(btn2Panel, BorderLayout.SOUTH);
        JButton closeBtn = new JButton("Close");
        closeBtn.setToolTipText("Disconnect and exit");
        btn2Panel.add(closeBtn);
        JButton submitBtn = new JButton("Send");
        submitBtn.setToolTipText("Press Enter to send");
        btn2Panel.add(submitBtn);
        sendPanel.add(btn2Panel, BorderLayout.SOUTH);

        JPanel onlineListPane = new JPanel();
        onlineListPane.setLayout(new BorderLayout());
        onlineCountLbl = new JLabel("Online users (1)");
        onlineListPane.add(onlineCountLbl, BorderLayout.NORTH);

        JPanel currentUserPane = new JPanel();
        currentUserPane.setLayout(new BorderLayout());
        currentUserPane.add(new JLabel("Current user"), BorderLayout.NORTH);

        JSplitPane splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                onlineListPane, currentUserPane);
        splitPane3.setDividerLocation(340);
        splitPane3.setDividerSize(1);
        userPanel.add(splitPane3, BorderLayout.CENTER);

        DataBuffer.onlineUserListModel = new OnlineUserListModel(DataBuffer.onlineUsers);

        onlineList = new JList<>(DataBuffer.onlineUserListModel);
        onlineList.setCellRenderer(new MyCellRenderer());

        onlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        onlineListPane.add(new JScrollPane(onlineList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        currentUserLbl = new JLabel();
        currentUserPane.add(currentUserLbl);

        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                logout();
            }
        });

        privateChatCheckBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(privateChatCheckBox.isSelected()){
                    User selectedUser = onlineList.getSelectedValue();
                    if(null == selectedUser){
                        otherInfoLbl.setText("Status: select a user for private chat");
                    }else if(DataBuffer.currentUser.getId() == selectedUser.getId()){
                        otherInfoLbl.setText("Status: you cannot message yourself");
                    }else{
                        otherInfoLbl.setText("Status: private chat with " + selectedUser.getNickname()
                                + " (" + selectedUser.getId() + ")");
                    }
                }else{
                    otherInfoLbl.setText("Status: public chat");
                }
            }
        });

        onlineList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                User selectedUser = onlineList.getSelectedValue();
                if (privateChatCheckBox.isSelected() && selectedUser != null) {
                    if(DataBuffer.currentUser.getId() == selectedUser.getId()){
                        otherInfoLbl.setText("Status: you cannot message yourself");
                    }else{
                        otherInfoLbl.setText("Status: private chat with " + selectedUser.getNickname()
                                + " (" + selectedUser.getId() + ")");
                    }
                }
            }
        });

        sendArea.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send-message");
        sendArea.getActionMap().put("send-message", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                sendTxtMsg();
            }
        });
        submitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                sendTxtMsg();
            }
        });

        shakeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                sendShakeMsg();
            }
        });

        sendFileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                sendFile();
            }
        });

        this.loadData();
    }

    public void loadData(){

        if(null != DataBuffer.currentUser){
            currentUserLbl.setIcon(DataBuffer.currentUser.getHeadIcon());
            currentUserLbl.setText(DataBuffer.currentUser.getNickname()
                    + "(" + DataBuffer.currentUser.getId() + ")");
        }

        onlineCountLbl.setText("Online users (" + DataBuffer.onlineUserListModel.getSize() + ")");

        new ClientThread(this).start();
    }

    public void sendShakeMsg(){
        User selectedUser = onlineList.getSelectedValue();
        if(null != selectedUser){
            if(DataBuffer.currentUser.getId() == selectedUser.getId()){
                JOptionPane.showMessageDialog(ChatFrame.this, "You cannot nudge yourself.",
                        "Cannot send", JOptionPane.ERROR_MESSAGE);
            }else{
                Message msg = new Message();
                msg.setFromUser(DataBuffer.currentUser);
                msg.setToUser(selectedUser);
                msg.setSendTime(new Date());

                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                StringBuffer sb = new StringBuffer();
                sb.append(" ").append(msg.getFromUser().getNickname())
                        .append("(").append(msg.getFromUser().getId()).append(") ")
                        .append(df.format(msg.getSendTime()))
                        .append("\n  nudged ").append(msg.getToUser().getNickname())
                        .append("(").append(msg.getToUser().getId()).append(") ")
                        .append("\n");
                msg.setMessage(sb.toString());

                Request request = new Request();
                request.setAction(ProtocolActions.SHAKE);
                request.setAttribute("msg", msg);
                try {
                    ClientUtil.sendTextRequest2(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ClientUtil.appendTxt2MsgListArea(msg.getMessage());
                new JFrameShaker(ChatFrame.this).startShake();
            }
        }else{
            JOptionPane.showMessageDialog(ChatFrame.this,
                    "Select one user before sending a nudge.",
                    "Cannot send", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void sendTxtMsg(){
        String content = sendArea.getText();
        if (content.isBlank()) {
            JOptionPane.showMessageDialog(ChatFrame.this, "A message cannot be empty.",
                    "Cannot send", JOptionPane.ERROR_MESSAGE);
        } else {
            User selectedUser = onlineList.getSelectedValue();

            Message msg = new Message();
            if(privateChatCheckBox.isSelected()){
                if(null == selectedUser){
                    JOptionPane.showMessageDialog(ChatFrame.this,
                            "Select a recipient for private chat.",
                            "Cannot send", JOptionPane.ERROR_MESSAGE);
                    return;
                }else if (DataBuffer.currentUser.getId() == selectedUser.getId()){
                    JOptionPane.showMessageDialog(ChatFrame.this,
                            "You cannot send a private message to yourself.",
                            "Cannot send", JOptionPane.ERROR_MESSAGE);
                    return;
                }else{
                    msg.setToUser(selectedUser);
                }
            }
            msg.setFromUser(DataBuffer.currentUser);
            msg.setSendTime(new Date());

            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            StringBuffer sb = new StringBuffer();
            sb.append(" ").append(df.format(msg.getSendTime())).append(" ")
                    .append(msg.getFromUser().getNickname())
                    .append("(").append(msg.getFromUser().getId()).append(") ");
            if(!this.privateChatCheckBox.isSelected()){
                sb.append("to everyone");
            }
            sb.append("\n  ").append(content).append("\n");
            msg.setMessage(sb.toString());

            Request request = new Request();
            request.setAction(ProtocolActions.CHAT);
            request.setAttribute("msg", msg);
            try {
                ClientUtil.sendTextRequest2(request);
            } catch (IOException e) {
                e.printStackTrace();
            }

            sendArea.setText("");
            sendArea.requestFocusInWindow();
            ClientUtil.appendTxt2MsgListArea(msg.getMessage());
        }
    }

    private void sendFile() {
        User selectedUser = onlineList.getSelectedValue();
        if(null != selectedUser){
            if(DataBuffer.currentUser.getId() == selectedUser.getId()){
                JOptionPane.showMessageDialog(ChatFrame.this,
                        "You cannot send a file to yourself.",
                        "Cannot send", JOptionPane.ERROR_MESSAGE);
            }else{
                JFileChooser jfc = new JFileChooser();
                if (jfc.showOpenDialog(ChatFrame.this) == JFileChooser.APPROVE_OPTION) {
                    File file = jfc.getSelectedFile();
                    sendFile = new FileInfo();
                    sendFile.setFromUser(DataBuffer.currentUser);
                    sendFile.setToUser(selectedUser);
                    try {
                        sendFile.setSrcName(file.getCanonicalPath());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    sendFile.setSendTime(new Date());

                    Request request = new Request();
                    request.setAction(ProtocolActions.FILE_OFFER);
                    request.setAttribute("file", sendFile);
                    try {
                        ClientUtil.sendTextRequest2(request);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ClientUtil.appendTxt2MsgListArea("[File] Offered "
                            + file.getName() + " to "
                            + selectedUser.getNickname() + "("
                            + selectedUser.getId() + "); waiting for a response.\n");
                }
            }
        }else{
            JOptionPane.showMessageDialog(ChatFrame.this,
                    "Select one user before offering a file.",
                    "Cannot send", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        int select = JOptionPane.showConfirmDialog(ChatFrame.this,
                "Disconnect from the server and exit?", "Exit JChat",
                JOptionPane.YES_NO_OPTION);
        if (select == JOptionPane.YES_OPTION) {
            Request req = new Request();
            req.setAction(ProtocolActions.EXIT);
            req.setAttribute("user", DataBuffer.currentUser);
            try {
                ClientUtil.sendTextRequest2(req);
            } catch (IOException ex) {
                ex.printStackTrace();
            }finally{
                System.exit(0);
            }
        }else{
            this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        }
    }

    public static void remove() {
        JOptionPane.showMessageDialog(sendArea,
                "The administrator signed you out.", "System notice",
                JOptionPane.INFORMATION_MESSAGE);

        Request req = new Request();
        req.setAction(ProtocolActions.EXIT);
        req.setAttribute("user", DataBuffer.currentUser);
        try {
            ClientUtil.sendTextRequest2(req);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }

    }
}
