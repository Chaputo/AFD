package modelo;

//Definición de la Clase y Atributos

public class Proveedor {
    private int id;
    private String nombre;
    private String cuit;
    private String contacto;

    //Definición de los constructores

    public Proveedor(int id, String nombre, String cuit, String contacto) {
        this.id = id;
        this.nombre = nombre;
        this.cuit = cuit;
        this.contacto = contacto;
    }

    public Proveedor(String nombre, String cuit, String contacto) {
        this.nombre = nombre;
        this.cuit = cuit;
        this.contacto = contacto;
    }

    // Metodos Getters y Setters

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