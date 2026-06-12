package modelo;

public class StockConsolidado {
    
    private int idItem;
    private String nombreItem;
    private int stockTotal;
    
    private int stockMinimoRecomendado; 

    public StockConsolidado(int idItem, String nombreItem, int stockTotal, int stockMinimoRecomendado) {
        this.idItem = idItem;
        this.nombreItem = nombreItem;
        this.stockTotal = stockTotal;
        this.stockMinimoRecomendado = stockMinimoRecomendado;
    }
    
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
    
    public boolean estaEnNivelCritico() {
        return this.stockTotal <= this.stockMinimoRecomendado;
    }
}