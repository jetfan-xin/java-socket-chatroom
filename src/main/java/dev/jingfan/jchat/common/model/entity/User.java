package dev.jingfan.jchat.common.model.entity;

import dev.jingfan.jchat.common.ui.AvatarIcons;
import java.io.Serializable;
import java.util.Objects;
import javax.swing.ImageIcon;

/** Serializable user profile exchanged by the client and dev.jingfan.jchat.server. */
public class User implements Serializable {
    private static final long serialVersionUID = 5942011574971970871L;
    private long id;
    private String password;
    private String nickname;
    private int head;
    private char sex;

    public User(String password, String nickname, char sex, int head) {
        this.password = password;
        this.sex = sex;
        this.head = head;
        this.nickname = (nickname == null || nickname.isBlank()) ? "Unnamed" : nickname.trim();
    }

    public User(long id, String password) {
        this.id = id;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setSex(char sex) {
        this.sex = sex;
    }

    public char getSex() {
        return sex;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setHead(int head) {
        this.head = head;
    }

    public int getHead() {
        return head;
    }

    public ImageIcon getHeadIcon() {
        return AvatarIcons.create(head);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, password, nickname, head, sex);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        return head == other.head && id == other.id && sex == other.sex
                && Objects.equals(nickname, other.nickname)
                && Objects.equals(password, other.password);
    }

    @Override
    public String toString() {
        return "User[id=" + id + ", nickname=" + nickname + ", head=" + head
                + ", sex=" + sex + "]";
    }
}
