package persistencia;
import modelo.Stock_SucursalDTO;
import java.sql.SQLException;
import java.util.List;

public interface Stock_SucursalDAO { 

    // Métodos

    List<Stock_SucursalDTO> listarStockPorSucursal(int idSucursal) throws SQLException;
    boolean actualizarStock(Stock_SucursalDTO stock) throws SQLException;
}