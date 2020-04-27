package client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class CellPainter extends DefaultTableCellRenderer {
   private int row, col;
   String _s;
   
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int _row, int column) {
        Component comp = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, _row, column);
        row = _row;
        col = column;
 //Cells are by default rendered as a JLabel.
  /*  JLabel comp = (JLabel) super.getTableCellVisualizerComponent(table, value, isSelected, hasFocus, row, column);*/
        String s = table.getModel().getValueAt(_row, column).toString();
        _s = s;
        if (s.equals("X")) { // If it is tic
            comp.setBackground(Color.RED);
            comp.setForeground(Color.CYAN);
            //comp.setFont(Font.decode("15"));
        } else { // or it is toe
            comp.setBackground(Color.BLUE);
        }
        return comp;
    }
    
    @Override
    protected void setValue(Object v) {
        // Allow superclass to set the value.

        super.setValue(v);

        // If in names column, color cell with even row number white on
        // dark green, and cell with odd row number black on white.
        

        if(col == 4) {
                setForeground(Color.red);
                setBackground(new Color(0, 128, 0));
            } else {

            }

            return;
    }
}
