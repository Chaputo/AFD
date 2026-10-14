package modelo;

import java.time.LocalDateTime;

/**
 * Representa un registro de movimiento de stock en el sistema.
 * Registra entradas, salidas y ajustes de productos para un control preciso de inventario por sucursal.
 */
public class MovimientoStock {
    private int id;
    private LocalDateTime fecha;
    private String tipo;
    private int cantidad;
    private String skuProducto;
    private Integer idOcRelacionada;
    private int idSucursal;

    /**
     * Constructor vacío por defecto.
     */
    public MovimientoStock() {
    }

    /**
     * Constructor completo para instanciar movimientos leídos desde la base de datos.
     * 
     * @param id Identificador único del movimiento.
     * @param fecha Marca temporal (fecha y hora) de la transacción.
     * @param tipo Tipo de movimiento (ej. "ENTRADA", "SALIDA", "AJUSTE").
     * @param cantidad Cantidad de unidades involucradas.
     * @param skuProducto Código SKU del producto.
     * @param idOcRelacionada Identificador de la Orden de Compra asociada (puede ser null).
     * @param idSucursal Identificador de la sucursal donde ocurrió el movimiento.
     */
    public MovimientoStock(int id, LocalDateTime fecha, String tipo, int cantidad, String skuProducto, Integer idOcRelacionada, int idSucursal) {
       this.id = id;
       this.fecha = fecha;
       this.tipo = tipo;
       this.cantidad = cantidad;
       this.skuProducto = skuProducto;
       this.idOcRelacionada = idOcRelacionada;
       this.idSucursal = idSucursal;
    }

    /**
     * Constructor para registrar un nuevo movimiento de stock sin ID asignado.
     * 
     * @param fecha Marca temporal de la transacción.
     * @param tipo Tipo de movimiento.
     * @param cantidad Cantidad de unidades.
     * @param skuProducto Código SKU del producto.
     * @param idOcRelacionada Identificador de la Orden de Compra (puede ser null).
     * @param idSucursal Identificador de la sucursal asignada.
     */
    public MovimientoStock(LocalDateTime fecha, String tipo, int cantidad, String skuProducto, Integer idOcRelacionada, int idSucursal) {
       this.fecha = fecha;
       this.tipo = tipo;
       this.cantidad = cantidad;
       this.skuProducto = skuProducto;
       this.idOcRelacionada = idOcRelacionada;
       this.idSucursal = idSucursal;
    }

    // Métodos Getters y Setters
    public int getId() { return this.id; }
    public LocalDateTime getFecha() { return this.fecha; }
    public String getTipo() { return this.tipo; }
    public int getCantidad() { return this.cantidad; }
    public String getSkuProducto() { return this.skuProducto; }
    public Integer getIdOcRelacionada() { return this.idOcRelacionada; }
    public int getIdSucursal() { return this.idSucursal; }

    public void setId(int id) { this.id = id; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setSkuProducto(String skuProducto) { this.skuProducto = skuProducto; }
    public void setIdOcRelacionada(Integer idOcRelacionada) { this.idOcRelacionada = idOcRelacionada; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }
}