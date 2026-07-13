package persistencia;
import modelo.LineaOC;
import java.sql.*;
import java.util.List;

public class LineaOCDAO {

    // Métodos

    public boolean guardarLineas(Connection conn, int idOc, List<LineaOC> lineas) throws SQLException {
        if (lineas == null || lineas.isEmpty()) {
            return true;
        }
        String SQL = "INSERT INTO linea_oc (id_oc, sku_producto, cantidad, precio_compra_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            for (LineaOC linea : lineas) {
                pstmt.setInt(1, idOc);
                pstmt.setString(2, linea.getSkuProducto());
                pstmt.setInt(3, linea.getCantidad());
                pstmt.setDouble(4, linea.getPrecioCompraUnitario());
                pstmt.setDouble(5, linea.getSubtotal());
                pstmt.addBatch();
            }
            int[] resultados = pstmt.executeBatch();
            for (int res : resultados) {
                if (res <= 0) {
                    return false; 
                }
            }
            return true;
        } catch (SQLException e) {
            throw e;
        }
    }
}