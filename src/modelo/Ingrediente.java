package modelo;

/**
 * Representa un ingrediente o materia prima en el inventario.
 * Contiene la información básica del insumo y su cantidad disponible en stock.
 */
public class Ingrediente {
    private int id;
    private String nombre;
    private int cantidadStock;

    /**
     * Constructor principal para instanciar un ingrediente.
     * 
     * @param id Identificador único del ingrediente en la base de datos.
     * @param nombre Nombre comercial o descriptivo del insumo.
     * @param cantidadStock Unidades disponibles en stock.
     */
    public Ingrediente(int id, String nombre, int cantidadStock) {
        this.id = id;
        this.nombre = nombre;
        this.cantidadStock = cantidadStock;
    }

    // Métodos Getters y Setters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getCantidadStock() { return cantidadStock; }
    
    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCantidadStock(int cantidadStock) { this.cantidadStock = cantidadStock; }

    /**
     * Devuelve una representación legible del ingrediente con su stock actual.
     * Formato utilizado principalmente en vistas e interfaces de usuario.
     */
    @Override
    public String toString() {
        return nombre + " (Stock: " + cantidadStock + ")"; 
    }
}