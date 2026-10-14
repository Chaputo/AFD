package vista;

import modelo.Pedido;
import persistencia.PedidoDAO;
import util.ExporterExcel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GestionOperativaPanel extends JPanel {

    // Variables y Constructor

    private static final String[] ESTADOS_OPERATIVOS = {"Pendiente", "En camino", "Finalizado", "Devuelto", "Cancelado"};
    private final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private JTable tablaPedidosActivos;
    private DefaultTableModel modelPedidos;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTable tablaDetalleProductos;
    private DefaultTableModel modelDetalle;
    private JTextField txtBuscador;
    private JCheckBox chkMostrarTodo; 
    private JButton btnActualizarEstado;
    private JButton btnRecargar;
    private JButton btnExportarExcel;
    private JButton btnDevolverItem; // <-- Nuevo botón para devolución parcial

    public GestionOperativaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        inicializarModeloTabla();
        inicializarModeloDetalle();

        JScrollPane scrollCabecera = new JScrollPane(tablaPedidosActivos);
        
        // Panel inferior para los detalles
        JPanel panelDetalleInferior = new JPanel(new BorderLayout());
        panelDetalleInferior.setBorder(BorderFactory.createTitledBorder("Artículos incluidos en el Pedido Seleccionado"));
        panelDetalleInferior.add(new JScrollPane(tablaDetalleProductos), BorderLayout.CENTER);
        
        // Barra de herramientas para el detalle
        JPanel panelAccionesDetalle = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnDevolverItem = new JButton("↩️ Devolver producto seleccionado");
        btnDevolverItem.setEnabled(false); // Se habilita solo al seleccionar un ítem
        panelAccionesDetalle.add(btnDevolverItem);
        panelDetalleInferior.add(panelAccionesDetalle, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollCabecera, panelDetalleInferior);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        agregarListeners();
        cargarPedidosActivos();
    }

    private void inicializarModeloTabla() {
        modelPedidos = new DefaultTableModel(new Object[]{"ID", "Fecha pedido", "Total", "Estado", "Cliente", "Usuario"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaPedidosActivos = new JTable(modelPedidos);
        tablaPedidosActivos.setRowHeight(25);
        tablaPedidosActivos.getColumnModel().getColumn(2).setCellRenderer(new CurrencyCellRenderer());
        sorter = new TableRowSorter<>(modelPedidos);
        tablaPedidosActivos.setRowSorter(sorter);
    }

    private void inicializarModeloDetalle() {
        modelDetalle = new DefaultTableModel(new Object[]{"SKU Producto", "Descripción / Nombre", "Cantidad", "Precio Unitario", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaDetalleProductos = new JTable(modelDetalle);
        tablaDetalleProductos.setRowHeight(23);
        tablaDetalleProductos.getColumnModel().getColumn(2).setCellRenderer(new RightAlignedCellRenderer());
        tablaDetalleProductos.getColumnModel().getColumn(3).setCellRenderer(new CurrencyCellRenderer());
        tablaDetalleProductos.getColumnModel().getColumn(4).setCellRenderer(new CurrencyCellRenderer());
    }

    private JPanel crearPanelSuperior() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 0));
        panelPrincipal.setBorder(BorderFactory.createTitledBorder("Gestión operativa de pedidos"));
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnActualizarEstado = new JButton("🔄 Cambiar estado");
        btnRecargar = new JButton("↻ Recargar lista");
        btnExportarExcel = new JButton("Exportar a Excel");
        btnActualizarEstado.setBackground(new Color(255, 165, 0));
        btnActualizarEstado.setForeground(Color.WHITE);
        panelBotones.add(btnActualizarEstado);
        panelBotones.add(btnRecargar);
        panelBotones.add(btnExportarExcel);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        chkMostrarTodo = new JCheckBox("Mostrar historial completo");
        panelBusqueda.add(chkMostrarTodo);
        panelBusqueda.add(new JLabel("Buscar:"));
        txtBuscador = new JTextField(15);
        panelBusqueda.add(txtBuscador);
        panelPrincipal.add(panelBotones, BorderLayout.WEST);
        panelPrincipal.add(panelBusqueda, BorderLayout.EAST);
        return panelPrincipal;
    }

    private void agregarListeners() {
    btnActualizarEstado.addActionListener(e -> mostrarDialogoCambioEstado());
    btnRecargar.addActionListener(e -> cargarPedidosActivos());
    btnExportarExcel.addActionListener(e -> {
        if (tablaPedidosActivos.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos en la tabla para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ExporterExcel.exportarTabla(tablaPedidosActivos, "Gestion_Pedidos");
    });
    chkMostrarTodo.addActionListener(e -> cargarPedidosActivos());
    
    // Listener de la tabla de Pedidos
    tablaPedidosActivos.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            cargarDetalleProductosSeleccionados();
        }
    });

    // Listener de la tabla de Detalles (Habilita botón de devolución parcial)
    tablaDetalleProductos.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            btnDevolverItem.setEnabled(tablaDetalleProductos.getSelectedRow() != -1);
        }
    });

    // Evento del botón de devolución parcial
    btnDevolverItem.addActionListener(e -> procesarDevolucionParcial());

    // Buscador flexible (ignora tildes, acentos y mayúsculas/minúsculas)
    txtBuscador.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyReleased(java.awt.event.KeyEvent evt) {
            String busqueda = normalizarTexto(txtBuscador.getText());
            
            if (busqueda.trim().isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        // Revisa las columnas: ID (0), Fecha (1), Estado (3), Cliente (4), Usuario (5)
                        int[] columnasAFiltrar = {0, 1, 3, 4, 5}; 
                        
                        for (int col : columnasAFiltrar) {
                            String valorCelda = entry.getStringValue(col);
                            String celdaNormalizada = normalizarTexto(valorCelda);
                            if (celdaNormalizada.contains(busqueda)) {
                                return true; // Coincide si lo encuentra en cualquier columna
                            }
                        }
                        return false;
                    }
                });
            }
        }
    });
}

    private void cargarPedidosActivos() {
        modelPedidos.setRowCount(0);
        modelDetalle.setRowCount(0);
        btnDevolverItem.setEnabled(false);
        try {
            List<Pedido> pedidos = pedidoDAO.listarPedidosActivos(chkMostrarTodo.isSelected()); 
            for (Pedido p : pedidos) {
                modelPedidos.addRow(new Object[]{
                    p.getId(), 
                    p.getFechaHora().format(DATETIME_FORMAT), 
                    p.getTotal(),       
                    p.getEstado(),                             
                    p.getNombreCliente(), 
                    p.getNombreUsuario()  
                });
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos: " + e.getMessage(), "Error de Carga", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDetalleProductosSeleccionados() {
        modelDetalle.setRowCount(0); 
        btnDevolverItem.setEnabled(false);
        int fila = tablaPedidosActivos.getSelectedRow();
        if (fila == -1) return;
        int modelRow = tablaPedidosActivos.convertRowIndexToModel(fila);
        Integer idPedido = (Integer) modelPedidos.getValueAt(modelRow, 0);
        try {
            List<Object[]> lineas = pedidoDAO.buscarDetalleProductosPorPedido(idPedido);
            for (Object[] linea : lineas) {
                modelDetalle.addRow(new Object[]{
                    linea[0],
                    linea[1],
                    linea[2],
                    linea[3],
                    linea[4]
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error de carga en componentes para el Pedido #" + idPedido);
        }
    }

    private void procesarDevolucionParcial() {
        int filaPedido = tablaPedidosActivos.getSelectedRow();
        int filaDetalle = tablaDetalleProductos.getSelectedRow();

        if (filaPedido == -1 || filaDetalle == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la lista inferior.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRowPedido = tablaPedidosActivos.convertRowIndexToModel(filaPedido);
        Integer idPedido = (Integer) modelPedidos.getValueAt(modelRowPedido, 0);
        
        String sku = (String) modelDetalle.getValueAt(filaDetalle, 0);
        String descripcion = (String) modelDetalle.getValueAt(filaDetalle, 1);
        int cantMax = Integer.parseInt(modelDetalle.getValueAt(filaDetalle, 2).toString());

        // Diálogo para pedir la cantidad a devolver
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, cantMax, 1);
        JSpinner spinner = new JSpinner(spinnerModel);
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Producto: " + descripcion + " (" + sku + ")"));
        panel.add(new JLabel("Cantidad a devolver (Máx: " + cantMax + "):"));
        panel.add(spinner);

        int result = JOptionPane.showConfirmDialog(this, panel, "Devolución parcial - Pedido #" + idPedido, JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            int cantADevolver = (Integer) spinner.getValue();
            
           try {
                // Ejecuta la transacción real en la base de datos
                boolean exito = pedidoDAO.devolverProductoIndividual(idPedido, sku, cantADevolver);
                
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Se reintegraron " + cantADevolver + " unidad(es) de '" + descripcion + "' al inventario.");
                    cargarPedidosActivos(); // Recarga las tablas con las cantidades y totales actualizados
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al procesar devolución: " + e.getMessage(), "Error DB", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarDialogoCambioEstado() {
        int fila = tablaPedidosActivos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un pedido de la lista.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tablaPedidosActivos.convertRowIndexToModel(fila);
        Integer idPedido = (Integer) modelPedidos.getValueAt(modelRow, 0); 
        String estadoAnterior = (String) modelPedidos.getValueAt(modelRow, 3);
        JComboBox<String> cmb = new JComboBox<>(ESTADOS_OPERATIVOS);
        cmb.setSelectedItem(estadoAnterior);
        int opcion = JOptionPane.showConfirmDialog(this, cmb, "Cambiar estado #" + idPedido, JOptionPane.OK_CANCEL_OPTION);
        if (opcion == JOptionPane.OK_OPTION) {
            String nuevoEstado = (String) cmb.getSelectedItem();
            if (nuevoEstado != null && !nuevoEstado.equals(estadoAnterior)) {
                confirmarYProcesar(idPedido, estadoAnterior, nuevoEstado);
            }
        }
    }

    private void confirmarYProcesar(int id, String anterior, String nuevo) {
        String mensaje = "¿Confirmar cambio de '" + anterior + "' a '" + nuevo + "'?";
        boolean anteriorRepuesto = anterior.equalsIgnoreCase("Devuelto") || anterior.equalsIgnoreCase("Cancelado");
        boolean nuevoRepuesto = nuevo.equalsIgnoreCase("Devuelto") || nuevo.equalsIgnoreCase("Cancelado");
        if (anteriorRepuesto && !nuevoRepuesto) {
            mensaje += "\n⚠️ ATENCIÓN: Se volverá a descontar el stock de los productos.";
        } else if (!anteriorRepuesto && nuevoRepuesto) {
            mensaje += "\nℹ️ Se repondrá el stock de los productos.";
        }
        int conf = JOptionPane.showConfirmDialog(this, mensaje, "Confirmar operación", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            try {
                if (pedidoDAO.actualizarEstadoConStock(id, anterior, nuevo)) {
                    JOptionPane.showMessageDialog(this, "Estado actualizado con éxito.");
                    cargarPedidosActivos(); 
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error DB", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String normalizarTexto(String texto) {
    if (texto == null) return "";
    String temp = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD);
    return temp.replaceAll("\\p{M}", "").toLowerCase();
    }
}