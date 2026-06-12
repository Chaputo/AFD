package modelo;

import java.time.LocalDateTime;
import java.util.List; 


public class OrdenDeCompra {
    private int id;
    private int idProveedor; 
    private int idUsuario;
    private LocalDateTime fechaEmision;
    private double total;
    private String estado; 
    
    private List<LineaOC> lineasOC; 

    public OrdenDeCompra(int id, int idProveedor, int idUsuario, LocalDateTime fechaEmision, double total, String estado) {
        this.id = id;
        this.idProveedor = idProveedor;
        this.idUsuario = idUsuario;
        this.fechaEmision = fechaEmision;
        this.total = total;
        this.estado = estado;
    }
    
    public OrdenDeCompra(int id, int idProveedor, int idUsuario, LocalDateTime fechaEmision, double total, String estado, List<LineaOC> lineasOC) {
        this.id = id;
        this.idProveedor = idProveedor;
        this.idUsuario = idUsuario;
        this.fechaEmision = fechaEmision;
        this.total = total;
        this.estado = estado;
        this.lineasOC = lineasOC;
    }

    public OrdenDeCompra(int idProveedor, int idUsuario, LocalDateTime fechaEmision, double total, String estado) {
        this.idProveedor = idProveedor;
        this.idUsuario = idUsuario;
        this.fechaEmision = fechaEmision;
        this.total = total;
        this.estado = estado;
    }
    
    public OrdenDeCompra(int idProveedor, int idUsuario, LocalDateTime fechaEmision, double total, String estado, List<LineaOC> lineasOC) {
        this.idProveedor = idProveedor;
        this.idUsuario = idUsuario;
        this.fechaEmision = fechaEmision;
        this.total = total;
        this.estado = estado;
        this.lineasOC = lineasOC;
    }

    public int getId() { return id; }
    public int getIdProveedor() { return idProveedor; }
    public int getIdUsuario() { return idUsuario; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public double getTotal() { return total; }
    public String getEstado() { return estado; }
    public List<LineaOC> getLineasOC() { return lineasOC; }

    public void setId(int id) { this.id = id; } 
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setTotal(double total) { this.total = total; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setLineasOC(List<LineaOC> lineasOC) { this.lineasOC = lineasOC; }
}