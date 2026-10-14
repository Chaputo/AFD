package persistencia;
import java.sql.SQLException;
import java.util.List;

public interface ReporteDAO {

    // Métodos
    List<String> obtenerNombresSucursales() throws SQLException;
    List<Object[]> consultarStockPorSucursal(List<String> nombresSucursales) throws SQLException;
    // Cantidad de productos por sucursal con stock por debajo del mínimo (null o 0 = todas las sucursales)
    int contarStockCritico(Integer idSucursal) throws SQLException;
}