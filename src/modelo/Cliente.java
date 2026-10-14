package modelo;

/**
 * Representa un Cliente dentro del sistema.
 * Modela la información personal y de contacto necesaria para asociar a los pedidos.
 */
public class Cliente {
    private int id;
    private String nombre;
    private String telefono;
    private String direccion;

    /**
     * Constructor utilizado para instanciar clientes leídos de la base de datos.
     * 
     * @param id Identifier único del cliente.
     * @param nombre Nombre completo del cliente.
     * @param telefono Teléfono de contacto.
     * @param direccion Dirección de entrega o residencia.
     */
    public Cliente(int id, String nombre, String telefono, String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    /**
     * Constructor utilizado para la creación de un nuevo cliente antes de insertarlo en la base de datos.
     * 
     * @param nombre Nombre completo del cliente.
     * @param telefono Teléfono de contacto.
     * @param direccion Dirección de entrega o residencia.
     */
    public Cliente(String nombre, String telefono, String direccion) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    // Metodos Getters y Setters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setDireccion(String direccion) { this.direccion = direccion; } 
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return "Cliente [id=" + id + ", nombre=" + nombre + ", telefono=" + telefono + ", direccion=" + direccion + "]";
    }
}