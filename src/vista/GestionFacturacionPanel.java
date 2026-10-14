package vista;

import persistencia.ConexionDB;
import util.ExporterExcel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GestionFacturacionPanel extends JPanel {

    // Variables y Constructor

    private final DateTimeFormatter DATE_PARSE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private JTable tablaMovimientos;
    private DefaultTableModel modelMovimientos;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTable tablaDetalleItems;
    private DefaultTableModel modelDetalle;
    private JComboBox<SucursalFiltroItem> cmbFiltroSucursal;
    private JTextField txtBuscadorGlobal;
    private JButton btnRecargar;
    private JButton btnExportarExcel;
    private JLabel lblTotalFacturado;
    private JFormattedTextField txtFechaDesde;
    private JFormattedTextField txtFechaHasta;
    private JButton btnLimpiarFechas;

    @SuppressWarnings("unused")
    private static class SucursalFiltroItem {
        int id; 
        String nombre;
        public SucursalFiltroItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }

    public GestionFacturacionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        inicializarTablas();
        JScrollPane scrollMaestro = new JScrollPane(tablaMovimientos);
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(BorderFactory.createTitledBorder("Artículos / Ítems del Movimiento Seleccionado"));
        panelInferior.add(new JScrollPane(tablaDetalleItems), BorderLayout.CENTER);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollMaestro, panelInferior);
        splitPane.setDividerLocation(280);
        splitPane.setResizeWeight(0.6);
        JPanel panelResumenCaja = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        lblTotalFacturado = new JLabel("Total Filtrado: $0.00");
        lblTotalFacturado.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalFacturado.setForeground(new Color(0, 128, 0));
        panelResumenCaja.add(lblTotalFacturado);
        add(crearPanelFiltros(), BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(panelResumenCaja, BorderLayout.SOUTH);
        agregarListeners();
        cargarSucursalesEnCombo();
        cargarMovimientosFacturacion();
    }

    private void inicializarTablas() {
        modelMovimientos = new DefaultTableModel(
            new Object[]{"ID Factura", "Fecha / Hora", "Tipo", "Sucursal", "Vendedor / Usuario", "Cliente", "Total"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaMovimientos = new JTable(modelMovimientos);
        tablaMovimientos.setRowHeight(25);
        tablaMovimientos.getColumnModel().getColumn(6).setCellRenderer(new CurrencyCellRenderer());
        sorter = new TableRowSorter<>(modelMovimientos);
        tablaMovimientos.setRowSorter(sorter);
        modelDetalle = new DefaultTableModel(
            new Object[]{"SKU", "Producto / Concepto", "Cantidad", "Precio Unitario", "Subtotal"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaDetalleItems = new JTable(modelDetalle);
        tablaDetalleItems.setRowHeight(23);
        tablaDetalleItems.getColumnModel().getColumn(2).setCellRenderer(new RightAlignedCellRenderer());
        tablaDetalleItems.getColumnModel().getColumn(3).setCellRenderer(new CurrencyCellRenderer());
        tablaDetalleItems.getColumnModel().getColumn(4).setCellRenderer(new CurrencyCellRenderer());
    }

    private JPanel crearPanelFiltros() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 0));
        panelPrincipal.setBorder(BorderFactory.createTitledBorder("Auditoría General de Facturación y Cajas"));
        JPanel panelIzquierdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelIzquierdo.add(new JLabel("Sucursal:"));
        cmbFiltroSucursal = new JComboBox<>();
        panelIzquierdo.add(cmbFiltroSucursal);
        try {
            MaskFormatter mascaraFecha = new MaskFormatter("##/##/####");
            mascaraFecha.setPlaceholderCharacter('_');
            txtFechaDesde = new JFormattedTextField(mascaraFecha);
            txtFechaDesde.setColumns(8);
            txtFechaDesde.setToolTipText("Fecha Desde (DD/MM/AAAA)");
            txtFechaHasta = new JFormattedTextField(mascaraFecha);
            txtFechaHasta.setColumns(8);
            txtFechaHasta.setToolTipText("Fecha Hasta (DD/MM/AAAA)");
            btnLimpiarFechas = new JButton("❌");
            btnLimpiarFechas.setToolTipText("Limpiar rango de fechas");
            panelIzquierdo.add(new JSeparator(JSeparator.VERTICAL));
            panelIzquierdo.add(new JLabel("Desde:"));
            panelIzquierdo.add(txtFechaDesde);
            panelIzquierdo.add(new JLabel("Hasta:"));
            panelIzquierdo.add(txtFechaHasta);
            panelIzquierdo.add(btnLimpiarFechas);
        } catch (Exception e) {
            System.err.println("Error al crear la máscara de fechas: " + e.getMessage());
        }
        btnRecargar = new JButton("↻ Actualizar");
        btnExportarExcel = new JButton("Exportar a Excel");
        
        panelIzquierdo.add(btnRecargar);
        panelIzquierdo.add(btnExportarExcel);

        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelDerecho.add(new JLabel("Buscar (Vendedor, Cliente, ID...):"));
        txtBuscadorGlobal = new JTextField(15);
        panelDerecho.add(txtBuscadorGlobal);
        panelPrincipal.add(panelIzquierdo, BorderLayout.WEST);
        panelPrincipal.add(panelDerecho, BorderLayout.EAST);
        return panelPrincipal;
    }

    private void agregarListeners() {
        btnRecargar.addActionListener(e -> cargarMovimientosFacturacion());
        btnExportarExcel.addActionListener(e -> {
            if (tablaMovimientos.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay datos en la tabla para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ExporterExcel.exportarTabla(tablaMovimientos, "Auditoria_Facturacion");
        });
        
        cmbFiltroSucursal.addActionListener(e -> aplicarFiltrosCruzados());
        java.awt.event.KeyAdapter fechaKeyAdapter = new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                aplicarFiltrosCruzados();
            }
        };
        txtFechaDesde.addKeyListener(fechaKeyAdapter);
        txtFechaHasta.addKeyListener(fechaKeyAdapter);
        btnLimpiarFechas.addActionListener(e -> {
            txtFechaDesde.setValue(null);
            txtFechaHasta.setValue(null);
            aplicarFiltrosCruzados();
        });
        tablaMovimientos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarDetalleMovimiento();
            }
        });
        txtBuscadorGlobal.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                aplicarFiltrosCruzados();
            }
        });
    }

    public void cargarSucursalesEnCombo() {
        cmbFiltroSucursal.removeAllItems();
        cmbFiltroSucursal.addItem(new SucursalFiltroItem(0, "Todas las Sucursales"));
        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_sucursal, nombre FROM sucursales ORDER BY nombre")) {
            while (rs.next()) {
                cmbFiltroSucursal.addItem(new SucursalFiltroItem(rs.getInt("id_sucursal"), rs.getString("nombre")));
            }
        } catch (SQLException ex) {
            System.err.println("Error cargando sucursales para facturación: " + ex.getMessage());
        }
    }

    private void cargarMovimientosFacturacion() {
        modelMovimientos.setRowCount(0);
        modelDetalle.setRowCount(0);
        String sql = "SELECT p.id_pedido, p.fecha_pedido, 'VENTA' AS tipo, s.nombre AS sucursal, " +
                     "u.nombre_completo AS vendedor, c.nombre AS cliente, p.total_pedido " +
                     "FROM pedidos p " + 
                     "JOIN sucursales s ON p.id_sucursal = s.id_sucursal " + 
                     "JOIN usuarios u ON p.id_usuario = u.id_usuario " + 
                     "LEFT JOIN cliente c ON p.id_cliente = c.id_cliente " +
                     "WHERE p.estado_entrega NOT IN ('Devuelto', 'Cancelado') " + // no se facturan ventas anuladas
                     "ORDER BY p.fecha_pedido DESC";
        try (Connection conn = ConexionDB.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("fecha_pedido");
                DateTimeFormatter formatoLatino = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                String fechaFormateada = (ts != null) ? ts.toLocalDateTime().format(formatoLatino) : "";
                modelMovimientos.addRow(new Object[]{
                    rs.getInt("id_pedido"),
                    fechaFormateada,
                    rs.getString("tipo"),
                    rs.getString("sucursal"),
                    rs.getString("vendedor"),
                    rs.getString("cliente") != null ? rs.getString("cliente") : "Consumidor Final",
                    rs.getDouble("total_pedido")
                });
            }
            aplicarFiltrosCruzados();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al procesar la facturación: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDetalleMovimiento() {
        modelDetalle.setRowCount(0);
        int fila = tablaMovimientos.getSelectedRow();
        if (fila == -1) return;
        int modelRow = tablaMovimientos.convertRowIndexToModel(fila);
        int idPedido = (int) modelMovimientos.getValueAt(modelRow, 0);
        String sql = "SELECT pr.sku, pr.nombre, dp.cantidad, dp.precio_unitario, (dp.cantidad * dp.precio_unitario) AS subtotal " +
                     "FROM pedido_detalle dp " + 
                     "JOIN producto pr ON dp.sku_producto = pr.sku " +
                     "WHERE dp.id_pedido = ?";
        try (Connection conn = ConexionDB.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idPedido);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    modelDetalle.addRow(new Object[]{
                        rs.getString("sku"),
                        rs.getString("nombre"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio_unitario"),
                        rs.getDouble("subtotal")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al cargar detalles de la factura #" + idPedido);
        }
    }

    private void aplicarFiltrosCruzados() {
    java.util.List<RowFilter<Object, Object>> filtros = new java.util.ArrayList<>();
    
    // Filtro por Sucursal
    if (cmbFiltroSucursal != null && cmbFiltroSucursal.getSelectedIndex() > 0) {
        String sucursalSeleccionada = cmbFiltroSucursal.getSelectedItem().toString().trim();
        if (!sucursalSeleccionada.isEmpty()) {
            filtros.add(RowFilter.regexFilter("(?i)^" + java.util.regex.Pattern.quote(sucursalSeleccionada) + "$", 3));
        }
    }
    
    // Filtro del Buscador Global (ignora tildes, acentos y mayúsculas/minúsculas)
    if (txtBuscadorGlobal != null) {
        String textoBusqueda = normalizarTexto(txtBuscadorGlobal.getText().trim());
        if (!textoBusqueda.isEmpty()) {
            filtros.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        String valorCelda = entry.getStringValue(i);
                        String celdaNormalizada = normalizarTexto(valorCelda);
                        if (celdaNormalizada.contains(textoBusqueda)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }
    
    // Filtro por Rango de Fechas
    if (txtFechaDesde != null && txtFechaHasta != null) {
        String desdeStr = txtFechaDesde.getText().trim();
        String hastaStr = txtFechaHasta.getText().trim();
        boolean tieneDesde = !desdeStr.contains("_") && desdeStr.length() == 10;
        boolean tieneHasta = !hastaStr.contains("_") && hastaStr.length() == 10;
        if (tieneDesde || tieneHasta) {
            filtros.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    try {
                        String fechaColumnaStr = entry.getStringValue(1);
                        if (fechaColumnaStr == null || fechaColumnaStr.isEmpty()) return false;
                        String soloFechaColumna = fechaColumnaStr.split(" ")[0];
                        LocalDate fechaFila = LocalDate.parse(soloFechaColumna, DATE_PARSE_FORMAT);
                        if (tieneDesde) {
                            LocalDate fechaDesde = LocalDate.parse(desdeStr, DATE_PARSE_FORMAT);
                            if (fechaFila.isBefore(fechaDesde)) return false;
                        }
                        if (tieneHasta) {
                            LocalDate fechaHasta = LocalDate.parse(hastaStr, DATE_PARSE_FORMAT);
                            if (fechaFila.isAfter(fechaHasta)) return false;
                        }
                        return true;
                    } catch (Exception ex) {
                        return false;
                    }
                }
            });
        }
    }
    
    // Aplicar la lista combinada de filtros
    if (sorter != null) {
        if (filtros.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filtros));
        }
    }
    
    recalcularTotalCajaVisual();
}

    private void recalcularTotalCajaVisual() {
        double totalSuma = 0.0;
        int filasVisibles = tablaMovimientos.getRowCount();
        for (int i = 0; i < filasVisibles; i++) {
            // Se lee del modelo (columna fija "Total") aunque el usuario reordene las columnas
            int filaModelo = tablaMovimientos.convertRowIndexToModel(i);
            totalSuma += ((Number) modelMovimientos.getValueAt(filaModelo, 6)).doubleValue();
        }
        lblTotalFacturado.setText(String.format("Total Facturado en Vista: $%.2f", totalSuma));
    }

    private static String normalizarTexto(String texto) {
    if (texto == null) return "";
    String temp = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD);
    return temp.replaceAll("\\p{M}", "").toLowerCase();
}
}