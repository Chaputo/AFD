package modelo;

/**
 * Objeto DTO que representa el stock consolidado (global) de un ítem.
 * Agrupa las existencias totales sumadas de todas las sucursales y evalúa
 * reglas de negocio para alertas de stock crítico.
 */
public class StockConsolidado {
    
    private int idItem;
    private String nombreItem;
    private int stockTotal;
    private int stockMinimoRecomendado; 

    /**
     * Constructor principal para instanciar la vista consolidada de stock de un ítem.
     * 
     * @param idItem Identificador único del ítem/producto.
     * @param nombreItem Nombre o descripción del ítem.
     * @param stockTotal Sumatoria total de unidades disponibles en la cadena/empresa.
     * @param stockMinimoRecomendado Umbral de stock mínimo consolidado para alertas.
     */
    public StockConsolidado(int idItem, String nombreItem, int stockTotal, int stockMinimoRecomendado) {
        this.idItem = idItem;
        this.nombreItem = nombreItem;
        this.stockTotal = stockTotal;
        this.stockMinimoRecomendado = stockMinimoRecomendado;
    }

    // Métodos Getters
    public int getIdItem() {
        return idItem;
    }
    public String getNombreItem() {
        return nombreItem;
    }
    public int getStockTotal() {
        return stockTotal;
    }
    public int getStockMinimoRecomendado() {
        return stockMinimoRecomendado;
    }

    /**
     * Evalúa si el ítem se encuentra en nivel crítico de existencias.
     * 
     * @return true si el stock total es menor o igual al mínimo recomendado; false en caso contrario.
     */
    public boolean estaEnNivelCritico() {
        return this.stockTotal <= this.stockMinimoRecomendado;
    }
}