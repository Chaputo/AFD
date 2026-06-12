package persistencia;

import modelo.Stock_Sucursal;
import java.sql.*;
import java.time.LocalDateTime;

public class StockTransaccionalHandler { 

    public Stock_Sucursal obtenerStockActual(Connection conn, String skuProducto, int idSucursal) throws SQLException {
        String SQL = "SELECT id, cantidad_disponible, fecha_ultimo_movimiento FROM stock_sucursal WHERE sku_producto = ? AND id_sucursal = ?";
        Stock_Sucursal stock = null;
        
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, skuProducto); 
            pstmt.setInt(2, idSucursal);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha_ultimo_movimiento");
                    LocalDateTime fecha = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

                    stock = new Stock_Sucursal(
                        rs.getInt("id"), 
                        skuProducto, 
                        idSucursal,
                        rs.getInt("cantidad_disponible"), 
                        fecha
                    );
                }
            }
        }
        return stock;
    }

    public boolean actualizarStockTransaccional(Connection conn, Stock_Sucursal nuevoStock, String tipoMovimiento) throws SQLException {
        
        Stock_Sucursal stockExistente = obtenerStockActual(conn, nuevoStock.getSkuProducto(), nuevoStock.getIdSucursal());
        int cantidadMovida = nuevoStock.getCantidad(); 

        if (stockExistente == null) {
            if (!"INGRESO".equals(tipoMovimiento)) {
                throw new SQLException("No se puede realizar un " + tipoMovimiento + " sin stock previo.");
            }

            String SQL_INSERT = "INSERT INTO stock_sucursal (sku_producto, id_sucursal, cantidad_disponible, fecha_ultimo_movimiento) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT)) {
                pstmt.setString(1, nuevoStock.getSkuProducto());
                pstmt.setInt(2, nuevoStock.getIdSucursal());
                pstmt.setInt(3, cantidadMovida); 
                pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                return pstmt.executeUpdate() > 0;
            }
        } else {
            int saldoActual = stockExistente.getCantidad();
            int saldoFinal = "INGRESO".equals(tipoMovimiento) ? (saldoActual + cantidadMovida) : (saldoActual - cantidadMovida);
            
            if (saldoFinal < 0) throw new SQLException("Stock insuficiente.");

            String SQL_UPDATE = "UPDATE stock_sucursal SET cantidad_disponible = ?, fecha_ultimo_movimiento = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE)) {
                pstmt.setInt(1, saldoFinal);
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(3, stockExistente.getIdStock());
                return pstmt.executeUpdate() > 0;
            }
        }
    }
}