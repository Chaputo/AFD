package modelo;

//Definición de la Clase y Atributos

public class DetallePedido {
    private int id;
    private int idPedido;
    private String sku;
    private double precioUnitario;
    private int cantidad;
    
    //Definición de los constructores
    
    public DetallePedido() {}
    public DetallePedido(int id, int idPedido, String sku, double precioUnitario, int cantidad) {
        this.id = id;
        this.idPedido = idPedido;
        this.sku = sku;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    // Metodos Getters y Setters

    public int getId() { return id; }
    public int getIdPedido() { return idPedido; }
    public String getSku() { return sku; } // Este es el que pedía PedidoDAO
    public double getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }
    public void setId(int id) { this.id = id; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }
    public void setSku(String sku) { this.sku = sku; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}