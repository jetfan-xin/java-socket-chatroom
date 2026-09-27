package dev.jingfan.jchat.server;
import dev.jingfan.jchat.common.config.AppConfig;
import dev.jingfan.jchat.common.model.entity.User;
import dev.jingfan.jchat.server.model.entity.OnlineUserTableModel;
import dev.jingfan.jchat.server.model.entity.RegisteredUserTableModel;

import java.awt.*;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;
/** Process-wide server state shared by request handlers and the admin console. */
public class DataBuffer {

    public static ServerSocket serverSocket;

    public static Map<Long, OnlineClientIOCache> onlineUserIOCacheMap;

    public static Map<Long, User> onlineUsersMap;

    public static Properties configProp;

    public static RegisteredUserTableModel registeredUserTableModel;

    public static OnlineUserTableModel onlineUserTableModel;

    public static Dimension screenSize;

    static{

        onlineUserIOCacheMap = new ConcurrentSkipListMap<>();
        onlineUsersMap = new ConcurrentSkipListMap<>();
        configProp = AppConfig.load();
        registeredUserTableModel = new RegisteredUserTableModel();
        onlineUserTableModel = new OnlineUserTableModel();
        screenSize = GraphicsEnvironment.isHeadless()
                ? new Dimension(1280, 800)
                : Toolkit.getDefaultToolkit().getScreenSize();
    }

    private DataBuffer() {
    }
}
