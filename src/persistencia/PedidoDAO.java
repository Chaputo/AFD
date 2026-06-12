package persistencia;

import modelo.Pedido;
import modelo.DetallePedido;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    public boolean registrarVenta(Pedido pedido, List<DetallePedido> detalles, int idSucursal) throws Exception {
        Connection conn = null;
        int pedidoId = -1; 

        try {
            conn = ConexionDB.getConnection();
            if (conn == null) return false;
            
            conn.setAutoCommit(false); 

            // 1. Insertar Pedido (Agregada la columna tipo_pago)
            String SQL_PEDIDO = "INSERT INTO pedidos (fecha_pedido, total_pedido, id_cliente, id_usuario, id_sucursal, estado_entrega, tipo_pago) VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmtPedido = conn.prepareStatement(SQL_PEDIDO, Statement.RETURN_GENERATED_KEYS)) {
                pstmtPedido.setTimestamp(1, Timestamp.valueOf(pedido.getFechaHora()));
                pstmtPedido.setDouble(2, pedido.getTotal());
                pstmtPedido.setInt(3, pedido.getIdCliente());
                pstmtPedido.setInt(4, pedido.getIdUsuario());
                pstmtPedido.setInt(5, idSucursal);
                pstmtPedido.setString(6, pedido.getEstado());
                pstmtPedido.setString(7, pedido.getTipoPago()); // Guardamos el método seleccionado
                
                pstmtPedido.executeUpdate();
                
                ResultSet rs = pstmtPedido.getGeneratedKeys();
                if (rs.next()) {
                    pedidoId = rs.getInt(1);
                } else {
                    throw new SQLException("Error al obtener ID del pedido.");
                }
            }

            // 2. Insertar Detalles y Descontar Stock
            String SQL_DETALLE = "INSERT INTO pedido_detalle (id_pedido, sku_producto, precio_unitario, cantidad) VALUES (?, ?, ?, ?)";
            String SQL_STOCK = "UPDATE stock_sucursal SET cantidad_disponible = cantidad_disponible - ? WHERE sku_producto = ? AND id_sucursal = ?";

            try (PreparedStatement psDetalle = conn.prepareStatement(SQL_DETALLE);
                 PreparedStatement psStock = conn.prepareStatement(SQL_STOCK)) {
                
                for (DetallePedido d : detalles) {
                    // Detalle
                    psDetalle.setInt(1, pedidoId);
                    psDetalle.setString(2, d.getSku()); 
                    psDetalle.setDouble(3, d.getPrecioUnitario());
                    psDetalle.setInt(4, d.getCantidad());
                    psDetalle.executeUpdate();

                    // Stock
                    psStock.setInt(1, d.getCantidad());
                    psStock.setString(2, d.getSku());
                    psStock.setInt(3, idSucursal);
                    psStock.executeUpdate();
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
            if (nuevoEstado.equalsIgnoreCase("Devuelto")) {
                operacionStock = "ss.cantidad_disponible = ss.cantidad_disponible + pd.cantidad"; 
            } 
            else if (estadoAnterior.equalsIgnoreCase("Devuelto") && 
                    (nuevoEstado.equalsIgnoreCase("Entregado") || nuevoEstado.equalsIgnoreCase("Pendiente"))) {
                operacionStock = "ss.cantidad_disponible = ss.cantidad_disponible - pd.cantidad";
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

    public List<Pedido> listarPedidosActivos() throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        
        // MODIFICACIÓN: Agregamos JOINs para traer los nombres reales desde las tablas relacionadas
        // NOTA: Ajustá "clientes" o "usuarios" y sus columnas si en tu BD se llaman de otra forma (ej: "id" en vez de "id_cliente")
        String sql = "SELECT p.id_pedido, p.fecha_pedido, p.total_pedido, p.estado_entrega, p.tipo_pago, " +
                     "c.nombre AS nombre_cliente, u.nombre AS nombre_usuario " +
                     "FROM pedidos p " +
                     "INNER JOIN clientes c ON p.id_cliente = c.id_cliente " +
                     "INNER JOIN usuarios u ON p.id_usuario = u.id_usuario " +
                     "WHERE p.estado_entrega NOT IN ('Finalizado', 'Cancelado') " + // Corregido 'Entregado' por 'Finalizado'
                     "ORDER BY p.fecha_pedido DESC";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pedido p = new Pedido();
                p.setId(rs.getInt("id_pedido"));
                p.setFechaHora(rs.getTimestamp("fecha_pedido").toLocalDateTime());
                p.setTotalPedido(rs.getDouble("total_pedido"));
                p.setEstadoEntrega(rs.getString("estado_entrega"));
                p.setTipoPago(rs.getString("tipo_pago"));
                
                // ¡ACÁ ESTÁ LA MAGIA! Seteamos los nombres que recuperamos del JOIN
                p.setNombreCliente(rs.getString("nombre_cliente"));
                p.setNombreUsuario(rs.getString("nombre_usuario"));
                
                lista.add(p);
            }
        }
        return lista;
    }
}