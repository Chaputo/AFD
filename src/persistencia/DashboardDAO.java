package persistencia;

import java.sql.SQLException;
import java.util.Map;

public interface DashboardDAO {

    // Métodos para el ComboBox
    Map<Integer, String> obtenerSucursales() throws SQLException;

    // Métodos KPI (el período puede ser el año actual, un mes o un rango de fechas)
    double obtenerVentasTotales(Integer idSucursal, Periodo periodo) throws SQLException;
    double obtenerTicketPromedio(Integer idSucursal, Periodo periodo) throws SQLException;
    int obtenerAlertasStockCritico(Integer idSucursal) throws SQLException;
    int obtenerPedidosPendientes(Integer idSucursal) throws SQLException;

    // Métodos para Gráficos
    Map<String, Double> obtenerTicketPromedioUltimosMeses(Integer idSucursal) throws SQLException;
    Map<String, Integer> obtenerVentasPorDiaSemana(Integer idSucursal, Periodo periodo) throws SQLException;
    Map<String, Integer> obtenerHorariosConcurridos(Integer idSucursal, Periodo periodo) throws SQLException;
    Map<String, Integer> obtenerTop5ProductosVendidos(Integer idSucursal, Periodo periodo) throws SQLException;
}
