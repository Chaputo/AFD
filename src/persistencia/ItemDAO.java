package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemDAO {

    public int obtenerIdPorSku(String identificador) throws SQLException {
        String SQL = "SELECT id_item FROM Items WHERE nombre = ?"; 
        int idItem = 0;

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            
            pstmt.setString(1, identificador);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idItem = rs.getInt("id_item");
                }
            }
        }
        return idItem;
    }
    
}