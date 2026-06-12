package persistencia;

import modelo.Proveedor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    private Proveedor mapearProveedor(ResultSet rs) throws SQLException {
        return new Proveedor(
            rs.getInt("id_proveedor"), 
            rs.getString("nombre"),
            rs.getString("cuit"),
            rs.getString("contacto")
        );
    }

    public boolean guardarOActualizar(Proveedor proveedor) throws SQLException {
        // CORREGIDO: La lógica de inserción vs actualización se maneja según el ID (0 significa registro nuevo)
        boolean esNuevo = (proveedor.getId() == 0);
        String SQL;
        
        if (esNuevo) {
            SQL = "INSERT INTO proveedor (nombre, cuit, contacto) VALUES (?, ?, ?)";
        } else {
            SQL = "UPDATE proveedor SET nombre = ?, cuit = ?, contacto = ? WHERE id_proveedor = ?";
        }
        
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, proveedor.getNombre());
            pstmt.setString(2, proveedor.getCuit());
            pstmt.setString(3, proveedor.getContacto());
            
            if (!esNuevo) {
                pstmt.setInt(4, proveedor.getId());
            }

            int filasAfectadas = pstmt.executeUpdate();
            
            if (esNuevo && filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        proveedor.setId(rs.getInt(1)); 
                    }
                }
            }
            return filasAfectadas > 0;
        }
    }
    
    public Proveedor obtenerPorCuit(String cuit) throws SQLException {
        String SQL = "SELECT id_proveedor, nombre, cuit, contacto FROM proveedor WHERE cuit = ?";
        Proveedor proveedor = null;
        
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            
            pstmt.setString(1, cuit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    proveedor = mapearProveedor(rs);
                }
            }
        }
        return proveedor;
    }

    // ADICIONAL: Método útil para recuperar un proveedor específico mediante su clave primaria
    public Proveedor obtenerPorId(int id) throws SQLException {
        String SQL = "SELECT id_proveedor, nombre, cuit, contacto FROM proveedor WHERE id_proveedor = ?";
        Proveedor proveedor = null;
        
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    proveedor = mapearProveedor(rs);
                }
            }
        }
        return proveedor;
    }

    public List<Proveedor> listarTodos() throws SQLException {
        List<Proveedor> proveedores = new ArrayList<>();
        String SQL = "SELECT id_proveedor, nombre, cuit, contacto FROM proveedor ORDER BY nombre"; 
        
        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            
            while (rs.next()) {
                proveedores.add(mapearProveedor(rs));
            }
        } 
        return proveedores;
    }
    
    public boolean eliminar(int id) throws SQLException {
        String SQL = "DELETE FROM proveedor WHERE id_proveedor = ?";
        
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            throw e; 
        }
    }
}