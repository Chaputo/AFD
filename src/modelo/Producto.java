package modelo;

public class Producto {
    
    private int id; // Agregado: Necesario para relacionar con pedido_detalle e Inventario
    private String sku;
    private String nombre;
    private String tamano; // Agregado: El panel lo pide para la tabla del carrito
    private double precio;
    private int stockMinimo;
    private String unidadMedida;

    // Constructor completo actualizado
    public Producto(String sku, String nombre, double precio, int stockMinimo, String unidadMedida) {
        this.sku = sku;
        this.nombre = nombre;
        this.precio = precio;
        this.stockMinimo = stockMinimo;
        this.unidadMedida = unidadMedida;
    }
    
    public Producto() {}
    
    // --- Getters corregidos para eliminar errores del Panel ---
    public int getId() { return id; } // Elimina error en image_ca566a.png
    public String getSku() { return sku; }
    public String getNombre() { return nombre; }
    public String getTamano() { return (tamano != null) ? tamano : "N/A"; } // Elimina error de "getTamano"
    public double getPrecio() { return precio; }
    public int getStockMinimo() { return stockMinimo; }
    public String getUnidadMedida() { return unidadMedida; }
    
    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setSku(String sku) { this.sku = sku; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTamano(String tamano) { this.tamano = tamano; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    @Override
    public String toString() {
        // Mejorado para que en el ComboBox se vea limpio el nombre y precio
        return String.format("%s - $%.2f", nombre, precio);
    }
}