package persistencia;

import modelo.LineaOC;
import modelo.OrdenDeCompra;
import java.sql.*;

public class OrdenDeCompraDAO {

    public boolean guardarOCCompleta(OrdenDeCompra oc) throws SQLException {
        Connection con = null;
        PreparedStatement psCabecera = null;
        PreparedStatement psDetalle = null;
        ResultSet rs = null;

        // SQL de la Cabecera (orden_compra)
        String sqlCabecera = "INSERT INTO orden_compra (id_proveedor, id_usuario, fecha_emision, total, estado) VALUES (?, ?, ?, ?, ?)";
        
        // SQL del Detalle (linea_oc) - CORREGIDO: Se mapea 'precio_compra_unitario' para solucionar el error de columna desconocida
        String sqlDetalle = "INSERT INTO linea_oc (id_oc, sku_producto, cantidad, precio_compra_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";

        try {
            con = ConexionDB.getConnection(); 
            con.setAutoCommit(false); // Iniciamos la transacción (Atomicidad)

            // 1. Insertar la Cabecera de la Orden
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

            // 2. Recuperar el id_oc generado automáticamente por MySQL
            rs = psCabecera.getGeneratedKeys();
            int idOcGenerado = 0;
            if (rs.next()) {
                idOcGenerado = rs.getInt(1); 
            } else {
                con.rollback();
                return false;
            }

            // 3. Insertar el lote de productos en linea_oc
            psDetalle = con.prepareStatement(sqlDetalle);
            for (LineaOC linea : oc.getLineasOC()) { 
                psDetalle.setInt(1, idOcGenerado); // Relación FK al id padre
                psDetalle.setString(2, linea.getSkuProducto()); 
                psDetalle.setInt(3, linea.getCantidad());
                psDetalle.setDouble(4, linea.getPrecioCompraUnitario()); 
                psDetalle.setDouble(5, linea.getSubtotal());
                
                psDetalle.addBatch(); // Agrupamos la inserción en el lote
            }

            // Ejecutamos todo el lote de líneas de un solo viaje
            psDetalle.executeBatch();
            
            // Si todo anduvo joya, impactamos los cambios en MySQL definitivamente
            con.commit();
            return true;

        } catch (SQLException ex) {
            // Ante cualquier falla en el lote, volvemos atrás para no dejar datos huérfanos
            if (con != null) {
                try { con.rollback(); } catch (SQLException rbEx) { rbEx.printStackTrace(); }
            }
            throw ex; 
        } finally {
            // Cerramos los recursos de forma segura en el bloque finally
            if (rs != null) rs.close();
            if (psCabecera != null) psCabecera.close();
            if (psDetalle != null) psDetalle.close();
        }
    }
}