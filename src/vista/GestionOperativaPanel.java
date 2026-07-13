package vista;

import modelo.Pedido;
import persistencia.PedidoDAO;
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

    public GestionOperativaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        inicializarModeloTabla();
        inicializarModeloDetalle();

        JScrollPane scrollCabecera = new JScrollPane(tablaPedidosActivos);
        JPanel panelDetalleInferior = new JPanel(new BorderLayout());
        panelDetalleInferior.setBorder(BorderFactory.createTitledBorder("Artículos incluidos en el Pedido Seleccionado"));
        panelDetalleInferior.add(new JScrollPane(tablaDetalleProductos), BorderLayout.CENTER);
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
    }

    private JPanel crearPanelSuperior() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 0));
        panelPrincipal.setBorder(BorderFactory.createTitledBorder("Gestión operativa de pedidos"));
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnActualizarEstado = new JButton("🔄 Cambiar estado");
        btnRecargar = new JButton("↻ Recargar lista");
        btnActualizarEstado.setBackground(new Color(255, 165, 0));
        btnActualizarEstado.setForeground(Color.WHITE);
        panelBotones.add(btnActualizarEstado);
        panelBotones.add(btnRecargar);
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
        chkMostrarTodo.addActionListener(e -> cargarPedidosActivos());
        tablaPedidosActivos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarDetalleProductosSeleccionados();
            }
        });

        txtBuscador.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String texto = txtBuscador.getText();
                if (texto.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 0, 4));
                }
            }
        });
    }

    private void cargarPedidosActivos() {
        modelPedidos.setRowCount(0);
        modelDetalle.setRowCount(0);
        try {
            List<Pedido> pedidos = pedidoDAO.listarPedidosActivos(chkMostrarTodo.isSelected()); 
            for (Pedido p : pedidos) {
                modelPedidos.addRow(new Object[]{
                    p.getId(), 
                    p.getFechaHora().format(DATETIME_FORMAT), 
                    String.format("$%.2f", p.getTotal()),       
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
        int fila = tablaPedidosActivos.getSelectedRow();
        if (fila == -1) return;
        int modelRow = tablaPedidosActivos.convertRowIndexToModel(fila);
        Integer idPedido = (Integer) modelPedidos.getValueAt(modelRow, 0);
        try {
            List<Object[]> lineas = pedidoDAO.buscarDetalleProductosPorPedido(idPedido);
            for (Object[] linea : lineas) {
                String precioFormateado = String.format("$%.2f", (Double) linea[3]);
                String subtotalFormateado = String.format("$%.2f", (Double) linea[4]);
                modelDetalle.addRow(new Object[]{
                    linea[0],
                    linea[1],
                    linea[2],
                    precioFormateado,
                    subtotalFormateado
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error de carga en componentes para el Pedido #" + idPedido);
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
        if (anterior.equalsIgnoreCase("Devuelto") && (nuevo.equalsIgnoreCase("Entregado") || nuevo.equalsIgnoreCase("Finalizado"))) {
            mensaje += "\n⚠️ ATENCIÓN: Se volverá a descontar el stock de los productos.";
        } else if (nuevo.equalsIgnoreCase("Devuelto")) {
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
}