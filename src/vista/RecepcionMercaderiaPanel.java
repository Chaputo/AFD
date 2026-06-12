package vista;
import modelo.OrdenDeCompra;
import modelo.LineaOC;
import modelo.MovimientoStock;
import persistencia.OrdenDeCompraDAO;
import persistencia.MovimientoStockDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecepcionMercaderiaPanel extends JPanel {

    private OrdenDeCompraDAO ocDAO = new OrdenDeCompraDAO();
    private MovimientoStockDAO movimientoDAO = new MovimientoStockDAO();

    private JTable tablaOC;
    private DefaultTableModel modeloTablaOC;
    private JTable tablaLineas;
    private DefaultTableModel modeloTablaLineas;
    private JButton btnConfirmarRecepcion;
    private List<OrdenDeCompra> ordenesEmitidas;
    
    private static final int ID_SUCURSAL_RECEPCION = 1;

    public RecepcionMercaderiaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        modeloTablaOC = new DefaultTableModel(new String[]{"ID", "Proveedor", "Fecha emisión", "Total", "Estado"}, 0);
        tablaOC = new JTable(modeloTablaOC);
        tablaOC.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaOC.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaOC.getSelectedRow() != -1) {
                cargarLineasOCSeleccionada();
            }
        });

        modeloTablaLineas = new DefaultTableModel(new String[]{"SKU", "Producto", "Cant. pedida", "Cant. a recibir"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 3; }
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 3 ? Integer.class : super.getColumnClass(column);
            }
        };
        tablaLineas = new JTable(modeloTablaLineas);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(new JScrollPane(tablaOC));
        splitPane.setBottomComponent(new JScrollPane(tablaLineas));
        splitPane.setDividerLocation(150);
        
        add(splitPane, BorderLayout.CENTER);
        
        btnConfirmarRecepcion = new JButton("📦 Confirmar recepción y actualizar stock");
        btnConfirmarRecepcion.setBackground(new Color(41, 128, 185));
        btnConfirmarRecepcion.setForeground(Color.WHITE);
        btnConfirmarRecepcion.addActionListener(e -> confirmarRecepcion());
        
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSur.add(btnConfirmarRecepcion);
        add(panelSur, BorderLayout.SOUTH);

        cargarOrdenesDeCompraEmitidas();
    }
    
    private void cargarOrdenesDeCompraEmitidas() {
        modeloTablaOC.setRowCount(0);
        try {
            ordenesEmitidas = ocDAO.buscarPorEstadoPendiente(); 
            for (OrdenDeCompra oc : ordenesEmitidas) {
                modeloTablaOC.addRow(new Object[]{
                    oc.getId(), 
                    ocDAO.obtenerNombreProveedor(oc.getIdProveedor()), 
                    oc.getFechaEmision().toLocalDate(), 
                    oc.getTotal(), 
                    oc.getEstado()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void cargarLineasOCSeleccionada() {
        int fila = tablaOC.getSelectedRow();
        if (fila == -1) return;
        
        int idOc = (int) modeloTablaOC.getValueAt(fila, 0);
        OrdenDeCompra ocSeleccionada = ordenesEmitidas.stream()
            .filter(oc -> oc.getId() == idOc).findFirst().orElse(null);
            
        if (ocSeleccionada == null) return;
        modeloTablaLineas.setRowCount(0);
        
        for (LineaOC linea : ocSeleccionada.getLineasOC()) {
            modeloTablaLineas.addRow(new Object[]{
                linea.getSkuProducto(),
                linea.getNombreProducto(),
                linea.getCantidadPedida(),
                linea.getCantidadPedida() 
            });
        }
    }
    
    private void confirmarRecepcion() {
        int fila = tablaOC.getSelectedRow();
        if (fila == -1) return;
        
        int idOc = (int) modeloTablaOC.getValueAt(fila, 0);
        List<MovimientoStock> movimientos = new ArrayList<>();
        
        try {
            for (int i = 0; i < modeloTablaLineas.getRowCount(); i++) {
                String sku = (String) modeloTablaLineas.getValueAt(i, 0);
                int cantidadRecibida = Integer.parseInt(modeloTablaLineas.getValueAt(i, 3).toString());
                
                if (cantidadRecibida > 0) {
                    MovimientoStock mov = new MovimientoStock(
                        LocalDateTime.now(), 
                        "INGRESO", 
                        cantidadRecibida, 
                        sku, 
                        idOc,
                        ID_SUCURSAL_RECEPCION
                    );
                    movimientos.add(mov);
                }
            }
            
            if (movimientos.isEmpty()) return;

            for (MovimientoStock mov : movimientos) {
                movimientoDAO.registrarIngreso(mov);
            }
            
            ocDAO.actualizarEstado(idOc, "RECIBIDA_TOTAL");
            JOptionPane.showMessageDialog(this, "Stock actualizado correctamente en Sucursal Central.");
            cargarOrdenesDeCompraEmitidas();
            modeloTablaLineas.setRowCount(0);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en recepción: " + e.getMessage());
        }
    }
}