package persistencia;

import modelo.Stock_SucursalDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class Stock_SucursalDAOImpl {

    // Consultas base
    private static final String SQL_LISTAR_POR_SUCURSAL = 
        "SELECT " +
        "    p.sku, p.nombre AS nombre_producto, p.stockMinimo, s.id_sucursal, s.nombre AS nombre_sucursal, " +
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
        "    p.sku, p.nombre AS nombre_producto, p.stockMinimo, s.id_sucursal, s.nombre AS nombre_sucursal, " +
        "    COALESCE(ss.id, 0) AS id_stock, " + 
        "    COALESCE(ss.cantidad_disponible, 0) AS cantidad_actual, " + 
        "    ss.fecha_ultimo_movimiento " +
        "FROM producto p " +
        "CROSS JOIN sucursales s " +
        "LEFT JOIN stock_sucursal ss ON p.sku = ss.sku_producto AND s.id_sucursal = ss.id_sucursal " +
        "WHERE p.nombre LIKE ? OR p.sku LIKE ? " + 
        "ORDER BY p.nombre, s.nombre";


    // Métodos

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
            ultimaActualizacion,
            rs.getInt("stockMinimo")
        );
    }
}