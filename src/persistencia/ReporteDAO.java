package persistencia;

import modelo.StockConsolidado;
import java.sql.SQLException;
import java.util.List;

public interface ReporteDAO {
    List<StockConsolidado> consultarStockConsolidado() throws SQLException;
    
    // NUEVOS MÉTODOS PARA EL DESGLOSE DE SUCURSALES
    List<String> obtenerNombresSucursales() throws SQLException;
    List<Object[]> consultarStockPorSucursal(List<String> nombresSucursales) throws SQLException;
}