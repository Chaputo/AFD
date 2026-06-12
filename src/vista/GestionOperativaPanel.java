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

    private static final String[] ESTADOS_OPERATIVOS = {"Pendiente", "En camino", "Finalizado", "Devuelto", "Cancelado"};
    private final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private JTable tablaPedidosActivos;
    private DefaultTableModel modelPedidos;
    private TableRowSorter<DefaultTableModel> sorter;
    
    private JTextField txtBuscador;
    private JButton btnActualizarEstado;
    private JButton btnRecargar;

    public GestionOperativaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        inicializarModeloTabla();
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(new JScrollPane(tablaPedidosActivos), BorderLayout.CENTER);

        agregarListeners();
        cargarPedidosActivos();
    }

    private void inicializarModeloTabla() {
        modelPedidos = new DefaultTableModel(new Object[]{"ID", "Fecha pedido", "Total", "Estado", "ID Cliente", "ID Usuario"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaPedidosActivos = new JTable(modelPedidos);
        tablaPedidosActivos.setRowHeight(25);
        sorter = new TableRowSorter<>(modelPedidos);
        tablaPedidosActivos.setRowSorter(sorter);
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

    txtBuscador.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyReleased(java.awt.event.KeyEvent evt) {
            // Mantiene el filtro dinámico por ID (columna 0) y Nombre de Cliente (columna 4)
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtBuscador.getText(), 0, 4));
        }
    });
}

    private void cargarPedidosActivos() {
    modelPedidos.setRowCount(0);
    try {
        List<Pedido> pedidos = pedidoDAO.listarPedidosActivos(); 
        for (Pedido p : pedidos) {
            modelPedidos.addRow(new Object[]{
                p.getId(), 
                p.getFechaHora().format(DATETIME_FORMAT), 
                String.format("$%.2f", p.getTotal()), 
                p.getEstado(), 
                p.getNombreCliente(), // CAMBIO: Traemos el texto del nombre
                p.getNombreUsuario()  // CAMBIO: Traemos el texto del usuario
            });
        }
    } catch (SQLException e) { e.printStackTrace(); }
}

    private void mostrarDialogoCambioEstado() {
        int fila = tablaPedidosActivos.getSelectedRow();
        if (fila == -1) return;

        int modelRow = tablaPedidosActivos.convertRowIndexToModel(fila);
        Integer idPedido = (Integer) modelPedidos.getValueAt(modelRow, 0); 
        String estadoAnterior = (String) modelPedidos.getValueAt(modelRow, 3);

        JComboBox<String> cmb = new JComboBox<>(ESTADOS_OPERATIVOS);
        cmb.setSelectedItem(estadoAnterior);

        int opcion = JOptionPane.showConfirmDialog(this, cmb, "Cambiar estado #" + idPedido, JOptionPane.OK_CANCEL_OPTION);

        if (opcion == JOptionPane.OK_OPTION) {
            String nuevoEstado = (String) cmb.getSelectedItem();
            if (!nuevoEstado.equals(estadoAnterior)) {
                confirmarYProcesar(idPedido, estadoAnterior, nuevoEstado);
            }
        }
    }

    private void confirmarYProcesar(int id, String anterior, String nuevo) {
        String mensaje = "¿Confirmar cambio de '" + anterior + "' a '" + nuevo + "'?";
        
        // Lógica de advertencia de stock
        if (anterior.equals("Devuelto") && nuevo.equals("Entregado")) {
            mensaje += "\n⚠️ ATENCIÓN: Se volverá a descontar el stock de los productos.";
        } else if (nuevo.equals("Devuelto")) {
            mensaje += "\nℹ️ Se repondrá el stock de los productos.";
        }

        int conf = JOptionPane.showConfirmDialog(this, mensaje, "Confirmar operación", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            try {
                // Pasamos ambos estados al DAO para que sepa si sumar o restar stock
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