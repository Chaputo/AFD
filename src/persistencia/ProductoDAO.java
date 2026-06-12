package persistencia;

import modelo.Producto; 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> listar() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String SQL = "SELECT sku, nombre, precio_venta, stockMinimo, unidadMedida FROM producto ORDER BY nombre"; 
        
        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } 
        return productos;
    }

    public Producto obtenerPorSku(String sku) throws SQLException {
        String SQL = "SELECT * FROM producto WHERE sku = ?";
        Producto producto = null;
        
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            
            pstmt.setString(1, sku);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    producto = mapearProducto(rs);
                }
            }
        }
        return producto;
    }

    public Producto buscarPorSku(String sku) throws SQLException {
        String SQL = "SELECT * FROM producto WHERE sku = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            
            pstmt.setString(1, sku);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        }
        return null;
    }

    public boolean guardar(Producto producto) throws SQLException {
        String SQL;
        // Cambiamos la lógica: verificamos si el objeto Producto existe por su SKU
        boolean existe = (obtenerPorSku(producto.getSku()) != null);
        
        if (!existe) {
            SQL = "INSERT INTO producto (sku, nombre, precio_venta, stockMinimo, unidadMedida) VALUES (?, ?, ?, ?, ?)";
        } else {
            SQL = "UPDATE producto SET nombre = ?, precio_venta = ?, stockMinimo = ?, unidadMedida = ? WHERE sku = ?";
        }
        
        try (Connection conn = ConexionDB.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            
            if (!existe) {
                pstmt.setString(1, producto.getSku());
                pstmt.setString(2, producto.getNombre());
                pstmt.setDouble(3, producto.getPrecio());
                pstmt.setInt(4, producto.getStockMinimo());
                pstmt.setString(5, producto.getUnidadMedida());
            } else {
                pstmt.setString(1, producto.getNombre());
                pstmt.setDouble(2, producto.getPrecio());
                pstmt.setInt(3, producto.getStockMinimo());
                pstmt.setString(4, producto.getUnidadMedida());
                pstmt.setString(5, producto.getSku());
            }
            return pstmt.executeUpdate() > 0;
        }
    }

    public int obtenerIdPorSku(String sku) throws SQLException {
        String SQL = "SELECT * FROM producto WHERE sku = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, sku);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Intentamos obtener la primera columna sea cual sea su nombre
                    return rs.getInt(1); 
                }
            }
        }
        return 0;
    }

    public List<Producto> listarTodos() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearProducto(rs));
            }
        }
        return lista;
    }

    public List<Producto> buscarPorNombre(String nombre) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE nombre LIKE ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + nombre + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
        }
        return lista;
    }

    public boolean eliminar(String sku) throws SQLException {
        String SQL = "DELETE FROM producto WHERE sku = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, sku);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Método auxiliar para no repetir código de mapeo
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        // Como tu PK es el SKU, lo usamos como identificador
        p.setSku(rs.getString("sku"));
        p.setNombre(rs.getString("nombre"));
        p.setPrecio(rs.getDouble("precio_venta"));
        p.setStockMinimo(rs.getInt("stockMinimo"));
        p.setUnidadMedida(rs.getString("unidadMedida"));
        return p;
    }

    
}