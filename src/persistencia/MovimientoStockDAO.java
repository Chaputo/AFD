package persistencia;
import modelo.MovimientoStock;
import modelo.Stock_Sucursal;
import java.sql.*;
import java.time.LocalDateTime;

public class MovimientoStockDAO {

    private final StockTransaccionalHandler stockHandler = new StockTransaccionalHandler(); 

    // Métodos

    public boolean registrarIngreso(MovimientoStock movimiento) throws SQLException {
        
        Connection conn = null;
        String SQL_INSERT_MOVIMIENTO = "INSERT INTO movimiento_stock " +
            "(fecha, tipo, cantidad, sku_producto, id_oc_relacionada) " +
            "VALUES (?, ?, ?, ?, ?)";
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false); 
            try (PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_MOVIMIENTO)) {
                pstmt.setTimestamp(1, Timestamp.valueOf(movimiento.getFecha())); 
                pstmt.setString(2, "INGRESO"); 
                pstmt.setInt(3, movimiento.getCantidad());
                pstmt.setString(4, movimiento.getSkuProducto());
                if (movimiento.getIdOcRelacionada() != null && movimiento.getIdOcRelacionada() > 0) {
                    pstmt.setInt(5, movimiento.getIdOcRelacionada());
                } else {
                    pstmt.setNull(5, java.sql.Types.INTEGER);
                }
                pstmt.executeUpdate();
            }
            Stock_Sucursal stockParaActualizar = new Stock_Sucursal(
                 0, 
                 movimiento.getSkuProducto(), 
                 movimiento.getIdSucursal(), 
                 movimiento.getCantidad(), 
                 LocalDateTime.now()
            );
            boolean exitoStock = stockHandler.actualizarStockTransaccional(conn, stockParaActualizar, "INGRESO");
            if (exitoStock) {
                conn.commit(); 
                return true;
            } else {
                conn.rollback(); 
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e; 
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); 
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}