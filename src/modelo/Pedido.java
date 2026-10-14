package modelo;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Representa la cabecera de un Pedido dentro del sistema.
 * Agrupa la información del cliente, el usuario que registró la operación, 
 * la sucursal de origen, montos, estado y la forma de pago utilizada.
 */
public class Pedido {
    private int id;
    private int idCliente;
    private int idUsuario;
    private int idSucursal; 
    private LocalDateTime fechaHora;
    private double total;
    private String estado;
    private String tipoPago;
    private String nombreCliente;
    private String nombreUsuario;

    /**
     * Constructor vacío por defecto.
     */
    public Pedido() {
    }

    /**
     * Constructor para instanciar pedidos leídos de la base de datos mediante IDs relacionales.
     * 
     * @param id Identificador único del pedido.
     * @param idCliente Identificador del cliente.
     * @param idUsuario Identificador del usuario que tomó el pedido.
     * @param idSucursal Identificador de la sucursal donde se realizó la venta.
     * @param fechaHora Fecha y hora exacta de la transacción.
     * @param total Monto total de la operación.
     * @param estado Estado actual del pedido (ej. "PENDIENTE", "ENTREGADO").
     * @param tipoPago Método de pago utilizado (ej. "EFECTIVO", "TARJETA").
     */
    public Pedido(int id, int idCliente, int idUsuario, int idSucursal, LocalDateTime fechaHora, double total, String estado, String tipoPago) {
        this.id = id;
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.idSucursal = idSucursal;
        this.fechaHora = fechaHora;
        this.total = total;
        this.estado = estado;
        this.tipoPago = tipoPago;
    }

    /**
     * Constructor completo para vistas o consultas con JOINs que incluyen las descripciones de cliente y usuario.
     * 
     * @param id Identificador único del pedido.
     * @param idCliente Identificador del cliente.
     * @param nombreCliente Nombre o razón social del cliente.
     * @param idUsuario Identificador del usuario.
     * @param nombreUsuario Nombre del vendedor/usuario.
     * @param idSucursal Identificador de la sucursal.
     * @param fechaHora Fecha y hora del pedido.
     * @param total Monto total.
     * @param estado Estado del pedido.
     * @param tipoPago Tipo de pago efectuado.
     */
    public Pedido(int id, int idCliente, String nombreCliente, int idUsuario, String nombreUsuario, int idSucursal, LocalDateTime fechaHora, double total, String estado, String tipoPago) {
        this.id = id;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.idSucursal = idSucursal;
        this.fechaHora = fechaHora;
        this.total = total;
        this.estado = estado;
        this.tipoPago = tipoPago;
    }

    // Métodos Getters y Setters
    public int getId() { return id; }
    public int getIdCliente() { return idCliente; }
    public int getIdUsuario() { return idUsuario; }
    public int getIdSucursal() { return idSucursal; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public double getTotal() { return total; }
    public String getEstado() { return estado; }
    public String getTipoPago() { return tipoPago; }
    public String getNombreCliente() { return nombreCliente; }
    public String getNombreUsuario() { return nombreUsuario; }

    public void setId(int id) { this.id = id; } 
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public void setFecha(Timestamp timestamp) { 
        if (timestamp != null) {
            this.fechaHora = timestamp.toLocalDateTime();
        }
    }
    public void setFechaPedido(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public void setTotalPedido(double total) { this.total = total; } 
    public void setTotal(double total) { this.total = total; }
    public void setEstadoEntrega(String estado) { this.estado = estado; } 
    public void setEstado(String estado) { this.estado = estado; }
}