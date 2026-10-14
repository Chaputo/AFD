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
    private static final String ARCHIVO_CONFIG_LOCAL = "db.local.properties";
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

    /**
     * Si la base no responde y db.local.properties indica un servidor local (db.servidor.exe),
     * lo inicia y espera a que acepte conexiones. Pensado para el MariaDB portable de la demo.
     */
    public static void iniciarServidorSiHaceFalta() {
        String exe = CONFIG.getProperty("db.servidor.exe", "").trim();
        if (exe.isEmpty() || servidorResponde()) return;
        try {
            java.util.List<String> comando = new java.util.ArrayList<>();
            comando.add(exe);
            String config = CONFIG.getProperty("db.servidor.config", "").trim();
            if (!config.isEmpty()) comando.add("--defaults-file=" + config);
            new ProcessBuilder(comando)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
            System.out.println("Iniciando servidor de base de datos: " + exe);
            for (int i = 0; i < 40 && !servidorResponde(); i++) {
                Thread.sleep(500);
            }
        } catch (IOException e) {
            System.err.println("No se pudo iniciar el servidor de base de datos: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean servidorResponde() {
        try (java.net.Socket s = new java.net.Socket()) {
            s.connect(new java.net.InetSocketAddress(HOST, Integer.parseInt(PUERTO)), 500);
            return true;
        } catch (IOException | NumberFormatException e) {
            return false;
        }
    }

    // Lee db.properties de la carpeta actual o, si no está, de la carpeta del .jar.
    // En esa misma carpeta, db.local.properties (opcional, no se sube al repo) pisa esos valores.

    private static Properties cargarConfiguracion() {
        Properties props = new Properties();
        for (Path carpeta : carpetasPosibles()) {
            Path ruta = carpeta.resolve(ARCHIVO_CONFIG);
            if (Files.isRegularFile(ruta)) {
                leer(props, ruta);
                Path local = carpeta.resolve(ARCHIVO_CONFIG_LOCAL);
                if (Files.isRegularFile(local)) leer(props, local);
                return props;
            }
        }
        System.out.println("No se encontró " + ARCHIVO_CONFIG + ", se usan los valores por defecto.");
        return props;
    }

    private static void leer(Properties props, Path ruta) {
        try (InputStream in = Files.newInputStream(ruta)) {
            props.load(in);
            System.out.println("Configuración de DB leída de " + ruta.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("No se pudo leer " + ruta + ": " + e.getMessage());
        }
    }

    private static Path[] carpetasPosibles() {
        Path actual = Paths.get("");
        try {
            Path jar = Paths.get(ConexionDB.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path carpetaJar = Files.isDirectory(jar) ? jar : jar.getParent();
            return new Path[] { actual, carpetaJar };
        } catch (Exception e) {
            return new Path[] { actual };
        }
    }
}
