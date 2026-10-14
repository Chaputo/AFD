package modelo;

import java.time.LocalDateTime;

/**
 * Objeto de Transferencia de Datos para la visualización detallada del stock por sucursal.
 * Consolida información del producto y de la sucursal con descripciones legibles para su presentación en las vistas.
 */
public class Stock_SucursalDTO {
    private int id;
    private String skuProducto;
    private String nombreProducto;
    private int idSucursal;
    private String nombreSucursal;
    private int cantidadDisponible;
    private LocalDateTime fechaUltimoMovimiento;
    private int stockMinimo;

    /**
     * Constructor vacío por defecto.
     */
    public Stock_SucursalDTO() {}

    /**
     * Constructor completo para instanciar el DTO a partir de consultas proyectadas (JOINs) en el DAO.
     * 
     * @param id Identificador único del registro de stock.
     * @param skuProducto Código SKU del producto.
     * @param nombreProducto Nombre comercial o descripción del producto.
     * @param idSucursal Identificador de la sucursal.
     * @param nombreSucursal Nombre o denominación de la sucursal.
     * @param cantidadDisponible Cantidad actual disponible en inventario.
     * @param fechaUltimoMovimiento Marca temporal del último movimiento registrado.
     * @param stockMinimo Umbral de stock mínimo definido para el producto.
     */
    public Stock_SucursalDTO(int id, String skuProducto, String nombreProducto, 
                            int idSucursal, String nombreSucursal, int cantidadDisponible, 
                            LocalDateTime fechaUltimoMovimiento, int stockMinimo) {
        this.id = id;
        this.skuProducto = skuProducto;
        this.nombreProducto = nombreProducto;
        this.idSucursal = idSucursal;
        this.nombreSucursal = nombreSucursal;
        this.cantidadDisponible = cantidadDisponible;
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
        this.stockMinimo = stockMinimo;
    }

    // Métodos Getters y Setters
    public int getId() { return id; }
    public String getSkuProducto() { return skuProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public int getIdSucursal() { return idSucursal; }
    public String getNombreSucursal() { return nombreSucursal; }
    public int getCantidadDisponible() { return cantidadDisponible; }
    public LocalDateTime getFechaUltimoMovimiento() { return fechaUltimoMovimiento; }
    public int getStockMinimo() { return stockMinimo; }

    public void setId(int id) { this.id = id; }
    public void setSkuProducto(String skuProducto) { this.skuProducto = skuProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }
    public void setNombreSucursal(String nombreSucursal) { this.nombreSucursal = nombreSucursal; }
    public void setCantidadDisponible(int cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
    public void setFechaUltimoMovimiento(LocalDateTime fechaUltimoMovimiento) { this.fechaUltimoMovimiento = fechaUltimoMovimiento; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
}