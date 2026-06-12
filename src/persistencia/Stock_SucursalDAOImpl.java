package persistencia;

import modelo.Stock_SucursalDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class Stock_SucursalDAOImpl {

    // Consultas SQL
    private static final String SQL_LISTAR_POR_SUCURSAL = 
        "SELECT " +
        "    p.sku, p.nombre AS nombre_producto, s.id_sucursal, s.nombre AS nombre_sucursal, " +
        "    COALESCE(ss.id, 0) AS id_stock, " + 
        "    COALESCE(ss.cantidad_disponible, 0) AS cantidad_actual, " + 
        "    ss.fecha_ultimo_movimiento " +
        "FROM producto p " +
        "CROSS JOIN sucursales s " +
        "LEFT JOIN stock_sucursal ss ON p.sku = ss.sku_producto AND s.id_sucursal = ss.id_sucursal " +
        "WHERE s.id_sucursal = ? " + 
        "ORDER BY p.nombre";

    private static final String SQL_BUSCAR_GLOBAL = 
        "SELECT " +
        "    p.sku, p.nombre AS nombre_producto, s.id_sucursal, s.nombre AS nombre_sucursal, " +
        "    COALESCE(ss.id, 0) AS id_stock, " + 
        "    COALESCE(ss.cantidad_disponible, 0) AS cantidad_actual, " + 
        "    ss.fecha_ultimo_movimiento " +
        "FROM producto p " +
        "CROSS JOIN sucursales s " +
        "LEFT JOIN stock_sucursal ss ON p.sku = ss.sku_producto AND s.id_sucursal = ss.id_sucursal " +
        "WHERE p.nombre LIKE ? OR p.sku LIKE ? " + 
        "ORDER BY p.nombre, s.nombre";

    private static final String SQL_ACTUALIZAR_CANTIDAD = 
        "UPDATE stock_sucursal SET cantidad_disponible = ?, fecha_ultimo_movimiento = ? WHERE id = ?";
    
    private static final String SQL_INSERTAR_CANTIDAD = 
        "INSERT INTO stock_sucursal (sku_producto, id_sucursal, cantidad_disponible, fecha_ultimo_movimiento) VALUES (?, ?, ?, ?)";

    /**
     * Lista el stock de todos los productos para una sucursal específica.
     */
    public List<Stock_SucursalDTO> listarStockPorSucursal(int idSucursal) throws SQLException {
        List<Stock_SucursalDTO> stockList = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_LISTAR_POR_SUCURSAL)) {
            
            pstmt.setInt(1, idSucursal);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stockList.add(mapearDTO(rs));
                }
            }
        } 
        return stockList;
    }

    /**
     * Busca productos por nombre o SKU en todas las sucursales disponibles.
     */
    public List<Stock_SucursalDTO> buscarStockGlobal(String criterio) throws SQLException {
        List<Stock_SucursalDTO> stockList = new ArrayList<>();
        String filtro = "%" + criterio + "%";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_BUSCAR_GLOBAL)) {
            
            pstmt.setString(1, filtro);
            pstmt.setString(2, filtro);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stockList.add(mapearDTO(rs));
                }
            }
        } 
        return stockList;
    }

    /**
     * Inserta o actualiza el registro de stock según corresponda.
     */
    public boolean actualizarStock(Stock_SucursalDTO stock) throws SQLException {
        if (stock.getId() > 0) { 
            try (Connection conn = ConexionDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(SQL_ACTUALIZAR_CANTIDAD)) {
                
                pstmt.setInt(1, stock.getCantidadDisponible());
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(3, stock.getId());
                
                return pstmt.executeUpdate() > 0;
            }
        } else {
            try (Connection conn = ConexionDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(SQL_INSERTAR_CANTIDAD)) {
                
                pstmt.setString(1, stock.getSkuProducto()); 
                pstmt.setInt(2, stock.getIdSucursal());
                pstmt.setInt(3, stock.getCantidadDisponible());
                pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                
                return pstmt.executeUpdate() > 0;
            }
        }
    }

    /**
     * Método auxiliar para evitar repetir la lógica de creación del DTO.
     */
    private Stock_SucursalDTO mapearDTO(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("fecha_ultimo_movimiento");
        LocalDateTime ultimaActualizacion = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();
        
        return new Stock_SucursalDTO(
            rs.getInt("id_stock"),
            rs.getString("sku"),
            rs.getString("nombre_producto"),
            rs.getInt("id_sucursal"),
            rs.getString("nombre_sucursal"),
            rs.getInt("cantidad_actual"),
            ultimaActualizacion
        );
    }
}