package persistencia;
import modelo.Sucursal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SucursalDAO {

    public List<Sucursal> listarTodas() throws SQLException {
        List<Sucursal> lista = new ArrayList<>();
        String sql = "SELECT id_sucursal, nombre, direccion FROM sucursales ORDER BY id_sucursal ASC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Sucursal(
                    rs.getInt("id_sucursal"),
                    rs.getString("nombre"),
                    rs.getString("direccion")
                ));
            }
        }
        return lista;
    }

    public boolean guardarOActualizar(Sucursal sucursal) throws SQLException {
        if (sucursal.getId() == 0) {
            String sql = "INSERT INTO sucursales (nombre, direccion) VALUES (?, ?)";
            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, sucursal.getNombre());
                ps.setString(2, sucursal.getDireccion());
                return ps.executeUpdate() > 0;
            }
        } else {
            String sql = "UPDATE sucursales SET nombre = ?, direccion = ? WHERE id_sucursal = ?";
            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, sucursal.getNombre());
                ps.setString(2, sucursal.getDireccion());
                ps.setInt(3, sucursal.getId());
                return ps.executeUpdate() > 0;
            }
        }
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Bloquea la sucursal mientras se valida y se borra
                try (PreparedStatement ps = con.prepareStatement("SELECT id_sucursal FROM sucursales WHERE id_sucursal = ? FOR UPDATE")) {
                    ps.setInt(1, id);
                    ps.executeQuery().close();
                }
                // Las tablas no tienen FK a sucursales: se valida acá para no dejar ventas ni stock huérfanos
                String sqlUso = "SELECT (SELECT COUNT(*) FROM pedidos WHERE id_sucursal = ?) AS pedidos, " +
                                "(SELECT COUNT(*) FROM stock_sucursal WHERE id_sucursal = ? AND cantidad_disponible <> 0) AS stock, " +
                                "(SELECT COUNT(*) FROM movimiento_stock WHERE id_sucursal = ?) AS movimientos";
                try (PreparedStatement ps = con.prepareStatement(sqlUso)) {
                    ps.setInt(1, id);
                    ps.setInt(2, id);
                    ps.setInt(3, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && (rs.getInt("pedidos") > 0 || rs.getInt("stock") > 0 || rs.getInt("movimientos") > 0)) {
                            throw new SQLException("La sucursal tiene " + rs.getInt("pedidos") + " pedido(s), "
                                    + rs.getInt("stock") + " producto(s) con stock y " + rs.getInt("movimientos")
                                    + " movimiento(s) registrados. No se puede eliminar.");
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM stock_sucursal WHERE id_sucursal = ? AND cantidad_disponible = 0")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                boolean borrada;
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM sucursales WHERE id_sucursal = ?")) {
                    ps.setInt(1, id);
                    borrada = ps.executeUpdate() > 0;
                }
                con.commit();
                return borrada;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}