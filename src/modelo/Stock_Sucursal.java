package modelo;

import java.time.LocalDateTime;

public class Stock_Sucursal {
    private int idStock;
    private String skuProducto;
    private int idSucursal;
    private int cantidad;
    private LocalDateTime ultimaActualizacion;

    public Stock_Sucursal() {}

    public Stock_Sucursal(int idStock, String skuProducto, int idSucursal, int cantidad, LocalDateTime ultimaActualizacion) {
        this.idStock = idStock;
        this.skuProducto = skuProducto;
        this.idSucursal = idSucursal;
        this.cantidad = cantidad;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public int getIdStock() { return idStock; }
    public void setIdStock(int idStock) { this.idStock = idStock; }

    public String getSkuProducto() { return skuProducto; }
    public void setSkuProducto(String skuProducto) { this.skuProducto = skuProducto; }

    public int getIdSucursal() { return idSucursal; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public LocalDateTime getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }
}