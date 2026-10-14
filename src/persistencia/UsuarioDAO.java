package persistencia;
import modelo.Usuario;
import util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // Consulta base
    private static final String SELECT_BASE = "SELECT id_usuario, nombre_completo, nombre_usuario, rol FROM usuarios WHERE activo = 1";

    // Métodos

    public Usuario autenticar(String nombreUsuario, String password) throws SQLException {
        Usuario usuario = null;
        String SQL = "SELECT id_usuario, nombre_completo, nombre_usuario, rol, password_hash FROM usuarios WHERE activo = 1 AND nombre_usuario = ?";
        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, nombreUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String guardado = rs.getString("password_hash");
                    if (PasswordUtil.verificar(password, guardado)) {
                        usuario = mapearUsuario(rs);
                        // Migra contraseñas viejas en texto plano al primer login exitoso
                        if (!PasswordUtil.esHash(guardado)) {
                            actualizarHash(conn, usuario.getId(), password);
                        }
                    }
                }
            }
        }
        return usuario;
    }

    private void actualizarHash(Connection conn, int idUsuario, String password) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("UPDATE usuarios SET password_hash = ? WHERE id_usuario = ?")) {
            pstmt.setString(1, PasswordUtil.hashear(password));
            pstmt.setInt(2, idUsuario);
            pstmt.executeUpdate();
        }
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String SQL = SELECT_BASE + " ORDER BY nombre_completo ASC";
        try (Connection conn = ConexionDB.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SQL)) {
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }

    public boolean guardarOActualizar(Usuario usuario) throws SQLException {
        return (usuario.getId() == 0) ? guardarNuevo(usuario) : actualizar(usuario);
    }

    private boolean guardarNuevo(Usuario usuario) throws SQLException {
        String SQL = "INSERT INTO usuarios (nombre_completo, nombre_usuario, password_hash, rol, activo) VALUES (?, ?, ?, ?, 1)"; 
        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getUsuario());
            pstmt.setString(3, PasswordUtil.hashear(usuario.getPassword()));
            pstmt.setString(4, usuario.getRol());
            return pstmt.executeUpdate() > 0;
        }
    }

    private boolean actualizar(Usuario usuario) throws SQLException {
        boolean tienePass = (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty());
        String SQL;
        if (tienePass) {
            SQL = "UPDATE usuarios SET nombre_completo = ?, nombre_usuario = ?, password_hash = ?, rol = ? WHERE id_usuario = ?"; 
        } else {
            SQL = "UPDATE usuarios SET nombre_completo = ?, nombre_usuario = ?, rol = ? WHERE id_usuario = ?";
        }
        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getUsuario());
            if (tienePass) {
                pstmt.setString(3, PasswordUtil.hashear(usuario.getPassword()));
                pstmt.setString(4, usuario.getRol());
                pstmt.setInt(5, usuario.getId());
            } else {
                pstmt.setString(3, usuario.getRol());
                pstmt.setInt(4, usuario.getId());
            }
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean esAdmin(int idUsuario) throws SQLException {
        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("SELECT rol FROM usuarios WHERE id_usuario = ?")) {
            pstmt.setInt(1, idUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && "ADMIN".equalsIgnoreCase(rs.getString("rol"));
            }
        }
    }

    public int contarAdminsActivos() throws SQLException {
        try (Connection conn = ConexionDB.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM usuarios WHERE activo = 1 AND rol = 'ADMIN'")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String SQL = "UPDATE usuarios SET activo = 0 WHERE id_usuario = ?";
        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario user = new Usuario(
            rs.getInt("id_usuario"),
            rs.getString("nombre_completo"),
            rs.getString("rol")
        );
        user.setUsuario(rs.getString("nombre_usuario"));
        return user;
    }
}