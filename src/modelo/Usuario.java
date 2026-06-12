package modelo;

public class Usuario {
    
    private int id;
    private String nombre;
    private String usuario;
    private String password;
    private String rol;

    public Usuario(int id, String nombre, String usuario, String password, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.usuario = usuario;
        this.password = password;
        this.rol = rol;
    }
    
    public Usuario(int id, String nombre, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.rol = rol;
        this.usuario = null;
        this.password = null;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUsuario() { return usuario; }
    public String getPassword() { return password; }
    public String getRol() { return rol; }
    
    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public void setUsuario(String usuario) { this.usuario = usuario; } 
    
    public void setPassword(String password) { this.password = password; } 
    
    public void setRol(String rol) { this.rol = rol; }
}