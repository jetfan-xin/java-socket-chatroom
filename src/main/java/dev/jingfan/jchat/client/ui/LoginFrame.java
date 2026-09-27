package dev.jingfan.jchat.client.ui;
import dev.jingfan.jchat.client.DataBuffer;
import dev.jingfan.jchat.client.util.ClientUtil;
import dev.jingfan.jchat.common.model.entity.Request;
import dev.jingfan.jchat.common.model.entity.Response;
import dev.jingfan.jchat.common.model.entity.ResponseStatus;
import dev.jingfan.jchat.common.model.entity.User;
import dev.jingfan.jchat.common.protocol.ProtocolActions;
import dev.jingfan.jchat.common.util.SocketUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

/** Login window for an already connected chat dev.jingfan.jchat.client. */
public final class LoginFrame extends JFrame {
    private static final long serialVersionUID = -3426717670093483287L;

    private JTextField idTxt;
    private JPasswordField pwdFld;

    public LoginFrame(){
        this.init();
        setVisible(true);
    }

    private void init(){
        this.setTitle("JChat - Sign in");
        this.setSize(330, 230);

        int x = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int y = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        this.setLocation((x - this.getWidth()) / 2, (y-this.getHeight())/ 2);
        this.setResizable(false);

        JLabel label = new JLabel("JCHAT", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(30, 64, 175));
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
        label.setPreferredSize(new Dimension(324,47));
        this.add(label, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel();
        Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        mainPanel.setBorder(BorderFactory.createTitledBorder(
                border, "Account details", TitledBorder.CENTER, TitledBorder.TOP));
        this.add(mainPanel, BorderLayout.CENTER);
        mainPanel.setLayout(null);

        JLabel nameLbl = new JLabel("Account:");
        nameLbl.setBounds(50, 30, 40, 22);
        mainPanel.add(nameLbl);
        idTxt = new JTextField();
        idTxt.setBounds(95, 30, 150, 22);
        idTxt.requestFocusInWindow();
        mainPanel.add(idTxt);

        JLabel pwdLbl = new JLabel("Password:");
        pwdLbl.setBounds(50, 60, 40, 22);
        mainPanel.add(pwdLbl);
        pwdFld = new JPasswordField();
        pwdFld.setBounds(95, 60, 150, 22);
        mainPanel.add(pwdFld);

        JPanel btnPanel = new JPanel();
        this.add(btnPanel, BorderLayout.SOUTH);
        btnPanel.setLayout(new BorderLayout());
        btnPanel.setBorder(new EmptyBorder(2, 8, 4, 8));

        JButton registerBtn = new JButton("Register");
        btnPanel.add(registerBtn, BorderLayout.WEST);
        JButton submitBtn = new JButton("Sign in");
        btnPanel.add(submitBtn, BorderLayout.EAST);

        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                SocketUtil.close(DataBuffer.clientSocket);
                System.exit(0);
            }
        });

        registerBtn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                new RegisterFrame();
            }
        });

        submitBtn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void login() {
        if (idTxt.getText().length() == 0
                || pwdFld.getPassword().length == 0) {
            JOptionPane.showMessageDialog(LoginFrame.this,
                    "Account and password are required.",
                    "Invalid input",JOptionPane.ERROR_MESSAGE);
            idTxt.requestFocusInWindow();
            return;
        }

        if(!idTxt.getText().matches("^\\d*$")){
            JOptionPane.showMessageDialog(LoginFrame.this,
                    "The account must contain digits only.",
                    "Invalid input",JOptionPane.ERROR_MESSAGE);
            idTxt.requestFocusInWindow();
            return;
        }

        Request req = new Request();
        req.setAction(ProtocolActions.LOGIN);
        req.setAttribute("id", idTxt.getText());
        req.setAttribute("password", new String(pwdFld.getPassword()));

        Response response = null;
        try {
            response = ClientUtil.sendTextRequest(req);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        if (response != null && response.getStatus() == ResponseStatus.OK) {

            User user2 = (User)response.getData("user");
            if(user2!= null){
                DataBuffer.currentUser = user2;

                DataBuffer.onlineUsers = (List<User>)response.getData("onlineUsers");

                LoginFrame.this.dispose();
                new ChatFrame();
            }else{
                String str = (String)response.getData("msg");
                JOptionPane.showMessageDialog(LoginFrame.this,
                        str,
                        "Sign-in failed",JOptionPane.ERROR_MESSAGE);
            }
        }else{
            JOptionPane.showMessageDialog(LoginFrame.this,
                    "The server could not complete the request.",
                    "Sign-in failed",JOptionPane.ERROR_MESSAGE);
        }
    }
}
