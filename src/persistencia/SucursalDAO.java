package persistencia;
import modelo.Sucursal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SucursalDAO {

    public List<Sucursal> listarTodas() throws SQLException {
        List<Sucursal> lista = new ArrayList<>();
        // Incluye las desactivadas: esta lista es la de administración, donde se pueden reactivar
        String sql = "SELECT id_sucursal, nombre, direccion, activa FROM sucursales ORDER BY activa DESC, id_sucursal ASC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Sucursal s = new Sucursal(
                    rs.getInt("id_sucursal"),
                    rs.getString("nombre"),
                    rs.getString("direccion")
                );
                s.setActiva(rs.getBoolean("activa"));
                lista.add(s);
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

    /**
     * Activa o desactiva una sucursal (borrado lógico). Sus pedidos, stock y movimientos se conservan:
     * una sucursal desactivada deja de mostrarse en el sistema y vuelve a aparecer al reactivarla.
     */
    public boolean cambiarEstado(int id, boolean activa) throws SQLException {
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (!activa) {
                    // Bloquea las sucursales activas para que dos desactivaciones simultáneas no dejen el sistema sin ninguna
                    int activas = 0;
                    try (PreparedStatement ps = con.prepareStatement("SELECT id_sucursal FROM sucursales WHERE activa = 1 FOR UPDATE");
                         ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) activas++;
                    }
                    if (activas <= 1) {
                        throw new SQLException("Es la única sucursal activa: no se puede desactivar.");
                    }
                }
                boolean ok;
                try (PreparedStatement ps = con.prepareStatement("UPDATE sucursales SET activa = ? WHERE id_sucursal = ?")) {
                    ps.setBoolean(1, activa);
                    ps.setInt(2, id);
                    ok = ps.executeUpdate() > 0;
                }
                con.commit();
                return ok;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}