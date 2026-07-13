package persistencia;
import modelo.LineaOC;
import modelo.OrdenDeCompra;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdenDeCompraDAO {

    // Métodos

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
        }
    }

    public List<OrdenDeCompra> listarOrdenesActivas(boolean mostrarTodo) throws SQLException {
        List<OrdenDeCompra> lista = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuilder sql = new StringBuilder(
            "SELECT o.id_oc, o.fecha_emision, o.total, o.estado, o.id_proveedor, o.id_usuario, " +
            "p.nombre AS proveedor, u.nombre_completo AS usuario " + // <-- Usando nombre_completo
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
        }
        return lista;
    }

    public boolean actualizarEstadoConStock(int idOC, String estadoAnterior, String nuevoEstado, int idSucursal) throws SQLException {
        Connection con = null;
        PreparedStatement psEstado = null;
        PreparedStatement psLineas = null;
        PreparedStatement psStock = null;
        ResultSet rs = null;
        String sqlUpdateEstado = "UPDATE orden_compra SET estado = ? WHERE id_oc = ?";
        String sqlSelectLineas = "SELECT sku_producto, cantidad FROM linea_oc WHERE id_oc = ?";
        String sqlUpsertStock = "INSERT INTO stock_sucursal (sku_producto, id_sucursal, cantidad_disponible, fecha_ultimo_movimiento) " +
                                "VALUES (?, ?, ?, NOW()) " +
                                "ON DUPLICATE KEY UPDATE cantidad_disponible = cantidad_disponible + VALUES(cantidad_disponible), fecha_ultimo_movimiento = NOW()"; 
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);
            psEstado = con.prepareStatement(sqlUpdateEstado);
            psEstado.setString(1, nuevoEstado);
            psEstado.setInt(2, idOC);
            psEstado.executeUpdate();
            int multiplicadorStock = 0;
            if (nuevoEstado.equalsIgnoreCase("RECIBIDA") && !estadoAnterior.equalsIgnoreCase("RECIBIDA")) {
                multiplicadorStock = 1;
            } else if (estadoAnterior.equalsIgnoreCase("RECIBIDA") && nuevoEstado.equalsIgnoreCase("CANCELADA")) {
                multiplicadorStock = -1;
            }
            if (multiplicadorStock != 0) {
                psLineas = con.prepareStatement(sqlSelectLineas);
                psLineas.setInt(1, idOC);
                rs = psLineas.executeQuery();
                psStock = con.prepareStatement(sqlUpsertStock);
                while (rs.next()) {
                    String sku = rs.getString("sku_producto");
                    int cantidad = rs.getInt("cantidad");
                    psStock.setString(1, sku);
                    psStock.setInt(2, idSucursal);
                    psStock.setInt(3, cantidad * multiplicadorStock); 
                    psStock.addBatch();
                }
                psStock.executeBatch();
            }
            con.commit();
            return true;
        } catch (SQLException ex) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException rbEx) { rbEx.printStackTrace(); }
            }
            throw ex;
        } finally {
            if (rs != null) rs.close();
            if (psEstado != null) psEstado.close();
            if (psLineas != null) psLineas.close();
            if (psStock != null) psStock.close();
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