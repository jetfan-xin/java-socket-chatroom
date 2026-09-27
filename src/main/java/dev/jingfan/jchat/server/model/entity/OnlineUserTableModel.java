package dev.jingfan.jchat.server.model.entity;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Table model used by the server console for connected users. */
public class OnlineUserTableModel extends AbstractTableModel {
    private static final long serialVersionUID = -444245379288364831L;

    private final String[] title = {"Account", "Display name", "Gender"};
    private final ArrayList<String[]> rows = new ArrayList<>();

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return title.length;
    }

    public String getColumnName(int column) {
        return title[column];
    }

    public String getValueAt(int row, int column) {
        return (rows.get(row))[column];
    }

    public void add(String[] value) {
        int row = rows.size();
        rows.add(value);
        fireTableRowsInserted(row, row);
    }

    public void remove(long id) {
        for (int i = 0; i < rows.size(); i++) {
            if (String.valueOf(id).equals(getValueAt(i, 0))) {
                rows.remove(i);
                fireTableRowsDeleted(i, i);
                return;
            }
        }
    }
}
