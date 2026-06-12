package persistencia;

import modelo.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    
    // Centralizamos el mapeo para evitar repetir código
    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getInt("id_cliente"), 
            rs.getString("nombre"),
            rs.getString("telefono"),
            rs.getString("direccion")
        );
    }

    /**
     * Este es el método que soluciona el error en tu panel.
     * Decide automáticamente si debe insertar o actualizar.
     */
    public boolean guardarOActualizar(Cliente cliente) throws SQLException {
        if (cliente.getId() == 0) {
            return guardar(cliente);
        } else {
            return actualizar(cliente);
        }
    }

    public boolean guardar(Cliente cliente) throws SQLException {
        // Agregamos 'activo' para que sea consistente con tu DB
        String SQL = "INSERT INTO cliente (nombre, telefono, direccion, activo) VALUES (?, ?, ?, 1)";
        
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getTelefono());
            pstmt.setString(3, cliente.getDireccion());
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        cliente.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }
    
    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        // Solo traemos los clientes que no fueron eliminados (activo = 1)
        String SQL = "SELECT id_cliente, nombre, telefono, direccion FROM cliente WHERE activo = 1 ORDER BY nombre";
        
        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            
            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
        } 
        return clientes;
    }
    
    public boolean actualizar(Cliente cliente) throws SQLException {
        String SQL = "UPDATE cliente SET nombre = ?, telefono = ?, direccion = ? WHERE id_cliente = ?";
        
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            
            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getTelefono());
            pstmt.setString(3, cliente.getDireccion());
            pstmt.setInt(4, cliente.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean eliminar(int id) throws SQLException {
        // Cambio: Borrado lógico en lugar de físico para no romper pedidos viejos
        String SQL = "UPDATE cliente SET activo = 0 WHERE id_cliente = ?";
        
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public List<Cliente> buscar(String termino) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        // Buscamos solo en los activos
        String SQL = "SELECT id_cliente, nombre, telefono, direccion FROM cliente " +
                     "WHERE activo = 1 AND (telefono LIKE ? OR nombre LIKE ?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            String terminoLike = "%" + termino + "%";
            pstmt.setString(1, terminoLike);
            pstmt.setString(2, terminoLike); 

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapearCliente(rs));
                }
            }
        }
        return clientes;
    }
}