package vista;

import modelo.OrdenDeCompra; 
import persistencia.OrdenDeCompraDAO; 
import persistencia.ConexionDB;
import util.ExporterExcel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GestionOperativaOCPanel extends JPanel {

    // Variables y Constructor

    private static final String[] ESTADOS_OC = {"EMITIDA", "SOLICITADA", "RECIBIDA", "CANCELADA"};
    private final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final OrdenDeCompraDAO ocDAO = new OrdenDeCompraDAO();
    private JTable tablaOCActivas;
    private DefaultTableModel modelOC;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTable tablaDetalleProductos;
    private DefaultTableModel modelDetalle;
    private JTextField txtBuscador;
    private JCheckBox chkMostrarTodo; 
    private JButton btnActualizarEstado;
    private JButton btnRecargar;
    private JButton btnExportarExcel;

    private static class SucursalItem {
        int id;
        String nombre;
        public SucursalItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }

    public GestionOperativaOCPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        inicializarModeloTabla();
        inicializarModeloDetalle();
        JScrollPane scrollCabecera = new JScrollPane(tablaOCActivas);
        JPanel panelDetalleInferior = new JPanel(new BorderLayout());
        panelDetalleInferior.setBorder(BorderFactory.createTitledBorder("Detalle de Productos de la OC Seleccionada"));
        panelDetalleInferior.add(new JScrollPane(tablaDetalleProductos), BorderLayout.CENTER);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollCabecera, panelDetalleInferior);
        splitPane.setDividerLocation(300); 
        splitPane.setResizeWeight(0.5); 
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        agregarListeners();
        cargarOrdenesCompra();
    }

    private void inicializarModeloTabla() {
        modelOC = new DefaultTableModel(new Object[]{"ID OC", "Fecha Emisión", "Total", "Estado", "Proveedor", "Usuario"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaOCActivas = new JTable(modelOC);
        tablaOCActivas.setRowHeight(25);
        tablaOCActivas.getColumnModel().getColumn(2).setCellRenderer(new CurrencyCellRenderer());
        sorter = new TableRowSorter<>(modelOC);
        tablaOCActivas.setRowSorter(sorter);
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
        panelPrincipal.setBorder(BorderFactory.createTitledBorder("Gestión operativa de órdenes de compra"));
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
    btnRecargar.addActionListener(e -> cargarOrdenesCompra());
    btnExportarExcel.addActionListener(e -> {
        if (tablaOCActivas.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos en la tabla para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ExporterExcel.exportarTabla(tablaOCActivas, "Ordenes_De_Compra");
    });
    chkMostrarTodo.addActionListener(e -> cargarOrdenesCompra());
    tablaOCActivas.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) { 
            cargarDetalleProductosSeleccionados();
        }
    });

    // Buscador insensible a tildes, acentos y mayúsculas
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
                        // Evalúa: ID OC (0), Fecha (1), Estado (3), Proveedor (4), Usuario (5)
                        int[] columnasAFiltrar = {0, 1, 3, 4, 5}; 
                        
                        for (int col : columnasAFiltrar) {
                            String valorCelda = entry.getStringValue(col);
                            String celdaNormalizada = normalizarTexto(valorCelda);
                            if (celdaNormalizada.contains(busqueda)) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
            }
        }
    });
}

    private void cargarOrdenesCompra() {
        modelOC.setRowCount(0);
        modelDetalle.setRowCount(0); 
        try {
            List<OrdenDeCompra> ordenes = ocDAO.listarOrdenesActivas(chkMostrarTodo.isSelected()); 
            for (OrdenDeCompra o : ordenes) {
                modelOC.addRow(new Object[]{
                    o.getId(), 
                    o.getFechaEmision().format(DATETIME_FORMAT), 
                    o.getTotal(), 
                    o.getEstado(), 
                    o.getNombreProveedor(), 
                    o.getNombreUsuario()  
                });
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos: " + e.getMessage(), "Error de Carga", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDetalleProductosSeleccionados() {
        modelDetalle.setRowCount(0); 
        int fila = tablaOCActivas.getSelectedRow();
        if (fila == -1) return; 
        int modelRow = tablaOCActivas.convertRowIndexToModel(fila);
        Integer idOC = (Integer) modelOC.getValueAt(modelRow, 0);
        try {
            List<Object[]> lineas = ocDAO.buscarDetalleProductosPorOrden(idOC);
            for (Object[] linea : lineas) {
                modelDetalle.addRow(new Object[]{
                    linea[0], 
                    linea[1], 
                    linea[2], 
                    linea[3],
                    linea[4],
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al cargar los productos de la OC #" + idOC);
        }
    }

    private void mostrarDialogoCambioEstado() {
        int fila = tablaOCActivas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una orden de compra de la lista.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tablaOCActivas.convertRowIndexToModel(fila);
        Integer idOC = (Integer) modelOC.getValueAt(modelRow, 0); 
        String estadoAnterior = (String) modelOC.getValueAt(modelRow, 3);
        JComboBox<String> cmbEstado = new JComboBox<>(ESTADOS_OC);
        cmbEstado.setSelectedItem(estadoAnterior);
        JComboBox<SucursalItem> cmbSucursales = new JComboBox<>();
        try (Connection conn = ConexionDB.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_sucursal, nombre FROM sucursales ORDER BY nombre")) {
            while (rs.next()) {
                cmbSucursales.addItem(new SucursalItem(rs.getInt("id_sucursal"), rs.getString("nombre")));
            }
        } catch (SQLException ex) {
            System.err.println("Error al obtener sucursales en caliente: " + ex.getMessage());
        }
        if (cmbSucursales.getItemCount() == 0) {
            cmbSucursales.addItem(new SucursalItem(1, "Sucursal Por Defecto (ID 1)"));
        }
        JPanel panelFormularioModal = new JPanel(new GridLayout(4, 1, 5, 5));
        panelFormularioModal.add(new JLabel("Seleccione el nuevo estado:"));
        panelFormularioModal.add(cmbEstado);
        panelFormularioModal.add(new JLabel("Sucursal de destino (ingreso de mercadería):"));
        panelFormularioModal.add(cmbSucursales);
        int opcion = JOptionPane.showConfirmDialog(this, panelFormularioModal, "Procesar OC #" + idOC, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcion == JOptionPane.OK_OPTION) {
            String nuevoEstado = (String) cmbEstado.getSelectedItem();
            SucursalItem sucursalSeleccionada = (SucursalItem) cmbSucursales.getSelectedItem();
            if (nuevoEstado != null && sucursalSeleccionada != null) {
                confirmarYProcesar(idOC, estadoAnterior, nuevoEstado, sucursalSeleccionada.id, sucursalSeleccionada.toString());
            }
        }
    }

    private void confirmarYProcesar(int id, String anterior, String nuevo, int idSucursal, String nombreSucursal) {
        String mensaje = "¿Confirmar cambio de estado de la OC '" + anterior + "' a '" + nuevo + "'?";
        if (nuevo.equalsIgnoreCase("RECIBIDA")) {
            mensaje += "\nℹ️ Se sumará la mercadería al stock de " + nombreSucursal + ".";
        } else if (anterior.equalsIgnoreCase("RECIBIDA") && nuevo.equalsIgnoreCase("CANCELADA")) {
            mensaje += "\n⚠️ ATENCIÓN: Se restará la mercadería del stock de la sucursal donde se recibió."
                     + "\nSi la OC es anterior a este registro, se restará de " + nombreSucursal + ".";
        }
        int conf = JOptionPane.showConfirmDialog(this, mensaje, "Confirmar operación", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            try {
                if (ocDAO.actualizarEstadoConStock(id, anterior, nuevo, idSucursal)) {
                    JOptionPane.showMessageDialog(this, "Estado y Stock procesados correctamente.");
                    cargarOrdenesCompra(); 
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error en la operación de base de datos: " + e.getMessage(), "Error DB", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

private static String normalizarTexto(String texto) {
    if (texto == null) return "";
    String temp = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD);
    return temp.replaceAll("\\p{M}", "").toLowerCase();
}

}