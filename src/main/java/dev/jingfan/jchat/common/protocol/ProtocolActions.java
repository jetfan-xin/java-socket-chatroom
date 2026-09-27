package dev.jingfan.jchat.common.protocol;

/** String action names retained for compatibility with the original wire protocol. */
public final class ProtocolActions {
    public static final String REGISTER = "userRegiste";
    public static final String LOGIN = "userLogin";
    public static final String EXIT = "exit";
    public static final String CHAT = "chat";
    public static final String SHAKE = "shake";
    public static final String FILE_OFFER = "toSendFile";
    public static final String FILE_ACCEPT = "agreeReceiveFile";
    public static final String FILE_REFUSE = "refuseReceiveFile";

    private ProtocolActions() {
    }
}
