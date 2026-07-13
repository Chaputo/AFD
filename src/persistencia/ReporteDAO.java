package persistencia;
import modelo.StockConsolidado;
import java.sql.SQLException;
import java.util.List;

public interface ReporteDAO {

    // Métodos

    List<StockConsolidado> consultarStockConsolidado() throws SQLException;
    List<String> obtenerNombresSucursales() throws SQLException;
    List<Object[]> consultarStockPorSucursal(List<String> nombresSucursales) throws SQLException;
}