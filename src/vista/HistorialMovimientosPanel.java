package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import util.ExporterExcel;

public class HistorialMovimientosPanel extends JPanel {

    private JComboBox<SucursalItem> comboSucursal;
    private JComboBox<String> comboTipoMovimiento;
    private JSpinner spinFechaDesde, spinFechaHasta;
    private JCheckBox chkUsarFechas;
    private JTextField txtBuscador;
    private JButton btnRefrescar, btnExportarExcel;
    private JTable tablaMovimientos;
    private DefaultTableModel tableModel;

    // Métricas del pie de página
    private JLabel lblTotalRegistros;
    private JLabel lblBalanceVariacion;
    private JLabel lblStockActualSucursal;

    public HistorialMovimientosPanel() {
        setLayout(new BorderLayout(15, 15));

        // Panel Superior: Filtros
        JPanel panelNorte = new JPanel(new BorderLayout());
        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));

        panelFiltro.add(new JLabel("Sucursal:"));
        comboSucursal = new JComboBox<>();
        cargarComboSucursales();
        panelFiltro.add(comboSucursal);

        panelFiltro.add(new JLabel("Tipo:"));
        comboTipoMovimiento = new JComboBox<>(new String[]{
            "TODOS", "INGRESO", "EGRESO", "VENTA", "DEVOLUCION", "AJUSTE"
        });
        panelFiltro.add(comboTipoMovimiento);

        // Filtro por Fecha
        chkUsarFechas = new JCheckBox("Fechas:");
        chkUsarFechas.setSelected(false);
        panelFiltro.add(chkUsarFechas);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1); // Por defecto un mes atrás
        
        spinFechaDesde = new JSpinner(new SpinnerDateModel(cal.getTime(), null, null, Calendar.DAY_OF_MONTH));
        spinFechaDesde.setEditor(new JSpinner.DateEditor(spinFechaDesde, "dd/MM/yyyy"));
        spinFechaDesde.setEnabled(false);

        spinFechaHasta = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spinFechaHasta.setEditor(new JSpinner.DateEditor(spinFechaHasta, "dd/MM/yyyy"));
        spinFechaHasta.setEnabled(false);

        panelFiltro.add(spinFechaDesde);
        panelFiltro.add(new JLabel("-"));
        panelFiltro.add(spinFechaHasta);

        JPanel panelAccionesNorte = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnRefrescar = new JButton("🔄 Refrescar");
        btnExportarExcel = new JButton("Exportar a Excel");
        panelAccionesNorte.add(btnRefrescar);
        panelAccionesNorte.add(btnExportarExcel);

        panelNorte.add(panelFiltro, BorderLayout.WEST);
        panelNorte.add(panelAccionesNorte, BorderLayout.EAST);

        // Configuración de Grilla y Buscador
        configurarTabla();
        JPanel panelIzquierdo = crearPanelBuscador();
        JPanel panelSur = crearPanelResumenTotales();

        add(panelNorte, BorderLayout.NORTH);
        add(panelIzquierdo, BorderLayout.WEST);
        add(new JScrollPane(tablaMovimientos), BorderLayout.CENTER);
        add(panelSur, BorderLayout.SOUTH);

        agregarListeners();
        cargarHistorialMovimientos();
    }

    public void cargarComboSucursales() {
        comboSucursal.removeAllItems();
        comboSucursal.addItem(new SucursalItem(0, "Todas las sucursales"));
        String sql = "SELECT id_sucursal, nombre FROM sucursales ORDER BY nombre";
        try (Connection conn = persistencia.ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                comboSucursal.addItem(new SucursalItem(
                    rs.getInt("id_sucursal"), 
                    rs.getString("nombre")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar sucursales: " + e.getMessage());
        }
    }

    private void configurarTabla() {
        String[] columnas = {
            "ID", "Fecha / Hora", "SKU", "Producto", "Sucursal", "Tipo", "Usuario", "Variación", "Stock Tras Cambio"
        };
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaMovimientos = new JTable(tableModel);
        tablaMovimientos.setRowHeight(24);

        tablaMovimientos.getColumnModel().getColumn(0).setPreferredWidth(45);
        tablaMovimientos.getColumnModel().getColumn(1).setPreferredWidth(130);
        tablaMovimientos.getColumnModel().getColumn(3).setPreferredWidth(180);
        
        tablaMovimientos.getColumnModel().getColumn(7).setCellRenderer(new FormatoVariacionRenderer());
        tablaMovimientos.getColumnModel().getColumn(8).setCellRenderer(new RightAlignedCellRenderer());
    }

    private JPanel crearPanelBuscador() {
        JPanel panelContenedor = new JPanel();
        panelContenedor.setLayout(new BoxLayout(panelContenedor, BoxLayout.Y_AXIS));

        JPanel pBusqueda = new JPanel(new GridBagLayout());
        pBusqueda.setBorder(BorderFactory.createTitledBorder("🔍 Búsqueda rápida"));
        GridBagConstraints gbcB = new GridBagConstraints();
        gbcB.insets = new Insets(5, 5, 5, 5); 
        gbcB.fill = GridBagConstraints.HORIZONTAL;

        txtBuscador = new JTextField(15);
        gbcB.gridx = 0; gbcB.gridy = 0;
        pBusqueda.add(new JLabel("Producto / Usuario / SKU:"), gbcB);
        gbcB.gridy = 1;
        pBusqueda.add(txtBuscador, gbcB);

        panelContenedor.add(pBusqueda);
        panelContenedor.add(Box.createVerticalGlue());
        return panelContenedor;
    }

    private JPanel crearPanelResumenTotales() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 8));
    panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));

    Font fontBold = new Font("SansSerif", Font.BOLD, 12);

    lblTotalRegistros = new JLabel("Movimientos: 0");
    lblTotalRegistros.setFont(fontBold);

    lblBalanceVariacion = new JLabel("Balance Neto: 0 u.");
    lblBalanceVariacion.setFont(fontBold);

    panel.add(lblTotalRegistros);
    panel.add(new JLabel("|"));
    panel.add(lblBalanceVariacion);

    return panel;
}

    private void agregarListeners() {
        comboSucursal.addActionListener(e -> cargarHistorialMovimientos());
        comboTipoMovimiento.addActionListener(e -> cargarHistorialMovimientos());

        chkUsarFechas.addActionListener(e -> {
            boolean habilitar = chkUsarFechas.isSelected();
            spinFechaDesde.setEnabled(habilitar);
            spinFechaHasta.setEnabled(habilitar);
            cargarHistorialMovimientos();
        });

        spinFechaDesde.addChangeListener(e -> { if(chkUsarFechas.isSelected()) cargarHistorialMovimientos(); });
        spinFechaHasta.addChangeListener(e -> { if(chkUsarFechas.isSelected()) cargarHistorialMovimientos(); });

        txtBuscador.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { cargarHistorialMovimientos(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { cargarHistorialMovimientos(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { cargarHistorialMovimientos(); }
        });

        btnRefrescar.addActionListener(e -> cargarHistorialMovimientos());

        btnExportarExcel.addActionListener(e -> {
            if (tablaMovimientos.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ExporterExcel.exportarTabla(tablaMovimientos, "Historial_Movimientos_Stock");
        });
    }

    public void cargarHistorialMovimientos() {
    tableModel.setRowCount(0);
    SucursalItem suc = (SucursalItem) comboSucursal.getSelectedItem();
    String tipoFiltro = (String) comboTipoMovimiento.getSelectedItem();
    String busqueda = txtBuscador.getText().trim().toLowerCase();

    // Se ajustó la consulta del nombre del usuario con COALESCE y NULLIF
    StringBuilder sql = new StringBuilder(
    "SELECT m.id_movimiento, m.fecha, m.sku_producto, p.nombre AS producto_nombre, " +
    "s.nombre AS sucursal_nombre, m.tipo, " +
    "COALESCE(NULLIF(u.nombre_completo, ''), u.nombre_usuario, 'Sistema') AS usuario_nombre, " +
    "m.cantidad, " +
    // Se utiliza ss.cantidad_disponible como stock actual base
    "COALESCE(NULLIF(m.stock_resultante, 0), " +
    "  COALESCE(ss.cantidad_disponible, 0) - COALESCE(SUM(m.cantidad) OVER ( " +
    "    PARTITION BY m.sku_producto, m.id_sucursal " +
    "    ORDER BY m.fecha DESC, m.id_movimiento DESC " +
    "    ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING " +
    "  ), 0) " +
    ") AS stock_resultante " +
    "FROM movimiento_stock m " +
    "LEFT JOIN producto p ON m.sku_producto = p.sku " +
    "LEFT JOIN sucursales s ON m.id_sucursal = s.id_sucursal " +
    "LEFT JOIN usuarios u ON m.id_usuario = u.id_usuario " +
    "LEFT JOIN stock_sucursal ss ON m.sku_producto = ss.sku_producto AND m.id_sucursal = ss.id_sucursal " +
    "WHERE 1=1 "
);

    List<Object> parametros = new ArrayList<>();

    // Filtro de Sucursal
    if (suc != null && suc.id > 0) {
        sql.append("AND m.id_sucursal = ? ");
        parametros.add(suc.id);
    }

    if (tipoFiltro != null && !"TODOS".equalsIgnoreCase(tipoFiltro)) {
        sql.append("AND UPPER(m.tipo) = ? ");
        parametros.add(tipoFiltro.toUpperCase());
    }

    // Filtro por Fechas opcional
    if (chkUsarFechas.isSelected()) {
        Date dateD = (Date) spinFechaDesde.getValue();
        Date dateH = (Date) spinFechaHasta.getValue();

        LocalDate ldDesde = dateD.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate ldHasta = dateH.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        sql.append("AND m.fecha >= ? AND m.fecha <= ? ");
        parametros.add(Timestamp.valueOf(ldDesde.atStartOfDay()));
        parametros.add(Timestamp.valueOf(ldHasta.atTime(23, 59, 59)));
    }

    // Filtro de Texto (incluye nombre_usuario en la búsqueda)
    if (!busqueda.isEmpty()) {
        sql.append("AND (LOWER(p.nombre) LIKE ? OR LOWER(m.sku_producto) LIKE ? OR LOWER(u.nombre_completo) LIKE ? OR LOWER(u.nombre_usuario) LIKE ?) ");
        String filtroLike = "%" + busqueda + "%";
        parametros.add(filtroLike);
        parametros.add(filtroLike);
        parametros.add(filtroLike);
        parametros.add(filtroLike);
    }

    sql.append("ORDER BY m.fecha DESC, m.id_movimiento DESC");

    int totalMovimientos = 0;
    int balanceNeto = 0;

    try (Connection conn = persistencia.ConexionDB.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql.toString())) {

        for (int i = 0; i < parametros.size(); i++) {
            ps.setObject(i + 1, parametros.get(i));
        }

        try (ResultSet rs = ps.executeQuery()) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            while (rs.next()) {
                int cantidad = rs.getInt("cantidad");
                String variacion = (cantidad > 0 ? "+" : "") + cantidad;
                String prodNombre = rs.getString("producto_nombre");
                String sucNombre = rs.getString("sucursal_nombre");
                String usrNombre = rs.getString("usuario_nombre");
                
                // Manejo de valores NULL históricos
                int stockResultante = rs.getInt("stock_resultante");
                boolean eraNullStock = rs.wasNull();

                totalMovimientos++;
                balanceNeto += cantidad;

                tableModel.addRow(new Object[]{
                    rs.getInt("id_movimiento"),
                    rs.getTimestamp("fecha") != null ? rs.getTimestamp("fecha").toLocalDateTime().format(fmt) : "-",
                    rs.getString("sku_producto"),
                    prodNombre != null ? prodNombre : "Sin Nombre",
                    sucNombre != null ? sucNombre : "Sin Asignar",
                    rs.getString("tipo"),
                    usrNombre,
                    variacion,
                    eraNullStock ? "N/D" : stockResultante
                });
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar movimientos: " + e.getMessage(), "Error DB", JOptionPane.ERROR_MESSAGE);
    }

    // Actualizar datos del footer
    lblTotalRegistros.setText("Movimientos: " + totalMovimientos);
    lblBalanceVariacion.setText("Balance Neto: " + (balanceNeto > 0 ? "+" : "") + balanceNeto + " u.");

    calcularStockActualSucursal(suc != null ? suc.id : 0);
}

    private void calcularStockActualSucursal(int idSucursal) {
    
}

    private static class FormatoVariacionRenderer extends DefaultTableCellRenderer {
        public FormatoVariacionRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                String valStr = value.toString();
                if (valStr.startsWith("+")) {
                    c.setForeground(new Color(46, 204, 113));
                } else if (valStr.startsWith("-")) {
                    c.setForeground(new Color(231, 76, 60));
                } else {
                    c.setForeground(table.getForeground());
                }
            }
            return c;
        }
    }

    private static class RightAlignedCellRenderer extends DefaultTableCellRenderer {
        public RightAlignedCellRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
    }

    private static class SucursalItem {
        int id; String nombre;
        SucursalItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
}