package dev.jingfan.jchat.common.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Quietly closes stream resources during cleanup paths. */
public final class IOUtil {
    private IOUtil() {
    }
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                System.err.println("Could not close stream: " + e.getMessage());
            }
        }
    }

    public static void close(InputStream is, OutputStream os) {
        close(is);
        close(os);
    }
}
