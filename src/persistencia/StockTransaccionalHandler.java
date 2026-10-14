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

    /**
     * Actualiza el stock en la sucursal y registra la auditoría en movimiento_stock.
     */
    public boolean actualizarStockTransaccional(Connection conn, Stock_Sucursal nuevoStock, String tipoMovimiento, int idUsuario) throws SQLException {
        Stock_Sucursal stockExistente = obtenerStockActual(conn, nuevoStock.getSkuProducto(), nuevoStock.getIdSucursal());
        int cantidadMovida = nuevoStock.getCantidad(); 
        int stockResultante = 0;
        int cantidadFirmada = 0; // Negativo para egresos, positivo para ingresos

        if (stockExistente == null) {
            if (!"INGRESO".equalsIgnoreCase(tipoMovimiento)) {
                throw new SQLException("No se puede realizar un " + tipoMovimiento + " sin stock previo.");
            }
            
            String SQL_INSERT = "INSERT INTO stock_sucursal (sku_producto, id_sucursal, cantidad_disponible, fecha_ultimo_movimiento) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT)) {
                pstmt.setString(1, nuevoStock.getSkuProducto());
                pstmt.setInt(2, nuevoStock.getIdSucursal());
                pstmt.setInt(3, cantidadMovida); 
                pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                
                if (pstmt.executeUpdate() == 0) {
                    return false;
                }
            }
            stockResultante = cantidadMovida;
            cantidadFirmada = cantidadMovida;
            
        } else {
            int saldoActual = stockExistente.getCantidad();
            boolean esIngreso = "INGRESO".equalsIgnoreCase(tipoMovimiento);
            
            stockResultante = esIngreso ? (saldoActual + cantidadMovida) : (saldoActual - cantidadMovida);
            cantidadFirmada = esIngreso ? cantidadMovida : -cantidadMovida;

            if (stockResultante < 0) throw new SQLException("Stock insuficiente.");
            
            String SQL_UPDATE = "UPDATE stock_sucursal SET cantidad_disponible = ?, fecha_ultimo_movimiento = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE)) {
                pstmt.setInt(1, stockResultante);
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(3, stockExistente.getIdStock());
                
                if (pstmt.executeUpdate() == 0) {
                    return false;
                }
            }
        }

        // --- REGISTRO EN LA AUDITORÍA (movimiento_stock) ---
        String SQL_AUDITORIA = "INSERT INTO movimiento_stock (fecha, sku_producto, id_sucursal, tipo, id_usuario, cantidad, stock_resultante) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement psMov = conn.prepareStatement(SQL_AUDITORIA)) {
            psMov.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            psMov.setString(2, nuevoStock.getSkuProducto());
            psMov.setInt(3, nuevoStock.getIdSucursal());
            psMov.setString(4, tipoMovimiento.toUpperCase());
            psMov.setInt(5, idUsuario);
            psMov.setInt(6, cantidadFirmada);
            psMov.setInt(7, stockResultante);
            psMov.executeUpdate();
        }

        return true;
    }

    // Sobrecarga para mantener compatibilidad por si se llama sin el idUsuario
    public boolean actualizarStockTransaccional(Connection conn, Stock_Sucursal nuevoStock, String tipoMovimiento) throws SQLException {
        return actualizarStockTransaccional(conn, nuevoStock, tipoMovimiento, 1); // 1 = Usuario Sistema / Admin por defecto
    }
}