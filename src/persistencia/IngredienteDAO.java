package persistencia;
import modelo.Ingrediente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredienteDAO {

    // Métodos
    
    public List<Ingrediente> listarTodos() throws SQLException {
        List<Ingrediente> ingredientes = new ArrayList<>();
        String SQL = "SELECT id_ingrediente, nombre, cantidad_stock FROM Ingredientes ORDER BY nombre";
        try (Connection conn = ConexionDB.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SQL)) {
            while (rs.next()) {
                Ingrediente ingrediente = new Ingrediente(
                    rs.getInt("id_ingrediente"),
                    rs.getString("nombre"),
                    rs.getInt("cantidad_stock")
                );
                ingredientes.add(ingrediente);
            }
        }
        return ingredientes;
    }
    
    public Ingrediente obtenerPorId(int id) throws SQLException {
        String SQL = "SELECT id_ingrediente, nombre, cantidad_stock FROM Ingredientes WHERE id_ingrediente = ?";
        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Ingrediente(
                        rs.getInt("id_ingrediente"),
                        rs.getString("nombre"),
                        rs.getInt("cantidad_stock")
                    );
                }
            }
        }
        return null; 
    }

    public Ingrediente obtenerPorIdEnConexion(Connection conn, int id) throws SQLException {
        String SQL = "SELECT id_ingrediente, nombre, cantidad_stock FROM Ingredientes WHERE id_ingrediente = ?";
        Ingrediente ingrediente = null;
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ingrediente = new Ingrediente(
                        rs.getInt("id_ingrediente"),
                        rs.getString("nombre"),
                        rs.getInt("cantidad_stock")
                    );
                }
            }
        }
        return ingrediente;
    }
    
    public void actualizarStockEnConexion(Connection conn, int idIngrediente, int nuevoStock) throws SQLException {
        String SQL = "UPDATE Ingredientes SET cantidad_stock = ? WHERE id_ingrediente = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, nuevoStock);
            pstmt.setInt(2, idIngrediente);
            pstmt.executeUpdate();
        }
    }

    public boolean guardarOActualizar(Ingrediente ingrediente) throws SQLException {
        if (ingrediente.getId() == 0) {
            return guardarNuevo(ingrediente);
        } else {
            return actualizarStockYNombre(ingrediente);
        }
    }
    
    private boolean guardarNuevo(Ingrediente ingrediente) throws SQLException {
        String SQL = "INSERT INTO Ingredientes (nombre, cantidad_stock) VALUES (?, ?)";
        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, ingrediente.getNombre());
            pstmt.setInt(2, ingrediente.getCantidadStock());
            return pstmt.executeUpdate() > 0;
        }
    }
    
    private boolean actualizarStockYNombre(Ingrediente ingrediente) throws SQLException {
        String SQL = "UPDATE Ingredientes SET nombre = ?, cantidad_stock = ? WHERE id_ingrediente = ?";
        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, ingrediente.getNombre());
            pstmt.setInt(2, ingrediente.getCantidadStock());
            pstmt.setInt(3, ingrediente.getId());
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean eliminar(int id) throws SQLException {
        String SQL = "DELETE FROM Ingredientes WHERE id_ingrediente = ?";
        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
}