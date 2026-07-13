package persistencia;
import modelo.StockConsolidado;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAOImpl implements ReporteDAO {

    // Consulta base

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

    // Métodos

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

    @Override
    public List<Object[]> consultarStockPorSucursal(List<String> nombresSucursales) throws SQLException {
        List<Object[]> datosReporte = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.sku AS id_item, p.nombre AS nombre_item, p.stockMinimo, ")
           .append("COALESCE(SUM(ss.cantidad_disponible), 0) AS stock_total ");
        for (String suc : nombresSucursales) {
            String sucEscapada = suc.replace("'", "''");
            sql.append(", COALESCE(SUM(CASE WHEN suc.nombre = '").append(sucEscapada)
               .append("' THEN ss.cantidad_disponible ELSE 0 END), 0) AS `").append(sucEscapada).append("` ");
        }
        sql.append("FROM producto p ")
           .append("LEFT JOIN stock_sucursal ss ON p.sku = ss.sku_producto ")
           .append("LEFT JOIN sucursales suc ON ss.id_sucursal = suc.id_sucursal ")
           .append("GROUP BY p.sku, p.nombre, p.stockMinimo ")
           .append("ORDER BY p.nombre");
        try (Connection conn = ConexionDB.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql.toString())) {
            while (rs.next()) {
                int cantidadColumnas = 5 + nombresSucursales.size();
                Object[] fila = new Object[cantidadColumnas];
                fila[0] = rs.getString("id_item"); 
                fila[1] = rs.getString("nombre_item");
                int stockTotal = rs.getInt("stock_total");
                fila[2] = stockTotal;
                int idx = 3;
                for (String suc : nombresSucursales) {
                    fila[idx] = rs.getInt(suc);
                    idx++;
                }
                int stockMinimo = rs.getInt("stockMinimo");
                fila[idx] = stockMinimo;
                fila[idx + 1] = (stockTotal < stockMinimo) ? "🔴 CRÍTICO" : "🟢 Normal";
                datosReporte.add(fila);
            }
        }
        return datosReporte;
    }
}