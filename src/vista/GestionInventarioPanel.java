package vista;
import modelo.MovimientoStock;
import modelo.Stock_SucursalDTO;
import modelo.Usuario;
import modelo.SesionUsuario;
import persistencia.MovimientoStockDAO;
import persistencia.Stock_SucursalDAOImpl;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class GestionInventarioPanel extends JPanel {

    // Variables y Constructor

    private JComboBox<SucursalItem> comboSucursal;
    private JTextField txtSku, txtNombre, txtStockActual, txtBuscador;
    private JButton btnActualizarStock, btnRefrescar;
    private JTable tablaStock;
    private DefaultTableModel tableModel;
    private JPanel pGestion; 
    private final MovimientoStockDAO movimientoDAO = new MovimientoStockDAO();
    private final Stock_SucursalDAOImpl stockDAO = new Stock_SucursalDAOImpl();

    public GestionInventarioPanel() {
        setLayout(new BorderLayout(15, 15));
        JPanel panelNorte = new JPanel(new BorderLayout());
        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltro.add(new JLabel("Seleccionar sucursal:"));
        comboSucursal = new JComboBox<>();
        cargarComboSucursales();
        panelFiltro.add(comboSucursal);
        btnRefrescar = new JButton("🔄 Refrescar");
        panelNorte.add(panelFiltro, BorderLayout.WEST);
        panelNorte.add(btnRefrescar, BorderLayout.EAST);
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

    private void cargarComboSucursales() {
        comboSucursal.removeAllItems();
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
            System.err.println("Error al cargar combos: " + e.getMessage());
        }
    }

    private void cargarDatosInventario() {
        tableModel.setRowCount(0);
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
            txtBuscador.setText("");
            cargarDatosInventario();
        });
    }

    private void buscarProductoGlobal(String criterio) {
        tableModel.setRowCount(0);
        try {
            List<Stock_SucursalDTO> resultados = stockDAO.buscarStockGlobal(criterio);
            for (Stock_SucursalDTO s : resultados) {
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
        String[] columnas = {"SKU", "Producto", "Stock actual", "Ubicación/Mín.", "Estado"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaStock = new JTable(tableModel);
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
            int stockAnterior = Integer.parseInt(tableModel.getValueAt(modelRow, 2).toString());
            int stockDestino = Integer.parseInt(txtStockActual.getText().trim());
            if (stockDestino < 0) {
                JOptionPane.showMessageDialog(this, "El inventario físico no puede ser menor a 0.");
                return;
            }
            int diferenciaCalculada = stockDestino - stockAnterior;
            if (diferenciaCalculada == 0) {
                JOptionPane.showMessageDialog(this, "El stock ingresado es idéntico al actual.");
                return;
            }
            MovimientoStock mov = new MovimientoStock();
            mov.setSkuProducto(txtSku.getText());
            mov.setCantidad(diferenciaCalculada);
            mov.setFecha(LocalDateTime.now());
            mov.setTipo(diferenciaCalculada > 0 ? "INGRESO" : "EGRESO");
            mov.setIdSucursal(((SucursalItem)comboSucursal.getSelectedItem()).id);
            if (movimientoDAO.registrarIngreso(mov)) {
                JOptionPane.showMessageDialog(this, "¡Inventario reajustado con éxito!");
                cargarDatosInventario();
                txtSku.setText("");
                txtNombre.setText("");
                txtStockActual.setText("");
            }
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