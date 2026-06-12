package modelo;

public class DetallePedido {
    private int id;
    private int idPedido;
    private String sku; // Antes idPizza (int), ahora sku (String) para SISTEMA AFD
    private double precioUnitario;
    private int cantidad;
    
    // Constructor actualizado
    public DetallePedido(int id, int idPedido, String sku, double precioUnitario, int cantidad) {
        this.id = id;
        this.idPedido = idPedido;
        this.sku = sku;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    // Constructor vacío (útil para frameworks o inicializaciones rápidas)
    public DetallePedido() {}

    // Getters
    public int getId() { return id; }
    public int getIdPedido() { return idPedido; }
    public String getSku() { return sku; } // Este es el que pedía PedidoDAO
    public double getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }
    public void setSku(String sku) { this.sku = sku; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}