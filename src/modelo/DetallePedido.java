package modelo;

/**
 * Representa el detalle o renglón individual dentro de un pedido (Línea de Pedido).
 * Mantiene la relación entre la orden principal, el producto solicitado (SKU), 
 * el precio asignado y la cantidad vendida.
 */
public class DetallePedido {
    private int id;
    private int idPedido;
    private String sku;
    private double precioUnitario;
    private int cantidad;
    
    /**
     * Constructor vacío por defecto.
     */
    public DetallePedido() {}

    /**
     * Constructor completo para instanciar un detalle de pedido existente.
     * 
     * @param id Identificador único de la línea de detalle.
     * @param idPedido Identificador del pedido cabecera al que pertenece.
     * @param sku Código SKU del producto asociado.
     * @param precioUnitario Precio del producto al momento de la venta.
     * @param cantidad Cantidad de unidades solicitadas.
     */
    public DetallePedido(int id, int idPedido, String sku, double precioUnitario, int cantidad) {
        this.id = id;
        this.idPedido = idPedido;
        this.sku = sku;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    // Métodos Getters y Setters
    public int getId() { return id; }
    public int getIdPedido() { return idPedido; }
    public String getSku() { return sku; } 
    public double getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }

    public void setId(int id) { this.id = id; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }
    public void setSku(String sku) { this.sku = sku; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}