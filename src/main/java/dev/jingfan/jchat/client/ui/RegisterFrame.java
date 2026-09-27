package dev.jingfan.jchat.client.ui;
import dev.jingfan.jchat.client.DataBuffer;
import dev.jingfan.jchat.client.util.ClientUtil;
import dev.jingfan.jchat.common.model.entity.Request;
import dev.jingfan.jchat.common.model.entity.Response;
import dev.jingfan.jchat.common.model.entity.ResponseStatus;
import dev.jingfan.jchat.common.model.entity.User;
import dev.jingfan.jchat.common.protocol.ProtocolActions;
import dev.jingfan.jchat.common.ui.AvatarIcons;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/** Registration window for creating a local demo account. */
public final class RegisterFrame extends JFrame {
    private static final long serialVersionUID = -768631070458723803L;
    private JPasswordField pwdFld;
    private JPasswordField pwd2Fld;
    private JTextField nickname;
    private JComboBox<ImageIcon> head;
    private JRadioButton sex0;
    private JRadioButton sex1;
    private JButton ok;
    private JButton reset;
    private JButton cancel;

    public RegisterFrame(){
        this.init();
        setVisible(true);
    }

    private void init(){
        this.setTitle("JChat - Create account");
        setBounds((DataBuffer.screenSize.width - 387)/2,
                (DataBuffer.screenSize.height - 267)/2,
                387, 267);
        getContentPane().setLayout(null);
        setResizable(false);

        JLabel label = new JLabel("Name:");
        label.setBounds(24, 36, 59, 17);
        getContentPane().add(label);

        nickname = new JTextField();
        nickname.setBounds(90, 34, 110, 22);
        getContentPane().add(nickname);

        JLabel label5 = new JLabel("Password:*");
        label5.setBounds(24, 72, 50, 17);
        getContentPane().add(label5);

        JLabel label3 = new JLabel("Confirm:*");
        label3.setBounds(24, 107, 65, 17);
        getContentPane().add(label3);

        pwdFld = new JPasswordField();
        pwdFld.setBounds(90, 70, 110, 22);
        getContentPane().add(pwdFld);

        pwd2Fld = new JPasswordField();
        pwd2Fld.setBounds(90, 105, 110, 22);
        getContentPane().add(pwd2Fld);

        JLabel label4 = new JLabel("Gender:");
        label4.setBounds(230, 36, 31, 17);
        getContentPane().add(label4);

        sex1 = new JRadioButton("M",true);
        sex1.setBounds (268, 31,44, 25);
        getContentPane().add(sex1);
        sex0 = new JRadioButton("F");
        sex0.setBounds(310, 31, 44, 25);
        getContentPane().add(sex0);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(sex0);
        buttonGroup.add(sex1);

        JLabel label6 = new JLabel("Avatar:");
        label6.setBounds(230, 72, 31, 17);
        getContentPane().add(label6);

        head = new JComboBox<>();
        head.setBounds(278, 70, 65, 45);
        head.setMaximumRowCount(5);
        for (int i = 0; i < 12; i++) {
            head.addItem(AvatarIcons.create(i));

        }
        head.setSelectedIndex(0);
        getContentPane().add(head);

        ok = new JButton("Create");
        ok.setBounds(27, 176, 60, 28);
        getContentPane().add(ok);

        reset = new JButton("Reset");
        reset.setBounds(123, 176, 60, 28);
        getContentPane().add(reset);

        cancel = new JButton("Cancel");
        cancel.setBounds(268, 176, 60, 28);
        getContentPane().add(cancel);

        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                RegisterFrame.this.dispose();
            }
        });

        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                RegisterFrame.this.dispose();
            }
        });

        reset.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                nickname.setText("");
                pwdFld.setText("");
                pwd2Fld.setText("");
                nickname.requestFocusInWindow();
            }
        });

        ok.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if (pwdFld.getPassword().length==0 || pwd2Fld.getPassword().length==0) {
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                            "Fields marked with * are required.");

                } else if (!new String(pwdFld.getPassword()).equals(new String(pwd2Fld.getPassword()))) {
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                            "The passwords do not match.");
                    pwdFld.setText("");
                    pwd2Fld.setText("");
                    pwdFld.requestFocusInWindow();

                } else {
                    User user = new User(new String(pwdFld.getPassword()),
                            nickname.getText(),
                            sex0.isSelected() ? 'f' : 'm',
                            head.getSelectedIndex());
                    try {
                        RegisterFrame.this.register(user);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void register(User user) throws IOException, ClassNotFoundException{
        Request request = new Request();
        request.setAction(ProtocolActions.REGISTER);
        request.setAttribute("user", user);

        Response response = ClientUtil.sendTextRequest(request);

        ResponseStatus status = response.getStatus();
        switch(status){
            case OK:
                User user2 = (User)response.getData("user");
                JOptionPane.showMessageDialog(RegisterFrame.this,
                        "Account created. Your account ID is " + user2.getId() + ".",
                        "Registration complete",JOptionPane.INFORMATION_MESSAGE);
                this.setVisible(false);
                break;
            default:
                JOptionPane.showMessageDialog(RegisterFrame.this,
                        "Registration failed. Please try again.",
                        "Server error",JOptionPane.ERROR_MESSAGE);
        }
    }
}
