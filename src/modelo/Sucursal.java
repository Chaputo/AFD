package modelo;

/**
 * Representa una Sucursal o punto de venta/depósito dentro del sistema.
 * Modela la ubicación física utilizada para la gestión descentralizada de inventario y pedidos.
 */
public class Sucursal {

    private int id;
    private String nombre;
    private String direccion;

    /**
     * Constructor para la creación de una nueva sucursal antes de ser registrada en la base de datos.
     * 
     * @param nombre Nombre o denominación de la sucursal.
     * @param direccion Dirección física o ubicación.
     */
    public Sucursal(String nombre, String direccion) {
        this.nombre = nombre;
        this.direccion = direccion;
    }

    /**
     * Constructor completo para instanciar sucursales recuperadas desde la base de datos.
     * 
     * @param id Identificador único de la sucursal.
     * @param nombre Nombre o denominación de la sucursal.
     * @param direccion Dirección física o ubicación.
     */
    public Sucursal(int id, String nombre, String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
    }

    // Métodos Getters y Setters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    
    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    /**
     * Devuelve el nombre de la sucursal.
     * Permite la renderización directa en componentes de interfaz como JComboBox.
     */
    @Override
    public String toString() {
        return nombre;
    }
}