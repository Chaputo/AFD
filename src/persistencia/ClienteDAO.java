package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import modelo.Cliente;

/**
 * Objeto de Acceso a Datos para la entidad Cliente.
 * Gestiona el ciclo de vida de persistencia CRUD y búsquedas avanzadas en la base de datos.
 */
public class ClienteDAO {
    
    /**
     * Convierte la fila actual de un ResultSet en un objeto de dominio Cliente.
     * 
     * @param rs ResultSet posicionado en el registro a mapear.
     * @return Instancia poblada de Cliente.
     * @throws SQLException Si ocurre un error al acceder a las columnas.
     */
    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getInt("id_cliente"), 
            rs.getString("nombre"),
            rs.getString("telefono"),
            rs.getString("direccion")
        );
    }

    /**
     * Determina si se debe insertar o actualizar el cliente según el ID recibido.
     * 
     * @param cliente Instancia a guardar o actualizar.
     * @return true si la operación fue exitosa; false en caso contrario.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public boolean guardarOActualizar(Cliente cliente) throws SQLException {
        if (cliente.getId() == 0) {
            return guardar(cliente);
        } else {
            return actualizar(cliente);
        }
    }

    /**
     * Inserta un nuevo cliente en la base de datos y le asigna el ID autogenerado.
     * 
     * @param cliente Instancia del nuevo cliente.
     * @return true si la inserción fue exitosa.
     * @throws SQLException Si ocurre un error en la sentencia SQL.
     */
    public boolean guardar(Cliente cliente) throws SQLException {
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
    
    /**
     * Obtiene todos los clientes activos registrados en el sistema ordenados por nombre.
     * 
     * @return Lista de clientes activos.
     * @throws SQLException Si ocurre un error de consulta.
     */
    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
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
    
    /**
     * Actualiza los datos de un cliente existente.
     * 
     * @param cliente Instancia con los datos modificados.
     * @return true si se actualizó el registro exitosamente.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
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
    
    /**
     * Realiza un borrado lógico del cliente desactivando su estado en la base de datos.
     * Preserva la integridad referencial con el historial de pedidos y ventas.
     * 
     * @param id Identificador único del cliente a desactivar.
     * @return true si se desactivó correctamente.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public boolean eliminar(int id) throws SQLException {
        String SQL = "UPDATE cliente SET activo = 0 WHERE id_cliente = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
}