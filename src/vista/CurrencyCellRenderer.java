package vista;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.DecimalFormat;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class CurrencyCellRenderer extends DefaultTableCellRenderer {
    private final DecimalFormat formatter = new DecimalFormat("#,##0.00");
    public CurrencyCellRenderer() {
        // Alinea el texto a la derecha
        setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Number) {
            value = formatter.format(value);
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}