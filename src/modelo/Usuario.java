package modelo;

/**
 * Representa a los usuarios del sistema, incluyendo sus credenciales de acceso 
 * y su rol para la gestión de permisos (RBAC).
 */
public class Usuario {
    
    private int id;
    private String nombre;
    private String usuario;
    private String password;
    private String rol;

    /**
     * Constructor completo para autenticación y gestión de usuarios.
     * 
     * @param id Identificador único del usuario.
     * @param nombre Nombre completo o legibles del usuario.
     * @param usuario Nombre de usuario / credencial de ingreso.
     * @param password Contraseña de acceso.
     * @param rol Rol asignado en el sistema (ej. "Gerente", "Vendedor").
     */
    public Usuario(int id, String nombre, String usuario, String password, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.usuario = usuario;
        this.password = password;
        this.rol = rol;
    }
    
    /**
     * Constructor seguro sin credenciales. 
     * Recomendado para cargar la información de sesión o listados donde no se deben exponer contraseñas.
     * 
     * @param id Identificador único del usuario.
     * @param nombre Nombre completo del usuario.
     * @param rol Rol asignado en el sistema.
     */
    public Usuario(int id, String nombre, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.rol = rol;
        this.usuario = null;
        this.password = null;
    }

    // Métodos Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; } 

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; } 

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}