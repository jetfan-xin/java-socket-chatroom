package dev.jingfan.jchat.server.ui;
import dev.jingfan.jchat.common.model.entity.User;
import dev.jingfan.jchat.server.DataBuffer;
import dev.jingfan.jchat.server.controller.RequestProcessor;
import dev.jingfan.jchat.server.model.service.UserService;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

/** Swing console for server status, announcements, private notices, and sign-outs. */
public final class ServerInfoFrame extends JFrame {
    private static final long serialVersionUID = 6274443611957724780L;
    private JTextField jta_msg;
    private JTable onlineUserTable ;
    private JTable registeredUserTable ;

    public ServerInfoFrame() {
        init();
        loadData();
        setVisible(true);
    }

    private void init() {
        this.setTitle("JChat Server");
        this.setBounds((DataBuffer.screenSize.width - 700)/2,
                (DataBuffer.screenSize.height - 475)/2, 700, 475);
        this.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        panel.setBorder(BorderFactory.createTitledBorder(border,
                "Server administration", TitledBorder.LEFT,TitledBorder.TOP));
        this.add(panel, BorderLayout.NORTH);

        JLabel label = new JLabel("Port: "
                + DataBuffer.configProp.getProperty("port"));
        panel.add(label);
        JButton exitBtn = new JButton("Stop server");
        panel.add(exitBtn);

        JLabel la_msg = new JLabel("Announcement:");
        panel.add(la_msg);

        jta_msg = new JTextField(30);

        ActionListener sendCaseMsgAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sendAllMsg();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        };

        jta_msg.addActionListener(sendCaseMsgAction);
        JButton bu_send = new JButton("Send");

        bu_send.addActionListener(sendCaseMsgAction);
        panel.add(jta_msg);
        panel.add(bu_send);

        onlineUserTable = new JTable(DataBuffer.onlineUserTableModel);
        registeredUserTable = new JTable(DataBuffer.registeredUserTableModel);

        JPopupMenu pop = getTablePop();
        onlineUserTable.setComponentPopupMenu(pop);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Online users", new JScrollPane(onlineUserTable));
        tabbedPane.addTab("Registered users", new JScrollPane(registeredUserTable));
        this.add(tabbedPane, BorderLayout.CENTER);

        final JLabel stateBar = new JLabel("", SwingConstants.RIGHT);
        stateBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        new java.util.Timer(true).scheduleAtFixedRate(
                new TimerTask(){
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    public void run() {
                        SwingUtilities.invokeLater(() -> stateBar.setText(
                                "Current time: " + df.format(new Date()) + "  "));
                    }
                }, 0, 1000);
        this.add(stateBar, BorderLayout.SOUTH);

        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        exitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                logout();
            }
        });
    }

    private JPopupMenu getTablePop() {
        JPopupMenu pop = new JPopupMenu();
        JMenuItem mi_send = new JMenuItem("Send private notice");

        mi_send.setActionCommand("send");
        JMenuItem mi_del = new JMenuItem("Sign out user");
        mi_del.setActionCommand("del");

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String s = e.getActionCommand();

                popMenuAction(s);
            }
        };
        mi_send.addActionListener(al);
        mi_del.addActionListener(al);
        pop.add(mi_send);
        pop.add(mi_del);
        return pop;
    }

    private void popMenuAction(String command) {

        final int selectIndex = onlineUserTable.getSelectedRow();
        if (selectIndex == -1) {
            JOptionPane.showMessageDialog(this, "Select an online user first.");
            return;
        }
        String userId = onlineUserTable.getValueAt(selectIndex, 0).toString();

        if (command.equals("del")) {

            try {
                RequestProcessor.remove(DataBuffer.onlineUsersMap.get(Long.valueOf(userId)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (command.equals("send")) {
            final JDialog jd = new JDialog(this, true);
            jd.setLayout(new FlowLayout());
            jd.setSize(200, 100);
            final JTextField jtd_m = new JTextField(20);
            JButton jb = new JButton("Send");
            jd.add(jtd_m);
            jd.add(jb);

            jb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Server sent a private notice.");
                    String msg = jtd_m.getText();
                    try {
                        RequestProcessor.chat_sys(
                                msg, DataBuffer.onlineUsersMap.get(Long.valueOf(userId)));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    jtd_m.setText("");
                    jd.dispose();
                }
            });
            jd.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Unknown command: " + command);
        }

        SwingUtilities.updateComponentTreeUI(onlineUserTable);
    }

    private void sendAllMsg() throws IOException {
        RequestProcessor.board(jta_msg.getText());
        jta_msg.setText("");
    }

    private void loadData(){
        List<User> users = new UserService().loadAllUser();

        int count = 0;
        for (User user : users) {
            count+=1;
            DataBuffer.registeredUserTableModel.add(new String[]{
                    String.valueOf(user.getId()),
                    user.getNickname(),
                    String.valueOf(user.getSex())
            });
        }
        UserService.setIdCount(count);
    }

    private void logout() {
        int select = JOptionPane.showConfirmDialog(ServerInfoFrame.this,
                "Stop the server and disconnect every client?",
                "Stop server",
                JOptionPane.YES_NO_OPTION);

        if (select == JOptionPane.YES_OPTION) {
            System.exit(0);
        }else{

            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
    }
}
