package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import modelo.SesionUsuario;
import modelo.Stock_SucursalDTO;
import modelo.Usuario;
import persistencia.MovimientoStockDAO;
import persistencia.Stock_SucursalDAOImpl;
import util.ExporterExcel;

public class GestionInventarioPanel extends JPanel {

    // Variables y Constructor

    private JComboBox<SucursalItem> comboSucursal;
    private JTextField txtSku, txtNombre, txtStockActual, txtBuscador;
    private JButton btnActualizarStock, btnRefrescar, btnExportarExcel;
    private JTable tablaStock;
    private DefaultTableModel tableModel;
    private JPanel pGestion; 
    private final MovimientoStockDAO movimientoDAO = new MovimientoStockDAO();
    private final Stock_SucursalDAOImpl stockDAO = new Stock_SucursalDAOImpl();
    private List<Stock_SucursalDTO> resultadosBusquedaActual;

    public GestionInventarioPanel() {
        setLayout(new BorderLayout(15, 15));
        JPanel panelNorte = new JPanel(new BorderLayout());
        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltro.add(new JLabel("Seleccionar sucursal:"));
        comboSucursal = new JComboBox<>();
        cargarComboSucursales();
        panelFiltro.add(comboSucursal);
        
        JPanel panelAccionesNorte = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnRefrescar = new JButton("🔄 Refrescar");
        btnExportarExcel = new JButton("Exportar a Excel");
        panelAccionesNorte.add(btnRefrescar);
        panelAccionesNorte.add(btnExportarExcel);

        panelNorte.add(panelFiltro, BorderLayout.WEST);
        panelNorte.add(panelAccionesNorte, BorderLayout.EAST);
        
        configurarTabla();
        JPanel panelIzquierdo = crearPanelFormulario(); 
        add(panelNorte, BorderLayout.NORTH);
        add(panelIzquierdo, BorderLayout.WEST); 
        add(new JScrollPane(tablaStock), BorderLayout.CENTER);
        agregarListeners();
        cargarDatosInventario();
        aplicarPermisos();
    }

    private void aplicarPermisos() {
        Usuario actual = SesionUsuario.getUsuario();
        if (actual == null) return;
        String rol = actual.getRol();
        if ("VENDEDOR".equalsIgnoreCase(rol)) {
            pGestion.setVisible(false);
            this.revalidate();
            this.repaint();
            System.out.println("DEBUG: Aplicando restricciones de Vendedor a " + actual.getNombre());
        } else {
            System.out.println("DEBUG: Acceso total para el rol: " + rol);
        }
    }

    public void cargarComboSucursales() {
        comboSucursal.removeAllItems();
        String sql = "SELECT id_sucursal, nombre FROM sucursales WHERE activa = 1 ORDER BY nombre";
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
            System.err.println("Error al cargar combos: " + e.getMessage());
        }
    }

    // Recarga respetando la vista actual: búsqueda global activa o sucursal del combo
    void refrescar() {
        String texto = txtBuscador.getText().trim();
        if (texto.length() >= 3) {
            buscarProductoGlobal(texto);
        } else {
            cargarDatosInventario();
        }
    }

    private void cargarDatosInventario() {
        tableModel.setRowCount(0);
        resultadosBusquedaActual = null; // la grilla pasa a mostrar la sucursal del combo
        SucursalItem suc = (SucursalItem) comboSucursal.getSelectedItem();
        if (suc == null) return;
        try {
            List<Stock_SucursalDTO> lista = stockDAO.listarStockPorSucursal(suc.id);
            for (Stock_SucursalDTO s : lista) {
                int stockMinimo = s.getStockMinimo(); 
                String estado = (s.getCantidadDisponible() <= stockMinimo) ? "⚠️ BAJO" : "✅ OK";
                tableModel.addRow(new Object[]{
                    s.getSkuProducto(), 
                    s.getNombreProducto(), 
                    s.getCantidadDisponible(), 
                    stockMinimo,
                    estado
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void agregarListeners() {
        comboSucursal.addActionListener(e -> {
            txtBuscador.setText(""); 
            cargarDatosInventario();
        });

        tablaStock.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaStock.getSelectedRow();
                if (fila != -1) {
                    int modelRow = tablaStock.convertRowIndexToModel(fila);
                    txtSku.setText(tableModel.getValueAt(modelRow, 0).toString());
                    txtNombre.setText(tableModel.getValueAt(modelRow, 1).toString());
                    txtStockActual.setText(tableModel.getValueAt(modelRow, 2).toString()); 
                }
            }
        });

        txtBuscador.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { accionarFiltro(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { accionarFiltro(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { accionarFiltro(); }
            private void accionarFiltro() {
                String texto = txtBuscador.getText().trim();
                if (texto.length() >= 3) {
                    buscarProductoGlobal(texto);
                } else if (texto.isEmpty()) {
                    cargarDatosInventario();
                }
            }
        });

        btnActualizarStock.addActionListener(e -> ejecutarMovimiento());

        btnRefrescar.addActionListener(e -> {
            String texto = txtBuscador.getText().trim();
            if (texto.length() >= 3) {
                buscarProductoGlobal(texto);
            } else {
                cargarDatosInventario();
            }
        });

        btnExportarExcel.addActionListener(e -> {
            if (tablaStock.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay datos en la tabla para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ExporterExcel.exportarTabla(tablaStock, "Inventario_Stock");
        });
    }

    private void buscarProductoGlobal(String criterio) {
        tableModel.setRowCount(0);
        try {
            resultadosBusquedaActual = stockDAO.buscarStockGlobal(criterio);
            for (Stock_SucursalDTO s : resultadosBusquedaActual) {
                String estado = (s.getCantidadDisponible() <= 5) ? "⚠️ BAJO" : "✅ OK";
                tableModel.addRow(new Object[]{
                    s.getSkuProducto(), 
                    s.getNombreProducto(), 
                    s.getCantidadDisponible(), 
                    s.getNombreSucursal(),
                    estado
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error en búsqueda: " + e.getMessage());
        }
    }

    private JPanel crearPanelFormulario() {
        JPanel panelContenedor = new JPanel();
        panelContenedor.setLayout(new BoxLayout(panelContenedor, BoxLayout.Y_AXIS));
        JPanel pBusqueda = new JPanel(new GridBagLayout());
        pBusqueda.setBorder(BorderFactory.createTitledBorder("🔍 Búsqueda rápida"));
        GridBagConstraints gbcB = new GridBagConstraints();
        gbcB.insets = new Insets(5,5,5,5); gbcB.fill = GridBagConstraints.HORIZONTAL;
        txtBuscador = new JTextField(15);
        gbcB.gridx = 0; gbcB.gridy = 0;
        pBusqueda.add(new JLabel("Producto:"), gbcB);
        gbcB.gridy = 1;
        pBusqueda.add(txtBuscador, gbcB);
        pGestion = new JPanel(new GridBagLayout());
        pGestion.setBorder(BorderFactory.createTitledBorder("Gestión de mercadería"));
        GridBagConstraints gbcG = new GridBagConstraints();
        gbcG.insets = new Insets(8,8,8,8); gbcG.fill = GridBagConstraints.HORIZONTAL;
        txtSku = new JTextField(12); txtSku.setEditable(false);
        txtNombre = new JTextField(12); txtNombre.setEditable(false);
        txtStockActual = new JTextField(12);
        txtStockActual.setHorizontalAlignment(JTextField.RIGHT);
        btnActualizarStock = new JButton("Actualizar Stock");
        btnActualizarStock.setBackground(new Color(41, 128, 185));
        btnActualizarStock.setForeground(Color.WHITE);
        gbcG.gridx = 0; gbcG.gridy = 0; pGestion.add(new JLabel("SKU:"), gbcG);
        gbcG.gridx = 1; pGestion.add(txtSku, gbcG);
        gbcG.gridx = 0; gbcG.gridy = 1; pGestion.add(new JLabel("Producto:"), gbcG);
        gbcG.gridx = 1; pGestion.add(txtNombre, gbcG);
        gbcG.gridx = 0; gbcG.gridy = 2; pGestion.add(new JLabel("Nuevo Stock Fijo:"), gbcG);
        gbcG.gridx = 1; pGestion.add(txtStockActual, gbcG);
        gbcG.gridx = 0; gbcG.gridy = 3; gbcG.gridwidth = 2; pGestion.add(btnActualizarStock, gbcG);
        panelContenedor.add(pBusqueda);
        panelContenedor.add(Box.createVerticalStrut(10));
        panelContenedor.add(pGestion);
        return panelContenedor;
    }

    private void configurarTabla() {
        String[] columnas = {"SKU", "Producto", "Stock actual", "Mínimo", "Estado"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaStock = new JTable(tableModel);
        tablaStock.setRowHeight(23);
        tablaStock.getColumnModel().getColumn(2).setCellRenderer(new RightAlignedCellRenderer());
        tablaStock.getColumnModel().getColumn(3).setCellRenderer(new RightAlignedCellRenderer());
    }

    private void ejecutarMovimiento() {
        if ("VENDEDOR".equalsIgnoreCase(SesionUsuario.getUsuario().getRol())) {
            JOptionPane.showMessageDialog(this, "Acción no permitida para vendedores.");
            return;
        }
        try {
            if (txtSku.getText().isEmpty() || txtStockActual.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione un producto e ingrese cantidad.");
                return;
            }
            int filaSeleccionada = tablaStock.getSelectedRow();
            if (filaSeleccionada == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione una fila válida de la tabla.");
                return;
            }
            int modelRow = tablaStock.convertRowIndexToModel(filaSeleccionada);
            int stockDestino = Integer.parseInt(txtStockActual.getText().trim());
            if (stockDestino < 0) {
                JOptionPane.showMessageDialog(this, "El inventario físico no puede ser menor a 0.");
                return;
            }

            // La sucursal sale de lo que muestra la grilla: resultado de búsqueda global o sucursal del combo
            String sku = tableModel.getValueAt(modelRow, 0).toString();
            int idSucursalDestino;
            if (resultadosBusquedaActual != null && modelRow < resultadosBusquedaActual.size()) {
                idSucursalDestino = resultadosBusquedaActual.get(modelRow).getIdSucursal();
            } else {
                idSucursalDestino = ((SucursalItem) comboSucursal.getSelectedItem()).id;
            }

            int diferencia = movimientoDAO.ajustarAStockFijo(sku, idSucursalDestino, stockDestino);
            if (diferencia == 0) {
                JOptionPane.showMessageDialog(this, "El stock ingresado es idéntico al actual.");
                return;
            }
            JOptionPane.showMessageDialog(this, "¡Inventario reajustado con éxito! (" + (diferencia > 0 ? "+" : "") + diferencia + " unidades)");

            // Refrescar manteniendo la vista actual (búsqueda o sucursal)
            String busqueda = txtBuscador.getText().trim();
            if (resultadosBusquedaActual != null && busqueda.length() >= 3) {
                buscarProductoGlobal(busqueda);
            } else {
                cargarDatosInventario();
            }
            txtSku.setText("");
            txtNombre.setText("");
            txtStockActual.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un número entero válido.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private static class SucursalItem {
        int id; String nombre;
        SucursalItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
}