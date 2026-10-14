package persistencia;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionDB {

    // Credenciales: se leen de db.properties (si existe) y si no se usan estos valores por defecto

    private static final String ARCHIVO_CONFIG = "db.properties";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final Properties CONFIG = cargarConfiguracion();

    private static final String HOST = CONFIG.getProperty("db.host", "localhost");
    private static final String PUERTO = CONFIG.getProperty("db.puerto", "3306");
    private static final String DB_NAME = CONFIG.getProperty("db.nombre", "gestioncomercialdb");
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + DB_NAME;
    private static final String USUARIO = CONFIG.getProperty("db.usuario", "root");
    private static final String CLAVE = CONFIG.getProperty("db.clave", "");

    // Método principal

    // Nunca devuelve null: si no puede conectar lanza SQLException con un mensaje claro
    public static Connection getConnection() throws SQLException {

        // Proceso de conexión

        try {
            // Cargar el driver JDBC
            Class.forName(DRIVER);
            // Abrir línea de conexión
            Connection conn = DriverManager.getConnection(URL, USUARIO, CLAVE);
            // Configurar la conexión
            conn.setAutoCommit(true);
            return conn;

        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC no encontrado. Verifica el JAR de mysql-connector-j.", e);
        } catch (SQLException e) {
            System.err.println("Error de conexión SQL: " + e.getMessage());
            throw new SQLException("No se pudo conectar a la base de datos '" + DB_NAME + "' en " + HOST + ":" + PUERTO
                    + ".\n¿Está iniciado el servidor MySQL/MariaDB?", e);
        }
    }

    // Busca db.properties en la carpeta actual y después junto al .jar de la aplicación

    private static Properties cargarConfiguracion() {
        Properties props = new Properties();
        for (Path ruta : rutasPosibles()) {
            if (Files.isRegularFile(ruta)) {
                try (InputStream in = Files.newInputStream(ruta)) {
                    props.load(in);
                    System.out.println("Configuración de DB leída de " + ruta.toAbsolutePath());
                    return props;
                } catch (IOException e) {
                    System.err.println("No se pudo leer " + ruta + ": " + e.getMessage());
                }
            }
        }
        System.out.println("No se encontró " + ARCHIVO_CONFIG + ", se usan los valores por defecto.");
        return props;
    }

    private static Path[] rutasPosibles() {
        Path actual = Paths.get(ARCHIVO_CONFIG);
        try {
            Path jar = Paths.get(ConexionDB.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path carpetaJar = Files.isDirectory(jar) ? jar : jar.getParent();
            return new Path[] { actual, carpetaJar.resolve(ARCHIVO_CONFIG) };
        } catch (Exception e) {
            return new Path[] { actual };
        }
    }
}
