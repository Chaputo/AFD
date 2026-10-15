package modelo;

/**
 * Representa una línea de detalle dentro de una Orden de Compra (OC).
 * Modela el producto o ítem solicitado a un proveedor, especificando cantidad, 
 * precio unitario pactado y el subtotal calculado.
 */
public class LineaOC {
    private int id;
    private int idOc;
    private String skuProducto; 
    private int cantidad;
    private double precioCompraUnitario;
    private double subtotal;
    private String nombreProducto;
    private int idProveedor;          // cada línea puede ser de un proveedor distinto
    private String nombreProveedor;

    /**
     * Constructor completo para instancias recuperadas desde la base de datos.
     * 
     * @param id Identificador único de la línea de la orden de compra.
     * @param idOc Identificador de la orden de compra cabecera.
     * @param skuProducto Código SKU del producto solicitado.
     * @param cantidad Cantidad de unidades a comprar.
     * @param precioCompraUnitario Precio de compra por unidad.
     * @param subtotal Importe total de esta línea (cantidad * precio unitario).
     */
    public LineaOC(int id, int idOc, String skuProducto, int cantidad, double precioCompraUnitario, double subtotal) {
        this.id = id;
        this.idOc = idOc;
        this.skuProducto = skuProducto;
        this.cantidad = cantidad;
        this.precioCompraUnitario = precioCompraUnitario;
        this.subtotal = subtotal;
    }

    /**
     * Constructor para la creación de una línea de OC mediante identificador SKU.
     * Calcula de forma automática el subtotal.
     * 
     * @param skuProducto Código SKU del producto.
     * @param cantidad Cantidad de unidades a solicitar.
     * @param precioCompraUnitario Precio unitario pactado.
     */
    public LineaOC(String skuProducto, int cantidad, double precioCompraUnitario) {
        this.skuProducto = skuProducto;
        this.cantidad = cantidad;
        this.precioCompraUnitario = precioCompraUnitario;
        this.subtotal = cantidad * precioCompraUnitario;
    }

    /**
     * Constructor alternativo para la creación de una línea de OC que incluye la descripción del producto.
     * Calcula de forma automática el subtotal.
     * 
     * @param skuProducto Código SKU del producto.
     * @param nombreProducto Nombre o descripción del producto.
     * @param cantidad Cantidad de unidades a solicitar.
     * @param precioCompraUnitario Precio unitario pactado.
     */
    public LineaOC(String skuProducto, String nombreProducto, int cantidad, double precioCompraUnitario) {
        this.skuProducto = skuProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioCompraUnitario = precioCompraUnitario;
        this.subtotal = cantidad * precioCompraUnitario; 
    }

    // Métodos Getters y Setters
    public int getId() { return id; }
    public int getIdOc() { return idOc; }
    public String getSkuProducto() { return skuProducto; }
    public double getPrecioCompraUnitario() { return precioCompraUnitario; }
    public double getSubtotal() { return subtotal; }
    public String getNombreProducto() { return nombreProducto; }
    public int getCantidad() { return cantidad; } 
    
    public void setId(int id) { this.id = id; }
    public void setIdOc(int idOc) { this.idOc = idOc; }
    public void setSkuProducto(String skuProducto) { this.skuProducto = skuProducto; }
    public void setPrecioCompraUnitario(double precioCompraUnitario) { this.precioCompraUnitario = precioCompraUnitario; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setCantidadPedida(int cantidad) { this.cantidad = cantidad; }
    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }
    public String getNombreProveedor() { return nombreProveedor; }
    public void setNombreProveedor(String nombreProveedor) { this.nombreProveedor = nombreProveedor; }
}                                                                                  