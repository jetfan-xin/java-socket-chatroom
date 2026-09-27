package dev.jingfan.jchat.client;
import dev.jingfan.jchat.client.model.entity.OnlineUserListModel;
import dev.jingfan.jchat.common.config.AppConfig;
import dev.jingfan.jchat.common.model.entity.User;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Properties;
/** Shared state for one desktop client process. */
public class DataBuffer {

    public static User currentUser;

    public static List<User> onlineUsers;

    public static Socket clientSocket;

    public static ObjectOutputStream oos;

    public static ObjectInputStream ois;

    public static Properties configProp;

    public static Dimension screenSize;

    public static String ip ;

    public static final int RECEIVE_FILE_PORT = 6667;

    public static OnlineUserListModel onlineUserListModel;

    static{
        screenSize = GraphicsEnvironment.isHeadless()
                ? new Dimension(1280, 800)
                : Toolkit.getDefaultToolkit().getScreenSize();
        configProp = AppConfig.load();
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (IOException e) {
            ip = "127.0.0.1";
        }
    }

    private DataBuffer(){}
}
