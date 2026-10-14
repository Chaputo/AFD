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
        String sql = "DELETE FROM sucursales WHERE id_sucursal = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}