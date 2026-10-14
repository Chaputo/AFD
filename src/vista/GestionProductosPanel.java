package vista;

import modelo.Producto;
import persistencia.ProductoDAO;
import util.ExporterExcel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.List;

public class GestionProductosPanel extends JPanel {

    private JTextField txtSku, txtNombre, txtPrecio, txtStockMinimo, txtUnidadMedida, txtBuscar;
    private JButton btnGuardar, btnEliminar, btnLimpiar, btnExportarExcel, btnRecargar;
    private JTable tablaProductos;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    
    private ProductoDAO productoDAO;

    public GestionProductosPanel() {
        productoDAO = new ProductoDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        inicializarComponentes();

        // Modelo y Tabla de Productos
        tableModel = new DefaultTableModel(
            new Object[]{"SKU", "Nombre", "Precio", "Stock mín.", "Unidad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Double.class;
                if (columnIndex == 3) return Integer.class;
                return String.class;
            }
        };

        tablaProductos = new JTable(tableModel);
        tablaProductos.setRowHeight(24);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.getTableHeader().setReorderingAllowed(false);

        // Renderizadores personalizados del proyecto
        tablaProductos.getColumnModel().getColumn(2).setCellRenderer(new CurrencyCellRenderer());
        tablaProductos.getColumnModel().getColumn(3).setCellRenderer(new RightAlignedCellRenderer());

        // TableRowSorter para filtrado rápido
        sorter = new TableRowSorter<>(tableModel);
        tablaProductos.setRowSorter(sorter);

        // Ancho de columna SKU (ajustado para dar espacio suficiente a códigos alfanuméricos largos)
        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(140);
        tablaProductos.getColumnModel().getColumn(0).setMinWidth(110);
        tablaProductos.getColumnModel().getColumn(0).setMaxWidth(180);

        JScrollPane scrollTabla = new JScrollPane(tablaProductos);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Catálogo de productos registrados"));

        // Panel cuerpo principal
        JPanel panelCuerpo = new JPanel(new BorderLayout(10, 0));
        JPanel panelIzquierdo = crearPanelLateralIzquierdo();
        panelIzquierdo.setPreferredSize(new Dimension(380, 0));

        panelCuerpo.add(panelIzquierdo, BorderLayout.WEST);
        panelCuerpo.add(scrollTabla, BorderLayout.CENTER);

        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(panelCuerpo, BorderLayout.CENTER);

        agregarListeners();
        cargarProductos();

        btnEliminar.setEnabled(false);
    }

    private void inicializarComponentes() {
        txtSku = new JTextField();
        txtNombre = new JTextField();
        txtPrecio = new JTextField();
        txtStockMinimo = new JTextField();
        txtUnidadMedida = new JTextField();
        txtBuscar = new JTextField();

        txtPrecio.setHorizontalAlignment(JTextField.RIGHT);
        txtStockMinimo.setHorizontalAlignment(JTextField.RIGHT);

        Dimension btnSize = new Dimension(160, 32);

        btnGuardar = new JButton("Guardar / Actualizar");
        btnLimpiar = new JButton("Limpiar / Nuevo");
        btnEliminar = new JButton("Eliminar Producto");
        btnRecargar = new JButton("↻ Refrescar");
        btnExportarExcel = new JButton("Exportar a Excel");

        btnGuardar.setPreferredSize(btnSize);
        btnLimpiar.setPreferredSize(btnSize);
        btnEliminar.setPreferredSize(btnSize);
        btnRecargar.setPreferredSize(btnSize);
        btnExportarExcel.setPreferredSize(btnSize);
    }

    private JPanel crearBarraSuperior() {
        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelNorte.add(btnRecargar);
        panelNorte.add(btnExportarExcel);
        return panelNorte;
    }

    private JPanel crearPanelLateralIzquierdo() {
        JPanel panelIzquierdo = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        Font labelFont = new Font("SansSerif", Font.PLAIN, 12);

        // --- SECCIÓN 1: BÚSQUEDA RÁPIDA ---
        JPanel panelBusqueda = new JPanel(new GridBagLayout());
        panelBusqueda.setBorder(BorderFactory.createTitledBorder("🔍 Búsqueda rápida"));
        GridBagConstraints gbcB = new GridBagConstraints();
        gbcB.insets = new Insets(4, 6, 4, 6);
        gbcB.fill = GridBagConstraints.HORIZONTAL;
        gbcB.weightx = 1.0;

        gbcB.gridx = 0; gbcB.gridy = 0;
        JLabel lblBuscar = new JLabel("SKU / Nombre / Unidad:");
        lblBuscar.setFont(labelFont);
        panelBusqueda.add(lblBuscar, gbcB);

        gbcB.gridy = 1;
        panelBusqueda.add(txtBuscar, gbcB);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weighty = 0.0;
        panelIzquierdo.add(panelBusqueda, gbc);

        // --- SECCIÓN 2: DEFINICIÓN DE PRODUCTO (MAESTRO) ---
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder("Definición de producto (maestro)"));
        GridBagConstraints gbcF = new GridBagConstraints();
        gbcF.insets = new Insets(4, 6, 4, 6);
        gbcF.fill = GridBagConstraints.HORIZONTAL;
        gbcF.weightx = 1.0;

        // SKU
        gbcF.gridx = 0; gbcF.gridy = 0;
        JLabel lblSku = new JLabel("SKU (Código):");
        lblSku.setFont(labelFont);
        panelForm.add(lblSku, gbcF);

        gbcF.gridy = 1;
        panelForm.add(txtSku, gbcF);

        // Nombre
        gbcF.gridy = 2;
        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setFont(labelFont);
        panelForm.add(lblNombre, gbcF);

        gbcF.gridy = 3;
        panelForm.add(txtNombre, gbcF);

        // Precio
        gbcF.gridy = 4;
        JLabel lblPrecio = new JLabel("Precio Venta ($):");
        lblPrecio.setFont(labelFont);
        panelForm.add(lblPrecio, gbcF);

        gbcF.gridy = 5;
        panelForm.add(txtPrecio, gbcF);

        // Stock Mínimo
        gbcF.gridy = 6;
        JLabel lblStock = new JLabel("Stock Mínimo:");
        lblStock.setFont(labelFont);
        panelForm.add(lblStock, gbcF);

        gbcF.gridy = 7;
        panelForm.add(txtStockMinimo, gbcF);

        // Unidad de Medida
        gbcF.gridy = 8;
        JLabel lblUnidad = new JLabel("Unidad de Medida:");
        lblUnidad.setFont(labelFont);
        panelForm.add(lblUnidad, gbcF);

        gbcF.gridy = 9;
        panelForm.add(txtUnidadMedida, gbcF);

        gbc.gridy = 1;
        panelIzquierdo.add(panelForm, gbc);

        // --- ESPACIADOR VERTICAL ---
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        panelIzquierdo.add(Box.createGlue(), gbc);

        // --- BOTONES INFERIORES ---
        gbc.weighty = 0.0;

        gbc.gridy = 3;
        panelIzquierdo.add(btnGuardar, gbc);

        gbc.gridy = 4;
        panelIzquierdo.add(btnLimpiar, gbc);

        gbc.gridy = 5;
        panelIzquierdo.add(btnEliminar, gbc);

        return panelIzquierdo;
    }

    private void agregarListeners() {
        btnGuardar.addActionListener(e -> guardarOActualizarProducto());
        btnEliminar.addActionListener(e -> eliminarProducto());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnRecargar.addActionListener(e -> cargarProductos());
        btnExportarExcel.addActionListener(e -> exportarProductosExcel());

        // Filtrado dinámico tolerante a acentos
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filtrar(); }

            private void filtrar() {
                String busqueda = normalizarTexto(txtBuscar.getText());

                if (busqueda.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                        @Override
                        public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                            for (int i = 0; i < entry.getValueCount(); i++) {
                                String valorCelda = entry.getStringValue(i);
                                String valorNormalizado = normalizarTexto(valorCelda);
                                if (valorNormalizado.contains(busqueda)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    });
                }
            }
        });

        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaVista = tablaProductos.getSelectedRow();
                if (filaVista != -1) {
                    cargarFilaSeleccionada(filaVista);
                }
            }
        });
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String textoSinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return textoSinAcentos.toLowerCase().trim();
    }

    private void exportarProductosExcel() {
        if (tablaProductos.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay productos en la tabla para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ExporterExcel.exportarTabla(tablaProductos, "Listado_Productos");
    }

    private void limpiarCampos() {
        txtSku.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtStockMinimo.setText("");
        txtUnidadMedida.setText("");
        txtSku.setEditable(true);
        btnEliminar.setEnabled(false);
        tablaProductos.clearSelection();
    }

    private void cargarProductos() {
        tableModel.setRowCount(0);
        try {
            List<Producto> productos = productoDAO.listarTodos();
            for (Producto p : productos) {
                tableModel.addRow(new Object[]{
                    p.getSku(), p.getNombre(), p.getPrecio(),
                    p.getStockMinimo(), p.getUnidadMedida()
                });
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar productos de la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarFilaSeleccionada(int filaVista) {
        int filaModelo = tablaProductos.convertRowIndexToModel(filaVista);
        txtSku.setText(tableModel.getValueAt(filaModelo, 0).toString());
        txtNombre.setText(tableModel.getValueAt(filaModelo, 1).toString());
        txtPrecio.setText(tableModel.getValueAt(filaModelo, 2).toString());
        txtStockMinimo.setText(tableModel.getValueAt(filaModelo, 3).toString());
        
        Object unidad = tableModel.getValueAt(filaModelo, 4);
        txtUnidadMedida.setText(unidad != null ? unidad.toString() : "");

        txtSku.setEditable(false);
        btnEliminar.setEnabled(true);
    }

    // Acepta formato argentino ("1.500,50", "1.500") y también "1500.50"
    private static double parsearPrecio(String texto) {
        String t = texto.trim().replace("$", "").replace(" ", "");
        if (t.contains(",")) {
            t = t.replace(".", "").replace(",", ".");
        } else if (t.matches("\\d{1,3}(\\.\\d{3})+")) {
            t = t.replace(".", "");
        }
        return Double.parseDouble(t);
    }

    private void guardarOActualizarProducto() {
        if (txtSku.getText().isBlank() || txtNombre.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "SKU y Nombre son campos obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String sku = txtSku.getText().trim();
            String nombre = txtNombre.getText().trim();
            double precio = parsearPrecio(txtPrecio.getText());
            int stockMinimo = Integer.parseInt(txtStockMinimo.getText().trim());
            String unidad = txtUnidadMedida.getText().trim();

            if (precio < 0 || stockMinimo < 0) {
                JOptionPane.showMessageDialog(this, "El precio y el stock mínimo no pueden ser negativos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Alta nueva (SKU editable): no pisar un producto existente
            if (txtSku.isEditable() && productoDAO.obtenerPorSku(sku) != null) {
                JOptionPane.showMessageDialog(this, "Ya existe un producto con el SKU '" + sku + "'.\nSeleccionalo en la tabla para modificarlo.", "SKU duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Producto p = new Producto(sku, nombre, precio, stockMinimo, unidad);
            if (productoDAO.guardar(p)) {
                JOptionPane.showMessageDialog(this, "Producto guardado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarProductos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo guardar/actualizar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            mostrarError("Formato numérico inválido en Precio o Stock Mínimo.");
        } catch (SQLException e) {
            mostrarError("Error de persistencia: " + e.getMessage());
        }
    }

    private void eliminarProducto() {
        String sku = txtSku.getText().trim();
        if (sku.isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de eliminar el producto " + sku + "?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (productoDAO.eliminar(sku)) {
                    JOptionPane.showMessageDialog(this, "Producto eliminado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarProductos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                mostrarError("No se puede eliminar: El producto posee registros asociados en el sistema.");
            } catch (SQLException e) {
                mostrarError("Error de base de datos: " + e.getMessage());
            }
        }
    }

    private void mostrarError(String msj) {
        JOptionPane.showMessageDialog(this, msj, "Error", JOptionPane.ERROR_MESSAGE);
    }
}