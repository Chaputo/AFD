package persistencia;

import modelo.MovimientoStock;
import modelo.Stock_Sucursal;
import java.sql.*;
import java.time.LocalDateTime;

public class MovimientoStockDAO {

    private final StockTransaccionalHandler stockHandler = new StockTransaccionalHandler(); 

    public boolean registrarIngreso(MovimientoStock movimiento) throws SQLException {
    Connection conn = null;
    try {
        conn = ConexionDB.getConnection();
        conn.setAutoCommit(false); 

        // Obtener el ID del usuario en sesión
        int idUsuario = (modelo.SesionUsuario.getUsuario() != null) 
                ? modelo.SesionUsuario.getUsuario().getId() 
                : 1;

        Stock_Sucursal stockParaActualizar = new Stock_Sucursal(
             0, 
             movimiento.getSkuProducto(), 
             movimiento.getIdSucursal(), 
             movimiento.getCantidad(), 
             LocalDateTime.now()
        );

        // 1. Actualizar el stock y registrar en movimiento_stock
        boolean exitoStock = stockHandler.actualizarStockTransaccional(conn, stockParaActualizar, "INGRESO", idUsuario);
        
        if (!exitoStock) {
            conn.rollback();
            return false;
        }

        // ELIMINAR todo el paso 2 y paso 3 de inserción en SQL_INSERT_MOVIMIENTO
        
        conn.commit(); 
        return true;

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