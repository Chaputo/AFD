package persistencia;

import modelo.Stock_Sucursal;
import java.sql.*;
import java.time.LocalDateTime;

public class MovimientoStockDAO {

    private final StockTransaccionalHandler stockHandler = new StockTransaccionalHandler();

    /**
     * Ajusta el stock de un producto en una sucursal a un valor fijo (inventario físico).
     * La diferencia se calcula contra el stock real de la base (con la fila bloqueada),
     * y se audita como INGRESO o EGRESO según corresponda.
     * @return la diferencia aplicada (0 si el stock ya era ese valor)
     */
    public int ajustarAStockFijo(String sku, int idSucursal, int stockDestino) throws SQLException {
    Connection conn = null;
    try {
        conn = ConexionDB.getConnection();
        conn.setAutoCommit(false);

        // Obtener el ID del usuario en sesión
        int idUsuario = modelo.SesionUsuario.getIdUsuarioOr(1);

        Stock_Sucursal actual = stockHandler.obtenerStockActual(conn, sku, idSucursal);
        int diferencia = stockDestino - (actual != null ? actual.getCantidad() : 0);
        if (diferencia == 0) {
            conn.rollback();
            return 0;
        }

        Stock_Sucursal movimiento = new Stock_Sucursal(0, sku, idSucursal, Math.abs(diferencia), LocalDateTime.now());
        String tipo = (diferencia > 0) ? "INGRESO" : "EGRESO";

        // Actualizar el stock y registrar en movimiento_stock
        if (!stockHandler.actualizarStockTransaccional(conn, movimiento, tipo, idUsuario)) {
            conn.rollback();
            throw new SQLException("No se pudo actualizar el stock.");
        }

        conn.commit();
        return diferencia;

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
