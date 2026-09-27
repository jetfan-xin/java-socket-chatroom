package dev.jingfan.jchat.server.model.service;

import dev.jingfan.jchat.common.model.entity.User;
import dev.jingfan.jchat.server.DataBuffer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** Persists demo accounts and authenticates login requests. */
public class UserService {
    private static final AtomicLong ID_COUNT = new AtomicLong(4);

    public static void setIdCount(long count) {
        ID_COUNT.updateAndGet(current -> Math.max(current, count));
        System.out.println("Registered users: " + count);
    }

    public synchronized void addUser(User user) {
        List<User> users = loadAllUser();
        long highestId = users.stream()
                .map(User::getId)
                .max(Comparator.naturalOrder())
                .orElse(0L);
        ID_COUNT.updateAndGet(current -> Math.max(current, highestId));
        user.setId(ID_COUNT.incrementAndGet());
        users.add(user);
        saveAllUser(users);
    }

    public User login(long id, String password) {
        return loadAllUser().stream()
                .filter(user -> id == user.getId() && password.equals(user.getPassword()))
                .findFirst()
                .orElse(null);
    }

    public User loadUser(long id) {
        return loadAllUser().stream()
                .filter(user -> id == user.getId())
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public List<User> loadAllUser() {
        Path path = databasePath();
        if (Files.notExists(path)) {
            List<User> defaults = defaultUsers();
            saveAllUser(defaults);
            return defaults;
        }

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            List<User> users = (List<User>) input.readObject();
            long highestId = users.stream()
                    .map(User::getId)
                    .max(Comparator.naturalOrder())
                    .orElse(0L);
            ID_COUNT.updateAndGet(current -> Math.max(current, highestId));
            return new CopyOnWriteArrayList<>(users);
        } catch (IOException | ClassNotFoundException exception) {
            throw new IllegalStateException("Could not read user database at " + path, exception);
        }
    }

    private synchronized void saveAllUser(List<User> users) {
        Path path = databasePath();
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (ObjectOutputStream output = new ObjectOutputStream(
                    new FileOutputStream(path.toFile()))) {
                output.writeObject(new ArrayList<>(users));
                output.flush();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write user database at " + path, exception);
        }
    }

    public void initUser() {
        saveAllUser(defaultUsers());
        ID_COUNT.set(4);
    }

    private List<User> defaultUsers() {
        List<User> users = new CopyOnWriteArrayList<>();
        users.add(user(1, "admin", "Admin", 'f', 0));
        users.add(user(2, "333333", "Zhang", 'm', 1));
        users.add(user(3, "444444", "Li", 'f', 2));
        users.add(user(4, "555555", "Wang", 'f', 3));
        return users;
    }

    private User user(long id, String password, String nickname, char sex, int avatar) {
        User user = new User(password, nickname, sex, avatar);
        user.setId(id);
        return user;
    }

    private Path databasePath() {
        return Paths.get(DataBuffer.configProp.getProperty("dbpath", "data/users.db"));
    }

    public static void main(String[] args) {
        System.out.println("Initializing demo users...");
        UserService service = new UserService();
        service.initUser();
        service.loadAllUser().forEach(System.out::println);
        System.out.println("Initialization complete.");
    }
}
