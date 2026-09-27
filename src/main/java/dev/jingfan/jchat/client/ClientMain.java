package dev.jingfan.jchat.client;
import dev.jingfan.jchat.client.ui.LoginFrame;

import javax.swing.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/** Connects to the chat server and starts the Swing login window. */
public final class ClientMain {

    private ClientMain() {
    }

    public static void main(String[] args) {
        connection();

        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        try {
            String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lookAndFeel);

        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(LoginFrame::new);
    }

    public static void connection() {
        String ip = DataBuffer.configProp.getProperty("ip");
        int port = Integer.parseInt(DataBuffer.configProp.getProperty("port"));
        try {
            DataBuffer.clientSocket = new Socket(ip, port);
            DataBuffer.oos = new ObjectOutputStream(DataBuffer.clientSocket.getOutputStream());
            DataBuffer.ois = new ObjectInputStream(DataBuffer.clientSocket.getInputStream());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(),
                    "Could not connect to the chat dev.jingfan.jchat.server.",
                    "Server unavailable", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
}
