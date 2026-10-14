package modelo;

import java.time.LocalDateTime;
import java.util.List; 

/**
 * Representa la cabecera de una Orden de Compra (OC) en el sistema.
 * Agrupa la información del proveedor, usuario emisor, montos totales,
 * estado transaccional y el detalle de ítems solicitados (líneas de OC).
 */
public class OrdenDeCompra {
    private int id;
    private int idProveedor; 
    private int idUsuario;
    private LocalDateTime fechaEmision;
    private double total;
    private String estado; 
    private List<LineaOC> lineasOC; 
    private String nombreProveedor;
    private String nombreUsuario;

    /**
     * Constructor vacío por defecto.
     */
    public OrdenDeCompra() {
    }
    
    /**
     * Constructor para instanciar órdenes existentes recuperadas de la base de datos sin el detalle de líneas.
     * 
     * @param id Identificador único de la orden de compra.
     * @param idProveedor Identificador del proveedor asignado.
     * @param idUsuario Identificador del usuario que emitió la orden.
     * @param fechaEmision Fecha y hora de emisión.
     * @param total Monto total de la orden.
     * @param estado Estado actual de la orden (ej. "PENDIENTE", "RECIBIDA").
     */
    public OrdenDeCompra(int id, int idProveedor, int idUsuario, LocalDateTime fechaEmision, double total, String estado) {
        this.id = id;
        this.idProveedor = idProveedor;
        this.idUsuario = idUsuario;
        this.fechaEmision = fechaEmision;
        this.total = total;
        this.estado = estado;
    }
    
    /**
     * Constructor completo para instanciar órdenes existentes con su lista de detalle.
     * 
     * @param id Identificador único de la orden de compra.
     * @param idProveedor Identificador del proveedor asignado.
     * @param idUsuario Identificador del usuario emisor.
     * @param fechaEmision Fecha y hora de emisión.
     * @param total Monto total de la orden.
     * @param estado Estado actual de la orden.
     * @param lineasOC Lista de líneas de detalle (items) asociadas.
     */
    public OrdenDeCompra(int id, int idProveedor, int idUsuario, LocalDateTime fechaEmision, double total, String estado, List<LineaOC> lineasOC) {
        this.id = id;
        this.idProveedor = idProveedor;
        this.idUsuario = idUsuario;
        this.fechaEmision = fechaEmision;
        this.total = total;
        this.estado = estado;
        this.lineasOC = lineasOC;
    }

    /**
     * Constructor para crear una nueva orden de compra antes de persistirla (sin ID) y sin detalle precargado.
     * 
     * @param idProveedor Identificador del proveedor asignado.
     * @param idUsuario Identificador del usuario emisor.
     * @param fechaEmision Fecha y hora de emisión.
     * @param total Monto total de la orden.
     * @param estado Estado inicial de la orden.
     */
    public OrdenDeCompra(int idProveedor, int idUsuario, LocalDateTime fechaEmision, double total, String estado) {
        this.idProveedor = idProveedor;
        this.idUsuario = idUsuario;
        this.fechaEmision = fechaEmision;
        this.total = total;
        this.estado = estado;
    }
    
    /**
     * Constructor para crear una nueva orden de compra completa (sin ID) incluyendo sus líneas de detalle.
     * 
     * @param idProveedor Identificador del proveedor asignado.
     * @param idUsuario Identificador del usuario emisor.
     * @param fechaEmision Fecha y hora de emisión.
     * @param total Monto total de la orden.
     * @param estado Estado inicial de la orden.
     * @param lineasOC Lista de líneas de detalle asociadas.
     */
    public OrdenDeCompra(int idProveedor, int idUsuario, LocalDateTime fechaEmision, double total, String estado, List<LineaOC> lineasOC) {
        this.idProveedor = idProveedor;
        this.idUsuario = idUsuario;
        this.fechaEmision = fechaEmision;
        this.total = total;
        this.estado = estado;
        this.lineasOC = lineasOC;
    }

    // Métodos Getters y Setters
    public int getId() { return id; }
    public int getIdProveedor() { return idProveedor; }
    public int getIdUsuario() { return idUsuario; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public double getTotal() { return total; }
    public String getEstado() { return estado; }
    public List<LineaOC> getLineasOC() { return lineasOC; }
    public String getNombreProveedor() { return nombreProveedor; }
    public String getNombreUsuario() { return nombreUsuario; }

    public void setId(int id) { this.id = id; } 
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }
    public void setTotal(double total) { this.total = total; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setLineasOC(List<LineaOC> lineasOC) { this.lineasOC = lineasOC; }
    public void setNombreProveedor(String nombreProveedor) { this.nombreProveedor = nombreProveedor; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
}