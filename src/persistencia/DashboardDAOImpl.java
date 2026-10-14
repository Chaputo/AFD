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

    // Solo cuentan como venta los pedidos que no fueron devueltos ni cancelados
    private static String ventaValida(String alias) {
        return alias + "estado_entrega NOT IN ('Devuelto', 'Cancelado')";
    }

    // Período del dashboard: un mes del año actual (MONTH = ?) o todo el año actual si no se elige mes
    private static String condicionPeriodo(String alias, boolean filtrarMes) {
        String anio = "YEAR(" + alias + "fecha_pedido) = YEAR(CURRENT_DATE())";
        return filtrarMes ? "MONTH(" + alias + "fecha_pedido) = ? AND " + anio : anio;
    }

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
    public double obtenerVentasTotalesMes(Integer idSucursal, Integer mes) throws SQLException {
        boolean filtrarSucursal = (idSucursal != null && idSucursal > 0);
        boolean filtrarMes = (mes != null && mes > 0);

        String condicionFecha = condicionPeriodo("", filtrarMes) + " AND " + ventaValida("");

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
    public double obtenerTicketPromedio(Integer idSucursal, Integer mes) throws SQLException {
        boolean filtrarSucursal = (idSucursal != null && idSucursal > 0);
        boolean filtrarMes = (mes != null && mes > 0);

        String condicionFecha = condicionPeriodo("", filtrarMes) + " AND " + ventaValida("");

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
        return reporteDAO.contarStockCritico(idSucursal);
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
        // Los 6 meses más recientes con ventas (se traen en orden descendente y se invierten)
        String sql = "SELECT DATE_FORMAT(MIN(fecha_pedido), '%m/%Y') AS mes, AVG(total_pedido) AS ticket_promedio " +
                     "FROM pedidos WHERE " + ventaValida("") + " " +
                     (filtrar ? "AND id_sucursal = ? " : "") +
                     "GROUP BY YEAR(fecha_pedido), MONTH(fecha_pedido) " +
                     "ORDER BY YEAR(fecha_pedido) DESC, MONTH(fecha_pedido) DESC LIMIT 6";

        List<String> meses = new java.util.ArrayList<>();
        List<Double> valores = new java.util.ArrayList<>();
        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (filtrar) stmt.setInt(1, idSucursal);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    meses.add(0, rs.getString("mes"));
                    valores.add(0, rs.getDouble("ticket_promedio"));
                }
            }
        }
        for (int i = 0; i < meses.size(); i++) {
            datos.put(meses.get(i), valores.get(i));
        }
        return datos;
    }

    @Override
    public Map<String, Integer> obtenerTop5ProductosVendidosMes(Integer idSucursal, Integer mes) throws SQLException {
        Map<String, Integer> datos = new LinkedHashMap<>();
        boolean filtrarSucursal = (idSucursal != null && idSucursal > 0);
        boolean filtrarMes = (mes != null && mes > 0);

        String condicionMes = condicionPeriodo("pe.", filtrarMes) + " AND " + ventaValida("pe.");

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
            "COUNT(*) AS cantidad FROM pedidos WHERE " + ventaValida("") + " "
        );

        if (filtrarSucursal) sql.append("AND id_sucursal = ? ");
        sql.append("AND ").append(condicionPeriodo("", filtrarMes)).append(" ");

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
    public Map<String, Integer> obtenerHorariosConcurridos(Integer idSucursal, Integer mes) throws SQLException {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        boolean filtrarSucursal = (idSucursal != null && idSucursal > 0);
        boolean filtrarMes = (mes != null && mes > 0);

        StringBuilder sql = new StringBuilder(
            "SELECT DATE_FORMAT(fecha_pedido, '%H:00') AS hora, COUNT(*) AS cantidad " +
            "FROM pedidos WHERE " + ventaValida("") + " "
        );

        if (filtrarSucursal) sql.append("AND id_sucursal = ? ");
        sql.append("AND ").append(condicionPeriodo("", filtrarMes)).append(" ");

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