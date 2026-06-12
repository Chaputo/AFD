package persistencia;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    
    private static final String DB_NAME = "gestioncomercialdb"; 
    private static final String URL = "jdbc:mysql://localhost:3306/" + DB_NAME; 
    
    private static final String USUARIO = "root"; 
    private static final String CLAVE = "";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver"; 

    public static Connection getConnection() {
        try {
            Class.forName(DRIVER);
            System.out.println("Intentando conectar a la DB...");
            
            Connection conn = DriverManager.getConnection(URL, USUARIO, CLAVE);
            conn.setAutoCommit(true); 

            System.out.println("Conexión exitosa a " + DB_NAME + "!");
            return conn;
            
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver JDBC no encontrado. Verifica el JAR.");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("Error de conexión SQL. ¿El servidor (MySQL) está iniciado y '" + DB_NAME + "' existe?");
            e.printStackTrace();
            return null;
        }
    }
}