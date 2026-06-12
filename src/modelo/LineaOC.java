package modelo;

public class LineaOC {
    private int id;
    private int idOc;
    private String skuProducto; 
    private int cantidad;
    private double precioCompraUnitario;
    private double subtotal;
    
    private String nombreProducto;

    public LineaOC(int id, int idOc, String skuProducto, int cantidad, double precioCompraUnitario, double subtotal) {
        this.id = id;
        this.idOc = idOc;
        this.skuProducto = skuProducto;
        this.cantidad = cantidad;
        this.precioCompraUnitario = precioCompraUnitario;
        this.subtotal = subtotal;
    }

    public LineaOC(String skuProducto, int cantidad, double precioCompraUnitario) {
        this.skuProducto = skuProducto;
        this.cantidad = cantidad;
        this.precioCompraUnitario = precioCompraUnitario;
        this.subtotal = cantidad * precioCompraUnitario;
    }

    public LineaOC(String skuProducto, String nombreProducto, int cantidad, double precioCompraUnitario) {
        this.skuProducto = skuProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioCompraUnitario = precioCompraUnitario;
        this.subtotal = cantidad * precioCompraUnitario; 
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdOc() { return idOc; }
    public void setIdOc(int idOc) { this.idOc = idOc; }

    public String getSkuProducto() { return skuProducto; }
    public void setSkuProducto(String skuProducto) { this.skuProducto = skuProducto; }

    public double getPrecioCompraUnitario() { return precioCompraUnitario; }
    public void setPrecioCompraUnitario(double precioCompraUnitario) { this.precioCompraUnitario = precioCompraUnitario; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidadPedida() { 
        return cantidad; 
    }
    public void setCantidadPedida(int cantidad) { 
        this.cantidad = cantidad;
    }

    public int getCantidad() { return cantidad; } 
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}