package persistencia;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAOImpl implements ReporteDAO {

    // Métodos

    @Override
    public List<String> obtenerNombresSucursales() throws SQLException {
        List<String> sucursales = new ArrayList<>();
        String sql = "SELECT nombre FROM sucursales WHERE activa = 1 ORDER BY id_sucursal";
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
           .append("COALESCE(SUM(CASE WHEN suc.activa = 1 THEN ss.cantidad_disponible ELSE 0 END), 0) AS stock_total ");
        // Alias por posición (suc_0, suc_1...) para no depender de caracteres especiales del nombre
        for (int i = 0; i < nombresSucursales.size(); i++) {
            String sucEscapada = nombresSucursales.get(i).replace("\\", "\\\\").replace("'", "''");
            sql.append(", COALESCE(SUM(CASE WHEN suc.nombre = '").append(sucEscapada)
               .append("' THEN ss.cantidad_disponible ELSE 0 END), 0) AS suc_").append(i).append(" ");
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
                for (int i = 0; i < nombresSucursales.size(); i++) {
                    fila[idx] = rs.getInt("suc_" + i);
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

    @Override
    public int contarStockCritico(Integer idSucursal) throws SQLException {
        boolean filtrar = (idSucursal != null && idSucursal > 0);
        // Incluye productos agotados (stock 0) en las sucursales donde se manejan
        String sql = "SELECT COUNT(*) FROM stock_sucursal ss " +
                     "JOIN producto p ON p.sku = ss.sku_producto " +
                     "JOIN sucursales s ON s.id_sucursal = ss.id_sucursal " +
                     "WHERE ss.cantidad_disponible < p.stockMinimo AND s.activa = 1" +
                     (filtrar ? " AND ss.id_sucursal = ?" : "");
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filtrar) ps.setInt(1, idSucursal);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}