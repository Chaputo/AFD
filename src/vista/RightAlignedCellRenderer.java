package vista;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.SwingConstants;

public class RightAlignedCellRenderer extends DefaultTableCellRenderer {
    public RightAlignedCellRenderer() {
        setHorizontalAlignment(SwingConstants.RIGHT);
    }
}