package modelo;

//Definición de la Clase y Atributos

public class Ingrediente {
    private int id;
    private String nombre;
    private int cantidadStock;

    //Definición de los constructores

    public Ingrediente(int id, String nombre, int cantidadStock) {
        this.id = id;
        this.nombre = nombre;
        this.cantidadStock = cantidadStock;
    }

    // Metodos Getters y Setters
    
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getCantidadStock() { return cantidadStock; }
    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCantidadStock(int cantidadStock) { this.cantidadStock = cantidadStock; }
    @Override
    public String toString() {
        return nombre + " (Stock: " + cantidadStock + ")"; 
    }
}