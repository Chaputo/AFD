package persistencia;

import java.sql.SQLException;
import java.util.Map;

public interface DashboardDAO {

    // Métodos para el ComboBox
    Map<Integer, String> obtenerSucursales() throws SQLException;

    // Métodos KPI
    double obtenerVentasTotalesMes(Integer idSucursal, Integer mes) throws SQLException;
    double obtenerTicketPromedio(Integer idSucursal, Integer mes) throws SQLException;
    int obtenerAlertasStockCritico(Integer idSucursal) throws SQLException;
    int obtenerPedidosPendientes(Integer idSucursal) throws SQLException;

    // Métodos para Gráficos
    Map<String, Double> obtenerTicketPromedioUltimosMeses(Integer idSucursal) throws SQLException;
    Map<String, Integer> obtenerVentasPorDiaSemana(Integer idSucursal, Integer mes) throws SQLException;
    Map<String, Integer> obtenerHorariosConcurridos(Integer idSucursal, Integer mes) throws SQLException;
    Map<String, Integer> obtenerTop5ProductosVendidosMes(Integer idSucursal, Integer mes) throws SQLException;
}