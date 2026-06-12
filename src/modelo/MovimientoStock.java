package modelo;

import java.time.LocalDateTime;

public class MovimientoStock {
   private int id;
   private LocalDateTime fecha;
   private String tipo;
   private int cantidad;
   private String skuProducto;
   private Integer idOcRelacionada;
   private int idSucursal;

   public MovimientoStock() {
   }

   public MovimientoStock(int id, LocalDateTime fecha, String tipo, int cantidad, String skuProducto, Integer idOcRelacionada, int idSucursal) {
      this.id = id;
      this.fecha = fecha;
      this.tipo = tipo;
      this.cantidad = cantidad;
      this.skuProducto = skuProducto;
      this.idOcRelacionada = idOcRelacionada;
      this.idSucursal = idSucursal; //
   }

   public MovimientoStock(LocalDateTime fecha, String tipo, int cantidad, String skuProducto, Integer idOcRelacionada, int idSucursal) {
      this.fecha = fecha;
      this.tipo = tipo;
      this.cantidad = cantidad;
      this.skuProducto = skuProducto;
      this.idOcRelacionada = idOcRelacionada;
      this.idSucursal = idSucursal; //
   }

   public int getId() { return this.id; }
   public LocalDateTime getFecha() { return this.fecha; }
   public String getTipo() { return this.tipo; }
   public int getCantidad() { return this.cantidad; }
   public String getSkuProducto() { return this.skuProducto; }
   public Integer getIdOcRelacionada() { return this.idOcRelacionada; }
   public int getIdSucursal() { return this.idSucursal; } //

   public void setId(int id) { this.id = id; }
   public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
   public void setTipo(String tipo) { this.tipo = tipo; }
   public void setCantidad(int cantidad) { this.cantidad = cantidad; }
   public void setSkuProducto(String skuProducto) { this.skuProducto = skuProducto; }
   public void setIdOcRelacionada(Integer idOcRelacionada) { this.idOcRelacionada = idOcRelacionada; }
   public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }
}