package modelo;

import java.time.LocalDateTime;

/**
 * Representa las existencias físicas de un producto dentro de una sucursal específica.
 * Permite la gestión distribuida de inventario y mantiene la auditoría del último movimiento realizado.
 */
public class Stock_Sucursal {
    private int idStock;
    private String skuProducto;
    private int idSucursal;
    private int cantidad;
    private LocalDateTime ultimaActualizacion;

    /**
     * Constructor vacío por defecto.
     */
    public Stock_Sucursal() {}

    /**
     * Constructor completo para instanciar registros de stock por sucursal recuperados de la base de datos.
     * 
     * @param idStock Identificador único del registro de stock.
     * @param skuProducto Código SKU del producto asociado.
     * @param idSucursal Identificador de la sucursal asignada.
     * @param cantidad Cantidad disponible en existencias.
     * @param ultimaActualizacion Marca temporal de la última modificación en el stock.
     */
    public Stock_Sucursal(int idStock, String skuProducto, int idSucursal, int cantidad, LocalDateTime ultimaActualizacion) {
        this.idStock = idStock;
        this.skuProducto = skuProducto;
        this.idSucursal = idSucursal;
        this.cantidad = cantidad;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    // Métodos Getters y Setters
    public int getIdStock() { return idStock; }
    public String getSkuProducto() { return skuProducto; }
    public int getIdSucursal() { return idSucursal; }
    public int getCantidad() { return cantidad; }
    public LocalDateTime getUltimaActualizacion() { return ultimaActualizacion; }

    public void setIdStock(int idStock) { this.idStock = idStock; }
    public void setSkuProducto(String skuProducto) { this.skuProducto = skuProducto; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }
}