package persistencia;

import modelo.LineaOC;
import modelo.OrdenDeCompra;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrdenDeCompraDAO {

    // Método auxiliar para consultar el stock disponible en la sucursal (bloquea la fila: se usa dentro de transacciones)
    private int obtenerStockActual(Connection conn, String sku, int idSucursal) throws SQLException {
        String sql = "SELECT cantidad_disponible FROM stock_sucursal WHERE sku_producto = ? AND id_sucursal = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku);
            ps.setInt(2, idSucursal);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cantidad_disponible");
                }
            }
        }
        return 0;
    }

    public boolean guardarOCCompleta(OrdenDeCompra oc) throws SQLException {
        Connection con = null;
        PreparedStatement psCabecera = null;
        PreparedStatement psDetalle = null;
        ResultSet rs = null;
        String sqlCabecera = "INSERT INTO orden_compra (id_proveedor, id_usuario, fecha_emision, total, estado) VALUES (?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO linea_oc (id_oc, sku_producto, cantidad, precio_compra_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        try {
            con = ConexionDB.getConnection(); 
            con.setAutoCommit(false); 
            psCabecera = con.prepareStatement(sqlCabecera, Statement.RETURN_GENERATED_KEYS);
            psCabecera.setInt(1, oc.getIdProveedor()); 
            psCabecera.setInt(2, oc.getIdUsuario());
            psCabecera.setTimestamp(3, Timestamp.valueOf(oc.getFechaEmision()));
            psCabecera.setDouble(4, oc.getTotal());
            psCabecera.setString(5, oc.getEstado());
            int filasAfectadas = psCabecera.executeUpdate();
            if (filasAfectadas == 0) {
                con.rollback();
                return false;
            }
            rs = psCabecera.getGeneratedKeys();
            int idOcGenerado = 0;
            if (rs.next()) {
                idOcGenerado = rs.getInt(1); 
            } else {
                con.rollback();
                return false;
            }
            psDetalle = con.prepareStatement(sqlDetalle);
            for (LineaOC linea : oc.getLineasOC()) { 
                psDetalle.setInt(1, idOcGenerado); 
                psDetalle.setString(2, linea.getSkuProducto()); 
                psDetalle.setInt(3, linea.getCantidad());
                psDetalle.setDouble(4, linea.getPrecioCompraUnitario()); 
                psDetalle.setDouble(5, linea.getSubtotal());
                psDetalle.addBatch(); 
            }
            psDetalle.executeBatch();
            con.commit();
            return true;
        } catch (SQLException ex) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException rbEx) { rbEx.printStackTrace(); }
            }
            throw ex; 
        } finally {
            if (rs != null) rs.close();
            if (psCabecera != null) psCabecera.close();
            if (psDetalle != null) psDetalle.close();
            if (con != null) con.close();
        }
    }

    public List<OrdenDeCompra> listarOrdenesActivas(boolean mostrarTodo) throws SQLException {
        List<OrdenDeCompra> lista = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuilder sql = new StringBuilder(
            "SELECT o.id_oc, o.fecha_emision, o.total, o.estado, o.id_proveedor, o.id_usuario, " +
            "p.nombre AS proveedor, u.nombre_completo AS usuario " +
            "FROM orden_compra o " +
            "INNER JOIN proveedor p ON o.id_proveedor = p.id_proveedor " +
            "INNER JOIN usuarios u ON o.id_usuario = u.id_usuario "
        );
        if (!mostrarTodo) {
            sql.append("WHERE o.estado NOT IN ('RECIBIDA', 'CANCELADA') ");
        }
        sql.append("ORDER BY o.id_oc DESC");
        try {
            con = ConexionDB.getConnection();
            ps = con.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                OrdenDeCompra oc = new OrdenDeCompra();
                oc.setId(rs.getInt("id_oc"));
                oc.setIdProveedor(rs.getInt("id_proveedor"));
                oc.setIdUsuario(rs.getInt("id_usuario"));
                oc.setFechaEmision(rs.getTimestamp("fecha_emision").toLocalDateTime());
                oc.setTotal(rs.getDouble("total"));
                oc.setEstado(rs.getString("estado"));
                oc.setNombreProveedor(rs.getString("proveedor"));
                oc.setNombreUsuario(rs.getString("usuario"));
                lista.add(oc);
            }
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (con != null) con.close();
        }
        return lista;
    }

    /**
     * Cambia el estado de una OC impactando el stock:
     * - Al pasar a RECIBIDA suma la mercadería en la sucursal elegida (los movimientos quedan vinculados a la OC).
     * - RECIBIDA solo puede pasar a CANCELADA, y resta la mercadería de la sucursal donde se recibió.
     * - CANCELADA es un estado final.
     * El estado anterior se relee de la base; el parámetro estadoAnterior queda por compatibilidad.
     */
    public boolean actualizarEstadoConStock(int idOC, String estadoAnterior, String nuevoEstado, int idSucursal) throws SQLException {
        String sqlUpsertStock = "INSERT INTO stock_sucursal (sku_producto, id_sucursal, cantidad_disponible, fecha_ultimo_movimiento) " +
                                "VALUES (?, ?, ?, NOW()) " +
                                "ON DUPLICATE KEY UPDATE cantidad_disponible = cantidad_disponible + VALUES(cantidad_disponible), fecha_ultimo_movimiento = NOW()";
        String sqlInsertMovimiento = "INSERT INTO movimiento_stock (fecha, sku_producto, id_sucursal, tipo, id_usuario, cantidad, stock_resultante, id_oc_relacionada) " +
                                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);

            // 1. Releer estado actual y usuario emisor bloqueando la OC
            String estadoActual;
            int idUsuario;
            try (PreparedStatement ps = con.prepareStatement("SELECT estado, id_usuario FROM orden_compra WHERE id_oc = ? FOR UPDATE")) {
                ps.setInt(1, idOC);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("La OC #" + idOC + " no existe.");
                    estadoActual = rs.getString("estado");
                    idUsuario = rs.getInt("id_usuario");
                }
            }

            if (estadoActual.equalsIgnoreCase(nuevoEstado)) {
                con.rollback();
                return true;
            }
            if (estadoActual.equalsIgnoreCase("CANCELADA")) {
                throw new SQLException("La OC #" + idOC + " está CANCELADA y ya no puede cambiar de estado.");
            }
            if (estadoActual.equalsIgnoreCase("RECIBIDA") && !nuevoEstado.equalsIgnoreCase("CANCELADA")) {
                throw new SQLException("La OC #" + idOC + " ya fue RECIBIDA: solo puede pasar a CANCELADA.");
            }

            // 2. Calcular qué cantidades mover y en qué sucursal: {sku, idSucursal, variacion}
            List<Object[]> movimientos = new ArrayList<>();
            String tipoMovimiento = null;

            if (nuevoEstado.equalsIgnoreCase("RECIBIDA")) {
                tipoMovimiento = "INGRESO";
                try (PreparedStatement ps = con.prepareStatement("SELECT sku_producto, cantidad FROM linea_oc WHERE id_oc = ?")) {
                    ps.setInt(1, idOC);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            movimientos.add(new Object[] { rs.getString("sku_producto"), idSucursal, rs.getInt("cantidad") });
                        }
                    }
                }
            } else if (estadoActual.equalsIgnoreCase("RECIBIDA")) {
                // Cancelación de una OC recibida: revertir lo ingresado en la sucursal donde entró
                tipoMovimiento = "AJUSTE";
                String sqlIngresos = "SELECT sku_producto, id_sucursal, SUM(cantidad) AS cantidad FROM movimiento_stock " +
                                     "WHERE id_oc_relacionada = ? AND tipo = 'INGRESO' GROUP BY sku_producto, id_sucursal";
                try (PreparedStatement ps = con.prepareStatement(sqlIngresos)) {
                    ps.setInt(1, idOC);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            movimientos.add(new Object[] { rs.getString("sku_producto"), rs.getInt("id_sucursal"), -rs.getInt("cantidad") });
                        }
                    }
                }
                // OCs recibidas antes de vincular movimientos: se usa la sucursal elegida en pantalla
                if (movimientos.isEmpty()) {
                    try (PreparedStatement ps = con.prepareStatement("SELECT sku_producto, cantidad FROM linea_oc WHERE id_oc = ?")) {
                        ps.setInt(1, idOC);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                movimientos.add(new Object[] { rs.getString("sku_producto"), idSucursal, -rs.getInt("cantidad") });
                            }
                        }
                    }
                }
                // No se puede revertir mercadería que ya se vendió
                for (Object[] m : movimientos) {
                    int disponible = obtenerStockActual(con, (String) m[0], (Integer) m[1]);
                    if (disponible + (Integer) m[2] < 0) {
                        throw new SQLException("No se puede cancelar: el producto " + m[0] + " tiene " + disponible
                                + " unidades y habría que restar " + (-(Integer) m[2]) + ".");
                    }
                }
            }

            // 3. Actualizar el estado de la OC
            try (PreparedStatement ps = con.prepareStatement("UPDATE orden_compra SET estado = ? WHERE id_oc = ?")) {
                ps.setString(1, nuevoEstado);
                ps.setInt(2, idOC);
                ps.executeUpdate();
            }

            // 4. Impactar stock y auditar
            try (PreparedStatement psStock = con.prepareStatement(sqlUpsertStock);
                 PreparedStatement psMov = con.prepareStatement(sqlInsertMovimiento)) {
                for (Object[] m : movimientos) {
                    String sku = (String) m[0];
                    int sucursal = (Integer) m[1];
                    int variacion = (Integer) m[2];

                    psStock.setString(1, sku);
                    psStock.setInt(2, sucursal);
                    psStock.setInt(3, variacion);
                    psStock.executeUpdate();

                    psMov.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    psMov.setString(2, sku);
                    psMov.setInt(3, sucursal);
                    psMov.setString(4, tipoMovimiento);
                    psMov.setInt(5, idUsuario);
                    psMov.setInt(6, variacion);
                    psMov.setInt(7, obtenerStockActual(con, sku, sucursal));
                    psMov.setInt(8, idOC);
                    psMov.executeUpdate();
                }
            }

            con.commit();
            return true;
        } catch (SQLException ex) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException rbEx) { rbEx.printStackTrace(); }
            }
            throw ex;
        } finally {
            if (con != null) con.close();
        }
    }

    public List<Object[]> buscarDetalleProductosPorOrden(int idOC) throws SQLException {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT l.sku_producto, p.nombre AS producto_nombre, l.cantidad, l.precio_compra_unitario, l.subtotal " +
                     "FROM linea_oc l " +
                     "INNER JOIN producto p ON l.sku_producto = p.sku " +
                     "WHERE l.id_oc = ?";    
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOC);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[] {
                        rs.getString("sku_producto"),
                        rs.getString("producto_nombre"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio_compra_unitario"),
                        rs.getDouble("subtotal")
                    };
                    lista.add(fila);
                }
            }
        }
        return lista;
    }
}