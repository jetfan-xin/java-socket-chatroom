package dev.jingfan.jchat;

import dev.jingfan.jchat.client.model.entity.OnlineUserListModel;
import dev.jingfan.jchat.common.model.entity.Message;
import dev.jingfan.jchat.common.model.entity.Request;
import dev.jingfan.jchat.common.model.entity.Response;
import dev.jingfan.jchat.common.model.entity.ResponseStatus;
import dev.jingfan.jchat.common.model.entity.ResponseType;
import dev.jingfan.jchat.common.model.entity.User;
import dev.jingfan.jchat.common.protocol.ProtocolActions;
import dev.jingfan.jchat.common.ui.AvatarIcons;
import dev.jingfan.jchat.server.controller.RequestProcessor;
import dev.jingfan.jchat.server.model.entity.OnlineUserTableModel;
import dev.jingfan.jchat.server.model.service.UserService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Closeable;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CopyOnWriteArrayList;

/** Dependency-free smoke tests that run in CI without opening Swing windows. */
public final class ChatroomSmokeTest {
    private static int checks;

    private ChatroomSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        testProtocolSerialization();
        testOnlineUserModels();
        testGeneratedAvatars();
        testUserPersistence();
        testTwoClientSocketFlow();
        System.out.println("Smoke tests passed: " + checks + " checks");
    }

    private static void testProtocolSerialization() throws Exception {
        User sender = user(8, "secret", "Ada", 'f', 3);
        Message message = new Message();
        message.setFromUser(sender);
        message.setMessage("Hello");

        Request request = new Request();
        request.setAction("chat");
        request.setAttribute("msg", message);

        Request copy = roundTrip(request, Request.class);
        Message copiedMessage = (Message) copy.getAttribute("msg");
        check("chat".equals(copy.getAction()), "request action survives serialization");
        check("Hello".equals(copiedMessage.getMessage()), "message survives serialization");

        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.CHAT);
        response.setData("txtMsg", message);
        Response responseCopy = roundTrip(response, Response.class);
        check(responseCopy.getType() == ResponseType.CHAT, "response type survives serialization");
        check(!sender.toString().contains("secret"), "user logs do not expose passwords");
    }

    private static void testOnlineUserModels() {
        User ada = user(8, "secret", "Ada", 'f', 3);
        User linus = user(9, "secret", "Linus", 'm', 4);
        OnlineUserListModel listModel = new OnlineUserListModel(new CopyOnWriteArrayList<>());
        listModel.addElement(ada);
        listModel.addElement(ada);
        listModel.addElement(linus);
        check(listModel.getSize() == 2, "online list rejects duplicate users");
        check(listModel.removeElement(ada), "online list removes existing users");
        check(listModel.getSize() == 1, "online list reports removal");

        OnlineUserTableModel tableModel = new OnlineUserTableModel();
        tableModel.add(new String[]{"8", "Ada", "f"});
        tableModel.add(new String[]{"9", "Linus", "m"});
        tableModel.remove(8);
        check(tableModel.getRowCount() == 1, "server table removes the matching row");
        tableModel.remove(404);
        check(tableModel.getRowCount() == 1, "server table ignores an unknown account");
    }

    private static void testGeneratedAvatars() {
        check(AvatarIcons.create(2).getIconWidth() == 40, "default avatar width is stable");
        check(AvatarIcons.create(2, 56).getIconHeight() == 56, "custom avatar size is honored");
    }

    private static void testUserPersistence() throws Exception {
        Path directory = Files.createTempDirectory("jchat-users-");
        Path database = directory.resolve("users.db");
        dev.jingfan.jchat.server.DataBuffer.configProp.setProperty("dbpath", database.toString());

        try {
            UserService service = new UserService();
            check(service.loadAllUser().size() == 4, "missing database creates four demo users");
            check(service.login(2, "333333") != null, "valid credentials authenticate");
            check(service.login(2, "wrong") == null, "invalid credentials are rejected");

            User newUser = new User("portfolio", "Grace", 'f', 5);
            service.addUser(newUser);
            check(newUser.getId() == 5, "registration allocates the next account ID");
            check(service.loadUser(5) != null, "registered user persists to disk");
        } finally {
            Files.deleteIfExists(database);
            Files.deleteIfExists(directory);
        }
    }

    private static void testTwoClientSocketFlow() throws Exception {
        Path directory = Files.createTempDirectory("jchat-integration-");
        Path database = directory.resolve("users.db");
        dev.jingfan.jchat.server.DataBuffer.configProp.setProperty("dbpath", database.toString());
        dev.jingfan.jchat.server.DataBuffer.onlineUserIOCacheMap.clear();
        dev.jingfan.jchat.server.DataBuffer.onlineUsersMap.clear();
        new UserService().initUser();

        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Thread acceptor = new Thread(() -> acceptClients(serverSocket, 2),
                    "smoke-test-acceptor");
            acceptor.start();

            try (TestClient first = new TestClient(serverSocket.getLocalPort());
                 TestClient second = new TestClient(serverSocket.getLocalPort())) {
                User firstUser = first.login("2", "333333");
                User secondUser = second.login("3", "444444");
                check(firstUser.getId() == 2, "first client signs in over a real socket");
                check(secondUser.getId() == 3, "second client signs in over a real socket");

                Response presence = first.read();
                check(presence.getType() == ResponseType.LOGIN,
                        "existing client receives an online-presence event");

                Message message = new Message();
                message.setFromUser(firstUser);
                message.setMessage("Integration hello");
                Request chat = new Request();
                chat.setAction(ProtocolActions.CHAT);
                chat.setAttribute("msg", message);
                first.send(chat);

                Response delivered = second.read();
                Message deliveredMessage = (Message) delivered.getData("txtMsg");
                check(delivered.getType() == ResponseType.CHAT,
                        "public chat is routed to the other client");
                check("Integration hello".equals(deliveredMessage.getMessage()),
                        "routed chat preserves its content");

                first.logout(firstUser);
                Response signedOut = second.read();
                check(signedOut.getType() == ResponseType.LOGOUT,
                        "other client receives a sign-out event");
                second.logout(secondUser);
            }

            acceptor.join(3_000);
            check(!acceptor.isAlive(), "integration acceptor terminates cleanly");
        } finally {
            dev.jingfan.jchat.server.DataBuffer.onlineUserIOCacheMap.clear();
            dev.jingfan.jchat.server.DataBuffer.onlineUsersMap.clear();
            Files.deleteIfExists(database);
            Files.deleteIfExists(directory);
        }
    }

    private static void acceptClients(ServerSocket serverSocket, int clientCount) {
        try {
            for (int index = 0; index < clientCount; index++) {
                Socket socket = serverSocket.accept();
                Thread handler = new Thread(new RequestProcessor(socket),
                        "smoke-test-handler-" + index);
                handler.start();
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Integration acceptor failed", exception);
        }
    }

    private static User user(long id, String password, String nickname, char sex, int avatar) {
        User user = new User(password, nickname, sex, avatar);
        user.setId(id);
        return user;
    }

    private static <T> T roundTrip(Object value, Class<T> type) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(value);
        }
        try (ObjectInputStream input = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            return type.cast(input.readObject());
        }
    }

    private static void check(boolean condition, String description) {
        checks++;
        if (!condition) {
            throw new AssertionError("Failed: " + description);
        }
    }

    private static final class TestClient implements Closeable {
        private final Socket socket;
        private final ObjectOutputStream output;
        private final ObjectInputStream input;

        private TestClient(int port) throws Exception {
            socket = new Socket("127.0.0.1", port);
            socket.setSoTimeout(3_000);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        }

        private User login(String account, String password) throws Exception {
            Request request = new Request();
            request.setAction(ProtocolActions.LOGIN);
            request.setAttribute("id", account);
            request.setAttribute("password", password);
            send(request);
            return (User) read().getData("user");
        }

        private void logout(User user) throws Exception {
            Request request = new Request();
            request.setAction(ProtocolActions.EXIT);
            request.setAttribute("user", user);
            send(request);
            read();
        }

        private void send(Request request) throws Exception {
            output.writeObject(request);
            output.flush();
        }

        private Response read() throws Exception {
            return (Response) input.readObject();
        }

        @Override
        public void close() throws java.io.IOException {
            input.close();
            output.close();
            socket.close();
        }
    }
}
