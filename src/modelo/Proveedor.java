package modelo;

/**
 * Representa la entidad Proveedor dentro del sistema.
 * Almacena los datos fiscales y de contacto de las empresas o entidades que suministran productos o insumos.
 */
public class Proveedor {
    private int id;
    private String nombre;
    private String cuit;
    private String contacto;

    /**
     * Constructor completo para instanciar proveedores recuperados de la base de datos.
     * 
     * @param id Identificador único del proveedor.
     * @param nombre Nombre comercial o razón social.
     * @param cuit Clave Única de Identificación Tributaria (CUIT/RUT).
     * @param contacto Información de contacto (vendedor, teléfono o correo).
     */
    public Proveedor(int id, String nombre, String cuit, String contacto) {
        this.id = id;
        this.nombre = nombre;
        this.cuit = cuit;
        this.contacto = contacto;
    }

    /**
     * Constructor para la creación de un nuevo proveedor antes de ser registrado en la base de datos.
     * 
     * @param nombre Nombre comercial o razón social.
     * @param cuit Clave Única de Identificación Tributaria.
     * @param contacto Información de contacto.
     */
    public Proveedor(String nombre, String cuit, String contacto) {
        this.nombre = nombre;
        this.cuit = cuit;
        this.contacto = contacto;
    }

    // Métodos Getters y Setters
    public int getIdProveedor() { 
        return this.id; 
    }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCuit() { return cuit; }
    public String getContacto() { return contacto; }

    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCuit(String cuit) { this.cuit = cuit; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    @Override
    public String toString() {
        return nombre;
    }
}