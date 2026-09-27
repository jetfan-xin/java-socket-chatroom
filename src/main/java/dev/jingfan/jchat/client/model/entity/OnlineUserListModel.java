package dev.jingfan.jchat.client.model.entity;
import dev.jingfan.jchat.common.model.entity.User;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;

/** Swing list model that publishes online-presence changes to the UI. */
public class OnlineUserListModel extends AbstractListModel<User> {
    private static final long serialVersionUID = -3903760573171074301L;
    private final ArrayList<User> onlineUsers;

    public OnlineUserListModel(List<User> onlineUsers) {
        this.onlineUsers = new ArrayList<>(onlineUsers);
    }

    public void addElement(User user) {
        if (onlineUsers.contains(user)) {
            return;
        }
        int index = onlineUsers.size();
        onlineUsers.add(user);
        fireIntervalAdded(this, index, index);
    }

    public boolean removeElement(User user) {
        int index = onlineUsers.indexOf(user);
        if (index >= 0) {
            onlineUsers.remove(index);
            fireIntervalRemoved(this, index, index);
            return true;
        }
        return false;
    }

    public int getSize() {
        return onlineUsers.size();
    }

    public User getElementAt(int i) {
        return onlineUsers.get(i);
    }

    public List<User> getOnlineUsers() {
        return onlineUsers;
    }
}
