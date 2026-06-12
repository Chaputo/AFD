package modelo;
import java.time.LocalDateTime;

public class Stock_SucursalDTO {
    private int id;
    private String skuProducto;
    private String nombreProducto;
    private int idSucursal;
    private String nombreSucursal;
    private int cantidadDisponible;
    private LocalDateTime fechaUltimoMovimiento;

    public Stock_SucursalDTO() {}

    public Stock_SucursalDTO(int id, String skuProducto, String nombreProducto, 
                            int idSucursal, String nombreSucursal, int cantidadDisponible, 
                            LocalDateTime fechaUltimoMovimiento) {
        this.id = id;
        this.skuProducto = skuProducto;
        this.nombreProducto = nombreProducto;
        this.idSucursal = idSucursal;
        this.nombreSucursal = nombreSucursal;
        this.cantidadDisponible = cantidadDisponible;
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
    }

    public int getId() { return id; }
    public String getSkuProducto() { return skuProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public int getIdSucursal() { return idSucursal; }
    public String getNombreSucursal() { return nombreSucursal; }
    public int getCantidadDisponible() { return cantidadDisponible; }
    public LocalDateTime getFechaUltimoMovimiento() { return fechaUltimoMovimiento; }

    public void setId(int id) { this.id = id; }
    public void setSkuProducto(String skuProducto) { this.skuProducto = skuProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }
    public void setNombreSucursal(String nombreSucursal) { this.nombreSucursal = nombreSucursal; }
    public void setCantidadDisponible(int cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
    public void setFechaUltimoMovimiento(LocalDateTime fechaUltimoMovimiento) { this.fechaUltimoMovimiento = fechaUltimoMovimiento; }
}