package dev.jingfan.jchat.server.model.entity;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Table model used by the server console for registered users. */
public class RegisteredUserTableModel extends AbstractTableModel {
    private static final long serialVersionUID = -6299791067241594227L;

    private final String[] title = {"Account", "Display name", "Gender"};
    private final ArrayList<String[]> rows = new ArrayList<>();

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return title.length;
    }

    public String getColumnName(int column){
        return title[column];
    }

    @Override
    public String getValueAt(int rowIndex, int columnIndex) {
        return (rows.get(rowIndex))[columnIndex];
    }

    public void add(String[] value){
        int row = rows.size();
        rows.add(value);
        fireTableRowsInserted(row, row);
    }

    public void remove(long id){
        for(int i = 0; i < rows.size(); i++) {
            if (String.valueOf(id).equals(getValueAt(i, 0))) {
                rows.remove(i);
                fireTableRowsDeleted(i, i);
                return;
            }
        }
    }
}
