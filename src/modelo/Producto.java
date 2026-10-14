package modelo;

/**
 * Representa un Producto en el catálogo del sistema.
 * Contiene la información técnica, comercial y de control de stock mínimo requerida para la gestión comercial.
 */
public class Producto {
    
    private int id;
    private String sku;
    private String nombre;
    private String tamano;
    private double precio;
    private int stockMinimo;
    private String unidadMedida;

    /**
     * Constructor para la creación e instanciación de un Producto con sus atributos obligatorios.
     * 
     * @param sku Código único de stock (Stock Keeping Unit).
     * @param nombre Nombre o descripción del producto.
     * @param precio Precio de venta unitario.
     * @param stockMinimo Cantidad mínima en stock antes de generar alertas de reabastecimiento.
     * @param unidadMedida Unidad de medida (ej. "Unidad", "Kg", "Lts").
     */
    public Producto(String sku, String nombre, double precio, int stockMinimo, String unidadMedida) {
        this.sku = sku;
        this.nombre = nombre;
        this.precio = precio;
        this.stockMinimo = stockMinimo;
        this.unidadMedida = unidadMedida;
    }
    
    /**
     * Constructor vacío por defecto.
     */
    public Producto() {}

    // Métodos Getters y Setters
    public int getId() { return id; }
    public String getSku() { return sku; }
    public String getNombre() { return nombre; }
    public String getTamano() { return (tamano != null) ? tamano : "N/A"; }
    public double getPrecio() { return precio; }
    public int getStockMinimo() { return stockMinimo; }
    public String getUnidadMedida() { return unidadMedida; }

    public void setId(int id) { this.id = id; }
    public void setSku(String sku) { this.sku = sku; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTamano(String tamano) { this.tamano = tamano; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    @Override
    public String toString() {
        return String.format("%s - $%.2f", nombre, precio);
    }
}