package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardDAOImpl implements DashboardDAO {

    private Connection obtenerConexion() throws SQLException {
        return ConexionDB.getConnection();
    }

    private final ReporteDAO reporteDAO = new ReporteDAOImpl();

    @Override
    public Map<Integer, String> obtenerSucursales() throws SQLException {
        Map<Integer, String> sucursales = new LinkedHashMap<>();
        String sql = "SELECT id_sucursal, nombre FROM sucursales ORDER BY nombre";
        try (Connection conn = obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sucursales.put(rs.getInt("id_sucursal"), rs.getString("nombre"));
            }
        }
        return sucursales;
    }

    @Override
    public double obtenerVentasTotalesMes(Integer idSucursal) throws SQLException {
        return obtenerVentasTotalesMes(idSucursal, null);
    }

    @Override
    public double obtenerVentasTotalesMes(Integer idSucursal, Integer mes) throws SQLException {
        boolean filtrarSucursal = (idSucursal != null && idSucursal > 0);
        boolean filtrarMes = (mes != null && mes > 0);

        String condicionFecha;
        if (filtrarMes) {
            condicionFecha = "MONTH(fecha_pedido) = ? AND YEAR(fecha_pedido) = YEAR(CURRENT_DATE())";
        } else {
            condicionFecha = "DATE_FORMAT(fecha_pedido, '%Y-%m') = (SELECT DATE_FORMAT(MAX(fecha_pedido), '%Y-%m') FROM pedidos)";
        }

        String sql = "SELECT COALESCE(SUM(total_pedido), 0) FROM pedidos WHERE " + condicionFecha +
                     (filtrarSucursal ? " AND id_sucursal = ? " : "");

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if (filtrarMes) {
                stmt.setInt(paramIndex++, mes);
            }
            if (filtrarSucursal) {
                stmt.setInt(paramIndex, idSucursal);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    @Override
    public double obtenerTicketPromedio(Integer idSucursal) throws SQLException {
        return obtenerTicketPromedio(idSucursal, null);
    }

    @Override
    public double obtenerTicketPromedio(Integer idSucursal, Integer mes) throws SQLException {
        boolean filtrarSucursal = (idSucursal != null && idSucursal > 0);
        boolean filtrarMes = (mes != null && mes > 0);

        String condicionFecha;
        if (filtrarMes) {
            condicionFecha = "MONTH(fecha_pedido) = ? AND YEAR(fecha_pedido) = YEAR(CURRENT_DATE())";
        } else {
            condicionFecha = "DATE_FORMAT(fecha_pedido, '%Y-%m') = (SELECT DATE_FORMAT(MAX(fecha_pedido), '%Y-%m') FROM pedidos)";
        }

        String sql = "SELECT COALESCE(AVG(total_pedido), 0) FROM pedidos WHERE " + condicionFecha +
                     (filtrarSucursal ? " AND id_sucursal = ? " : "");

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if (filtrarMes) {
                stmt.setInt(paramIndex++, mes);
            }
            if (filtrarSucursal) {
                stmt.setInt(paramIndex, idSucursal);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    @Override
    public int obtenerAlertasStockCritico(Integer idSucursal) throws SQLException {
        List<String> sucursales = reporteDAO.obtenerNombresSucursales();
        List<Object[]> datosReporte = reporteDAO.consultarStockPorSucursal(sucursales);
        
        int contadorCriticos = 0;

        for (Object[] fila : datosReporte) {
            int minimoRecomendado = obtenerEnteroSeguro(fila[fila.length - 2]);

            if (idSucursal != null && idSucursal > 0) {
                int indiceColumnaSucursal = 3 + (idSucursal - 1);
                if (indiceColumnaSucursal < fila.length - 2) {
                    int stockSucursal = obtenerEnteroSeguro(fila[indiceColumnaSucursal]);
                    if (stockSucursal > 0 && stockSucursal < minimoRecomendado) {
                        contadorCriticos++;
                    }
                }
            } else {
                for (int colSucursal = 3; colSucursal < fila.length - 2; colSucursal++) {
                    int stockSucursal = obtenerEnteroSeguro(fila[colSucursal]);
                    if (stockSucursal > 0 && stockSucursal < minimoRecomendado) {
                        contadorCriticos++;
                    }
                }
            }
        }

        return contadorCriticos;
    }

    private int obtenerEnteroSeguro(Object objeto) {
        if (objeto == null) return 0;
        if (objeto instanceof Number) {
            return ((Number) objeto).intValue();
        }
        try {
            return Integer.parseInt(objeto.toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public int obtenerPedidosPendientes(Integer idSucursal) throws SQLException {
        boolean filtrar = (idSucursal != null && idSucursal > 0);
        String sql = "SELECT COUNT(*) FROM pedidos WHERE estado_entrega = 'Pendiente' " +
                     (filtrar ? "AND id_sucursal = ? " : "");

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (filtrar) stmt.setInt(1, idSucursal);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public Map<String, Double> obtenerTicketPromedioUltimosMeses(Integer idSucursal) throws SQLException {
        Map<String, Double> datos = new LinkedHashMap<>();
        boolean filtrar = (idSucursal != null && idSucursal > 0);
        String sql = "SELECT DATE_FORMAT(fecha_pedido, '%b') AS mes, AVG(total_pedido) AS ticket_promedio " +
                     "FROM pedidos " +
                     (filtrar ? "WHERE id_sucursal = ? " : "") +
                     "GROUP BY YEAR(fecha_pedido), MONTH(fecha_pedido) " +
                     "ORDER BY fecha_pedido ASC LIMIT 6";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (filtrar) stmt.setInt(1, idSucursal);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    datos.put(rs.getString("mes"), rs.getDouble("ticket_promedio"));
                }
            }
        }
        return datos;
    }

    @Override
    public Map<String, Integer> obtenerStockPorSucursal() throws SQLException {
        Map<String, Integer> datos = new LinkedHashMap<>();
        String sql = "SELECT s.nombre, SUM(ss.cantidad_disponible) AS total_stock " +
                     "FROM stock_sucursal ss " +
                     "JOIN sucursales s ON ss.id_sucursal = s.id_sucursal " +
                     "GROUP BY s.nombre";

        try (Connection conn = obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                datos.put(rs.getString("nombre"), rs.getInt("total_stock"));
            }
        }
        return datos;
    }

    @Override
    public Map<String, Double> obtenerVentasPorCategoria(Integer idSucursal) throws SQLException {
        return obtenerVentasPorCategoria(idSucursal, null);
    }

    @Override
    public Map<String, Double> obtenerVentasPorCategoria(Integer idSucursal, Integer mes) throws SQLException {
        Map<String, Double> datos = new LinkedHashMap<>();
        boolean filtrarSucursal = (idSucursal != null && idSucursal > 0);
        boolean filtrarMes = (mes != null && mes > 0);

        StringBuilder sql = new StringBuilder(
            "SELECT COALESCE(p.unidadMedida, 'Sin categoría') AS categoria, " +
            "SUM(pd.cantidad * pd.precio_unitario) AS total " +
            "FROM pedido_detalle pd " +
            "JOIN producto p ON pd.sku_producto = p.sku " +
            "JOIN pedidos pe ON pd.id_pedido = pe.id_pedido WHERE 1=1 "
        );

        if (filtrarSucursal) sql.append("AND pe.id_sucursal = ? ");
        if (filtrarMes) sql.append("AND MONTH(pe.fecha_pedido) = ? AND YEAR(pe.fecha_pedido) = YEAR(CURRENT_DATE()) ");

        sql.append("GROUP BY p.unidadMedida");

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (filtrarSucursal) stmt.setInt(paramIndex++, idSucursal);
            if (filtrarMes) stmt.setInt(paramIndex, mes);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    datos.put(rs.getString("categoria"), rs.getDouble("total"));
                }
            }
        }
        return datos;
    }

    @Override
    public Map<String, Integer> obtenerTop5ProductosVendidosMes(Integer idSucursal) throws SQLException {
        return obtenerTop5ProductosVendidosMes(idSucursal, null);
    }

    @Override
    public Map<String, Integer> obtenerTop5ProductosVendidosMes(Integer idSucursal, Integer mes) throws SQLException {
        Map<String, Integer> datos = new LinkedHashMap<>();
        boolean filtrarSucursal = (idSucursal != null && idSucursal > 0);
        boolean filtrarMes = (mes != null && mes > 0);

        String condicionMes = filtrarMes 
            ? "MONTH(pe.fecha_pedido) = ? AND YEAR(pe.fecha_pedido) = YEAR(CURRENT_DATE())"
            : "MONTH(pe.fecha_pedido) = MONTH(CURRENT_DATE()) AND YEAR(pe.fecha_pedido) = YEAR(CURRENT_DATE())";

        String sql = "SELECT p.nombre, SUM(pd.cantidad) AS cantidad " +
                     "FROM pedido_detalle pd " +
                     "JOIN producto p ON pd.sku_producto = p.sku " +
                     "JOIN pedidos pe ON pd.id_pedido = pe.id_pedido " +
                     "WHERE " + condicionMes + " " +
                     (filtrarSucursal ? "AND pe.id_sucursal = ? " : "") +
                     "GROUP BY p.sku, p.nombre " +
                     "ORDER BY cantidad DESC LIMIT 5";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if (filtrarMes) {
                stmt.setInt(paramIndex++, mes);
            }
            if (filtrarSucursal) {
                stmt.setInt(paramIndex, idSucursal);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    datos.put(rs.getString("nombre"), rs.getInt("cantidad"));
                }
            }
        }
        return datos;
    }

    @Override
    public Map<String, Integer> obtenerVentasPorDiaSemana(Integer idSucursal) throws SQLException {
        return obtenerVentasPorDiaSemana(idSucursal, null);
    }

    @Override
    public Map<String, Integer> obtenerVentasPorDiaSemana(Integer idSucursal, Integer mes) throws SQLException {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        resultado.put("Dom", 0);
        resultado.put("Lun", 0);
        resultado.put("Mar", 0);
        resultado.put("Mié", 0);
        resultado.put("Jue", 0);
        resultado.put("Vie", 0);
        resultado.put("Sáb", 0);

        boolean filtrarSucursal = (idSucursal != null && idSucursal > 0);
        boolean filtrarMes = (mes != null && mes > 0);

        StringBuilder sql = new StringBuilder(
            "SELECT CASE DAYOFWEEK(fecha_pedido) " +
            "  WHEN 1 THEN 'Dom' WHEN 2 THEN 'Lun' WHEN 3 THEN 'Mar' " +
            "  WHEN 4 THEN 'Mié' WHEN 5 THEN 'Jue' WHEN 6 THEN 'Vie' " +
            "  WHEN 7 THEN 'Sáb' END AS dia, " +
            "COUNT(*) AS cantidad FROM pedidos WHERE 1=1 "
        );

        if (filtrarSucursal) sql.append("AND id_sucursal = ? ");
        if (filtrarMes) sql.append("AND MONTH(fecha_pedido) = ? AND YEAR(fecha_pedido) = YEAR(CURRENT_DATE()) ");

        sql.append("GROUP BY DAYOFWEEK(fecha_pedido) ORDER BY DAYOFWEEK(fecha_pedido)");

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (filtrarSucursal) stmt.setInt(paramIndex++, idSucursal);
            if (filtrarMes) stmt.setInt(paramIndex, mes);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.put(rs.getString("dia"), rs.getInt("cantidad"));
                }
            }
        }
        return resultado;
    }

    @Override
    public Map<String, Integer> obtenerHorariosConcurridos(Integer idSucursal) throws SQLException {
        return obtenerHorariosConcurridos(idSucursal, null);
    }

    @Override
    public Map<String, Integer> obtenerHorariosConcurridos(Integer idSucursal, Integer mes) throws SQLException {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        boolean filtrarSucursal = (idSucursal != null && idSucursal > 0);
        boolean filtrarMes = (mes != null && mes > 0);

        StringBuilder sql = new StringBuilder(
            "SELECT DATE_FORMAT(fecha_pedido, '%H:00') AS hora, COUNT(*) AS cantidad " +
            "FROM pedidos WHERE 1=1 "
        );

        if (filtrarSucursal) sql.append("AND id_sucursal = ? ");
        if (filtrarMes) sql.append("AND MONTH(fecha_pedido) = ? AND YEAR(fecha_pedido) = YEAR(CURRENT_DATE()) ");

        sql.append("GROUP BY HOUR(fecha_pedido) ORDER BY HOUR(fecha_pedido)");

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (filtrarSucursal) stmt.setInt(paramIndex++, idSucursal);
            if (filtrarMes) stmt.setInt(paramIndex, mes);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.put(rs.getString("hora"), rs.getInt("cantidad"));
                }
            }
        }
        return resultado;
    }
}