package persistencia;

import modelo.Pedido;
import modelo.DetallePedido;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    // Auxiliar para obtener el stock disponible actual de un producto en una sucursal
    private int obtenerStockActual(Connection conn, String sku, int idSucursal) throws SQLException {
        String sql = "SELECT cantidad_disponible FROM stock_sucursal WHERE sku_producto = ? AND id_sucursal = ?";
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

    public boolean registrarVenta(Pedido pedido, List<DetallePedido> detalles, int idSucursal) throws Exception {
        Connection conn = null;
        int pedidoId = -1; 
        try {
            conn = ConexionDB.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false); 
            
            String SQL_PEDIDO = "INSERT INTO pedidos (fecha_pedido, total_pedido, id_cliente, id_usuario, id_sucursal, estado_entrega, tipo_pago) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtPedido = conn.prepareStatement(SQL_PEDIDO, Statement.RETURN_GENERATED_KEYS)) {
                pstmtPedido.setTimestamp(1, Timestamp.valueOf(pedido.getFechaHora()));
                pstmtPedido.setDouble(2, pedido.getTotal());
                pstmtPedido.setInt(3, pedido.getIdCliente());
                pstmtPedido.setInt(4, pedido.getIdUsuario());
                pstmtPedido.setInt(5, idSucursal);
                pstmtPedido.setString(6, pedido.getEstado());
                pstmtPedido.setString(7, pedido.getTipoPago());
                pstmtPedido.executeUpdate();
                
                ResultSet rs = pstmtPedido.getGeneratedKeys();
                if (rs.next()) {
                    pedidoId = rs.getInt(1);
                } else {
                    throw new SQLException("Error al obtener ID del pedido.");
                }
            }

            String SQL_DETALLE = "INSERT INTO pedido_detalle (id_pedido, sku_producto, precio_unitario, cantidad) VALUES (?, ?, ?, ?)";
            String SQL_STOCK = "UPDATE stock_sucursal SET cantidad_disponible = cantidad_disponible - ? WHERE sku_producto = ? AND id_sucursal = ?";
            String SQL_MOVIMIENTO = "INSERT INTO movimiento_stock (fecha, sku_producto, id_sucursal, tipo, id_usuario, cantidad, stock_resultante) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement psDetalle = conn.prepareStatement(SQL_DETALLE);
                 PreparedStatement psStock = conn.prepareStatement(SQL_STOCK);
                 PreparedStatement psMovimiento = conn.prepareStatement(SQL_MOVIMIENTO)) {
                
                for (DetallePedido d : detalles) {
                    // 1. Insertar detalle del pedido
                    psDetalle.setInt(1, pedidoId);
                    psDetalle.setString(2, d.getSku()); 
                    psDetalle.setDouble(3, d.getPrecioUnitario());
                    psDetalle.setInt(4, d.getCantidad());
                    psDetalle.executeUpdate();

                    // 2. Descontar del stock
                    psStock.setInt(1, d.getCantidad());
                    psStock.setString(2, d.getSku());
                    psStock.setInt(3, idSucursal);
                    psStock.executeUpdate();

                    // 3. Obtener el nuevo stock para registrar en auditoría
                    int stockNuevo = obtenerStockActual(conn, d.getSku(), idSucursal);

                    // 4. Registrar en movimiento_stock para auditoría (Egresos en negativo)
                    psMovimiento.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    psMovimiento.setString(2, d.getSku());
                    psMovimiento.setInt(3, idSucursal);
                    psMovimiento.setString(4, "VENTA"); // O "PEDIDO"
                    psMovimiento.setInt(5, pedido.getIdUsuario());
                    psMovimiento.setInt(6, -d.getCantidad()); // Negativo porque es una salida
                    psMovimiento.setInt(7, stockNuevo);
                    psMovimiento.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            e.printStackTrace();
            throw e; 
        } finally {
            if (conn != null) conn.close();
        }
    }

    public boolean actualizarEstadoConStock(int idPedido, String estadoAnterior, String nuevoEstado) throws SQLException {
        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);
            
            String sqlPedido = "UPDATE pedidos SET estado_entrega = ? WHERE id_pedido = ?"; 
            PreparedStatement psPedido = con.prepareStatement(sqlPedido);
            psPedido.setString(1, nuevoEstado);
            psPedido.setInt(2, idPedido);
            psPedido.executeUpdate();

            String operacionStock = "";
            String tipoMovimiento = "";
            int signo = 1;

            if (nuevoEstado.equalsIgnoreCase("Devuelto")) {
                operacionStock = "ss.cantidad_disponible = ss.cantidad_disponible + pd.cantidad"; 
                tipoMovimiento = "DEVOLUCION";
                signo = 1;
            } 
            else if (estadoAnterior.equalsIgnoreCase("Devuelto") && 
                    (nuevoEstado.equalsIgnoreCase("Entregado") || nuevoEstado.equalsIgnoreCase("Pendiente"))) {
                operacionStock = "ss.cantidad_disponible = ss.cantidad_disponible - pd.cantidad";
                tipoMovimiento = "VENTA";
                signo = -1;
            }

            if (!operacionStock.isEmpty()) {
                String sqlStock = "UPDATE stock_sucursal ss " +
                                  "INNER JOIN pedido_detalle pd ON ss.sku_producto = pd.sku_producto " +
                                  "INNER JOIN pedidos p ON pd.id_pedido = p.id_pedido " +
                                  "SET " + operacionStock + " " +
                                  "WHERE pd.id_pedido = ? AND ss.id_sucursal = p.id_sucursal";
                PreparedStatement psStock = con.prepareStatement(sqlStock);
                psStock.setInt(1, idPedido);
                psStock.executeUpdate();

                // Registrar movimientos en la auditoría para cada ítem afectado
                String sqlDetalles = "SELECT p.id_sucursal, p.id_usuario, pd.sku_producto, pd.cantidad " +
                                     "FROM pedido_detalle pd INNER JOIN pedidos p ON pd.id_pedido = p.id_pedido " +
                                     "WHERE pd.id_pedido = ?";
                String sqlMov = "INSERT INTO movimiento_stock (fecha, sku_producto, id_sucursal, tipo, id_usuario, cantidad, stock_resultante) VALUES (?, ?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement psDet = con.prepareStatement(sqlDetalles);
                     PreparedStatement psMov = con.prepareStatement(sqlMov)) {
                    psDet.setInt(1, idPedido);
                    try (ResultSet rs = psDet.executeQuery()) {
                        while (rs.next()) {
                            int idSucursal = rs.getInt("id_sucursal");
                            int idUsuario = rs.getInt("id_usuario");
                            String sku = rs.getString("sku_producto");
                            int cantidad = rs.getInt("cantidad");
                            int stockNuevo = obtenerStockActual(con, sku, idSucursal);

                            psMov.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                            psMov.setString(2, sku);
                            psMov.setInt(3, idSucursal);
                            psMov.setString(4, tipoMovimiento);
                            psMov.setInt(5, idUsuario);
                            psMov.setInt(6, cantidad * signo);
                            psMov.setInt(7, stockNuevo);
                            psMov.executeUpdate();
                        }
                    }
                }
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) con.close();
        }
    }

    public boolean actualizarEstado(int idPedido, String nuevoEstado) throws SQLException {
        String sql = "UPDATE pedidos SET estado_entrega = ? WHERE id_pedido = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idPedido);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Pedido> listarPedidosActivos(boolean incluirInactivos) throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT p.id_pedido, p.fecha_pedido, p.total_pedido, p.estado_entrega, p.tipo_pago, " +
                    "c.nombre AS nombre_cliente, u.nombre_completo AS nombre_usuario " + 
                    "FROM pedidos p " +
                    "INNER JOIN cliente c ON p.id_cliente = c.id_cliente " + 
                    "INNER JOIN usuarios u ON p.id_usuario = u.id_usuario ";
        if (!incluirInactivos) {
            sql += "WHERE p.estado_entrega IN ('Pendiente', 'En camino') ";
        }
        sql += "ORDER BY p.fecha_pedido DESC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pedido p = new Pedido();
                p.setId(rs.getInt("id_pedido"));
                p.setFechaHora(rs.getTimestamp("fecha_pedido").toLocalDateTime());
                p.setTotal(rs.getDouble("total_pedido"));         
                p.setEstado(rs.getString("estado_entrega"));      
                p.setTipoPago(rs.getString("tipo_pago"));
                p.setNombreCliente(rs.getString("nombre_cliente"));
                p.setNombreUsuario(rs.getString("nombre_usuario")); 
                lista.add(p);
            }
        }
        return lista;
    }

    public List<Object[]> buscarDetalleProductosPorPedido(int idPedido) throws SQLException {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT pd.sku_producto, p.nombre AS producto_nombre, pd.cantidad, pd.precio_unitario, " +
                    "(pd.cantidad * pd.precio_unitario) AS subtotal " +
                    "FROM pedido_detalle pd " + 
                    "INNER JOIN producto p ON pd.sku_producto = p.sku " +
                    "WHERE pd.id_pedido = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[] {
                        rs.getString("sku_producto"),
                        rs.getString("producto_nombre"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio_unitario"),
                        rs.getDouble("subtotal")
                    };
                    lista.add(fila);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
            throw e; 
        }
        return lista;
    }

    public boolean devolverProductoIndividual(int idPedido, String skuProducto, int cantidadADevolver) throws SQLException {
        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            if (con == null) return false;
            con.setAutoCommit(false);

            // 1. Obtener la sucursal y el usuario del pedido
            int idSucursal = -1;
            int idUsuario = 1; // Por defecto sistema
            String sqlSucursal = "SELECT id_sucursal, id_usuario FROM pedidos WHERE id_pedido = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlSucursal)) {
                ps.setInt(1, idPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idSucursal = rs.getInt("id_sucursal");
                        idUsuario = rs.getInt("id_usuario");
                    } else {
                        con.rollback();
                        return false;
                    }
                }
            }

            // 2. Obtener la cantidad actual comprada del producto
            int cantidadActual = 0;
            String sqlCant = "SELECT cantidad FROM pedido_detalle WHERE id_pedido = ? AND sku_producto = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlCant)) {
                ps.setInt(1, idPedido);
                ps.setString(2, skuProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        cantidadActual = rs.getInt("cantidad");
                    } else {
                        con.rollback();
                        return false;
                    }
                }
            }

            if (cantidadADevolver > cantidadActual) {
                con.rollback();
                throw new IllegalArgumentException("La cantidad a devolver excede la cantidad comprada.");
            }

            // 3. Reintegrar la cantidad devuelta al stock de la sucursal
            String sqlStock = "UPDATE stock_sucursal SET cantidad_disponible = cantidad_disponible + ? WHERE sku_producto = ? AND id_sucursal = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlStock)) {
                ps.setInt(1, cantidadADevolver);
                ps.setString(2, skuProducto);
                ps.setInt(3, idSucursal);
                ps.executeUpdate();
            }

            // 4. Registrar la devolución en movimiento_stock (Ingreso positivo)
            int stockNuevo = obtenerStockActual(con, skuProducto, idSucursal);
            String sqlMov = "INSERT INTO movimiento_stock (fecha, sku_producto, id_sucursal, tipo, id_usuario, cantidad, stock_resultante) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlMov)) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(2, skuProducto);
                ps.setInt(3, idSucursal);
                ps.setString(4, "DEVOLUCION");
                ps.setInt(5, idUsuario);
                ps.setInt(6, cantidadADevolver); // Positivo porque vuelve al stock
                ps.setInt(7, stockNuevo);
                ps.executeUpdate();
            }

            // 5. Actualizar o eliminar la línea del detalle
            if (cantidadADevolver == cantidadActual) {
                String sqlDeleteDet = "DELETE FROM pedido_detalle WHERE id_pedido = ? AND sku_producto = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlDeleteDet)) {
                    ps.setInt(1, idPedido);
                    ps.setString(2, skuProducto);
                    ps.executeUpdate();
                }
            } else {
                String sqlUpdateDet = "UPDATE pedido_detalle SET cantidad = cantidad - ? WHERE id_pedido = ? AND sku_producto = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlUpdateDet)) {
                    ps.setInt(1, cantidadADevolver);
                    ps.setInt(2, idPedido);
                    ps.setString(3, skuProducto);
                    ps.executeUpdate();
                }
            }

            // 6. Recalcular el total_pedido en la tabla pedidos
            String sqlUpdateTotal = "UPDATE pedidos SET total_pedido = COALESCE((SELECT SUM(cantidad * precio_unitario) FROM pedido_detalle WHERE id_pedido = ?), 0) WHERE id_pedido = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpdateTotal)) {
                ps.setInt(1, idPedido);
                ps.setInt(2, idPedido);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) con.close();
        }
    }
}