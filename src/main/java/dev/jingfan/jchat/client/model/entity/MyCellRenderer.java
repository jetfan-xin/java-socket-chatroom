package dev.jingfan.jchat.client.model.entity;
import dev.jingfan.jchat.common.model.entity.User;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/** Renders an online user with a generated avatar and display name. */
public class MyCellRenderer extends JLabel implements ListCellRenderer<User> {
    private static final long serialVersionUID = 3460394416991636990L;

    public Component getListCellRendererComponent(
            JList<? extends User> list, User user, int index,
            boolean isSelected, boolean cellHasFocus) {
        String name = user.getNickname() + "(" + user.getId() + ")";
        setText(name);
        setIcon(user.getHeadIcon());
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;
    }
}
