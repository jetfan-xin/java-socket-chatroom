package dev.jingfan.jchat.common.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** Quietly closes socket resources during cleanup paths. */
public final class SocketUtil {
    private SocketUtil() {
    }
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                System.err.println("Could not close socket: " + e.getMessage());
            }
        }
    }

    public static void close(Socket socket) {
        close((Closeable) socket);
    }

    public static void close(ServerSocket serverSocket) {
        close((Closeable) serverSocket);
    }
}
