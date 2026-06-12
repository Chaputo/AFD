package persistencia;

import modelo.StockConsolidado;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAOImpl implements ReporteDAO {

    private static final String SQL_REPORTE_CONSOLIDADO = 
        "SELECT " +
        "    p.sku AS id_item, " + 
        "    p.nombre AS nombre_item, " +
        "    p.stockMinimo, " + 
        "    COALESCE(SUM(ss.cantidad_disponible), 0) AS stock_total " + 
        "FROM " +
        "    producto p " + 
        "LEFT JOIN " + 
        "    stock_sucursal ss ON p.sku = ss.sku_producto " +
        "GROUP BY " +
        "    p.sku, p.nombre, p.stockMinimo " + 
        "ORDER BY " +
        "    nombre_item";

    @Override
    public List<StockConsolidado> consultarStockConsolidado() throws SQLException {
        List<StockConsolidado> stockConsolidado = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(SQL_REPORTE_CONSOLIDADO);
             ResultSet rs = pstmt.executeQuery()) 
        {
            while (rs.next()) {
                String skuStr = rs.getString("id_item");
                int idItem = 0; 
                if (skuStr != null) {
                    try { 
                        idItem = Integer.parseInt(skuStr.replaceAll("[^0-9]", "")); 
                    } catch(Exception e) { idItem = 0; }
                }

                String nombreItem = rs.getString("nombre_item");
                int stockTotal = rs.getInt("stock_total"); 
                int stockMinimo = rs.getInt("stockMinimo"); 
                
                StockConsolidado item = new StockConsolidado(
                    idItem, 
                    nombreItem, 
                    stockTotal, 
                    stockMinimo
                );
                stockConsolidado.add(item);
            }
        } 
        return stockConsolidado;
    }

    /**
     * Obtiene los nombres de todas las sucursales cargadas en el sistema de manera ordenada.
     */
    @Override
    public List<String> obtenerNombresSucursales() throws SQLException {
        List<String> sucursales = new ArrayList<>();
        String sql = "SELECT nombre FROM sucursales ORDER BY id_sucursal";
        
        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sucursales.add(rs.getString("nombre"));
            }
        }
        return sucursales;
    }

    /**
     * Consulta el stock general desglosado de forma matricial (PIVOT) por cada sucursal activa.
     * Devuelve una lista de arrays listos para inyectar directo en el DefaultTableModel.
     */
    @Override
    public List<Object[]> consultarStockPorSucursal(List<String> nombresSucursales) throws SQLException {
        List<Object[]> datosReporte = new ArrayList<>();
        
        // 1. Construcción dinámica del Query SQL tipo Pivot
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.sku AS id_item, p.nombre AS nombre_item, p.stockMinimo, ")
           .append("COALESCE(SUM(ss.cantidad_disponible), 0) AS stock_total ");
        
        // Agregamos un SUM(CASE WHEN...) condicional por cada sucursal recibida por parámetro
        for (String suc : nombresSucursales) {
            String sucEscapada = suc.replace("'", "''"); // Evitar inyecciones o roturas por comillas simples
            sql.append(", COALESCE(SUM(CASE WHEN suc.nombre = '").append(sucEscapada)
               .append("' THEN ss.cantidad_disponible ELSE 0 END), 0) AS `").append(sucEscapada).append("` ");
        }
        
        sql.append("FROM producto p ")
           .append("LEFT JOIN stock_sucursal ss ON p.sku = ss.sku_producto ")
           .append("LEFT JOIN sucursales suc ON ss.id_sucursal = suc.id_sucursal ")
           .append("GROUP BY p.sku, p.nombre, p.stockMinimo ")
           .append("ORDER BY p.nombre");

        // 2. Ejecución y mapeo de datos plano a Object[] para que la UI no dependa de modelos rígidos
        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {
            
            while (rs.next()) {
                // Dimensión: ID + Nombre + StockTotal + N_Sucursales + Mínimo + Estado
                int cantidadColumnas = 5 + nombresSucursales.size();
                Object[] fila = new Object[cantidadColumnas];
                
                // Mapeamos los datos base. Mantenemos el SKU original de la BD para que no se repitan
                fila[0] = rs.getString("id_item"); 
                fila[1] = rs.getString("nombre_item");
                
                int stockTotal = rs.getInt("stock_total");
                fila[2] = stockTotal;
                
                // Extraemos dinámicamente el valor de stock para cada columna de sucursal
                int idx = 3;
                for (String suc : nombresSucursales) {
                    fila[idx] = rs.getInt(suc);
                    idx++;
                }
                
                int stockMinimo = rs.getInt("stockMinimo");
                fila[idx] = stockMinimo;
                
                // Definimos el Flag de estado basándonos en el stock consolidado total
                fila[idx + 1] = (stockTotal < stockMinimo) ? "🔴 CRÍTICO" : "🟢 Normal";
                
                datosReporte.add(fila);
            }
        }
        return datosReporte;
    }
}