package dev.jingfan.jchat.server;

import dev.jingfan.jchat.server.controller.RequestProcessor;
import dev.jingfan.jchat.server.ui.ServerInfoFrame;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Starts the chat server and, when available, its Swing administration console. */
public final class MainServer {
    private MainServer() {
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(DataBuffer.configProp.getProperty("port"));
        try {
            DataBuffer.serverSocket = new ServerSocket(port);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not bind chat server to port " + port, exception);
        }

        ExecutorService clients = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "chat-client-handler");
            thread.setDaemon(true);
            return thread;
        });

        Thread acceptor = new Thread(() -> acceptConnections(clients),
                "chat-connection-acceptor");
        acceptor.setDaemon(!GraphicsEnvironment.isHeadless());
        acceptor.start();
        System.out.println("Chat server listening on port " + port);

        if (!GraphicsEnvironment.isHeadless()) {
            configureLookAndFeel();
            SwingUtilities.invokeLater(ServerInfoFrame::new);
        }
    }

    private static void acceptConnections(ExecutorService clients) {
        while (!DataBuffer.serverSocket.isClosed()) {
            try {
                Socket socket = DataBuffer.serverSocket.accept();
                System.out.println("Client connected: "
                        + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                clients.submit(new RequestProcessor(socket));
            } catch (IOException exception) {
                if (!DataBuffer.serverSocket.isClosed()) {
                    System.err.println("Could not accept client: " + exception.getMessage());
                }
            }
        }
    }

    private static void configureLookAndFeel() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception exception) {
            System.err.println("Could not set look and feel: " + exception.getMessage());
        }
    }
}
