package vista;

import modelo.Producto;
import persistencia.ProductoDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class GestionProductosPanel extends JPanel {

    // Variables y Constructor

    private JTextField txtSku, txtNombre, txtPrecio, txtStockMinimo, txtUnidadMedida; 
    private JButton btnGuardar, btnEliminar, btnLimpiar;
    private JTable tablaProductos;
    private DefaultTableModel tableModel;
    private ProductoDAO productoDAO;

    public GestionProductosPanel() {
        productoDAO = new ProductoDAO();
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        inicializarComponentes();
        tableModel = new DefaultTableModel(
            new Object[]{"SKU", "Nombre", "Precio", "Stock mín.", "Unidad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaProductos = new JTable(tableModel);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.getTableHeader().setReorderingAllowed(false);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                crearPanelFormulario(), new JScrollPane(tablaProductos));
        splitPane.setDividerLocation(400); 
        add(splitPane, BorderLayout.CENTER);
        agregarListeners();
        cargarProductos();
    }

    private void inicializarComponentes() {
        txtSku = new JTextField();
        txtNombre = new JTextField();
        txtPrecio = new JTextField();
        txtStockMinimo = new JTextField();
        txtUnidadMedida = new JTextField();
        btnGuardar = new JButton("Guardar / actualizar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar / nuevo");
        btnEliminar.setEnabled(false);
    }
    
    private JPanel crearPanelFormulario() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        JPanel panelCampos = new JPanel(new GridLayout(6, 2, 8, 8));
        panelCampos.setBorder(BorderFactory.createTitledBorder("Definición de producto (maestro)"));
        panelCampos.add(new JLabel("SKU (código):"));
        panelCampos.add(txtSku);
        panelCampos.add(new JLabel("Nombre:"));
        panelCampos.add(txtNombre);
        panelCampos.add(new JLabel("Precio venta ($):"));
        panelCampos.add(txtPrecio);
        panelCampos.add(new JLabel("Stock mínimo:"));
        panelCampos.add(txtStockMinimo);
        panelCampos.add(new JLabel("Unidad de medida:"));
        panelCampos.add(txtUnidadMedida);
        JPanel panelBotones = new JPanel(new GridLayout(2, 1, 5, 5));
        panelBotones.add(btnGuardar);
        JPanel panelAux = new JPanel(new GridLayout(1, 2, 5, 5));
        panelAux.add(btnEliminar);
        panelAux.add(btnLimpiar);
        panelBotones.add(panelAux);
        panelPrincipal.add(panelCampos, BorderLayout.NORTH);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        return panelPrincipal;
    }

    private void agregarListeners() {
        btnGuardar.addActionListener(e -> guardarOActualizarProducto());
        btnEliminar.addActionListener(e -> eliminarProducto());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarFilaSeleccionada();
            }
        });
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
            mostrarError("Error al cargar productos de la base de datos.");
            e.printStackTrace();
        }
    }
    
    private void cargarFilaSeleccionada() {
        int fila = tablaProductos.getSelectedRow();
        if (fila != -1) {
            txtSku.setText(tableModel.getValueAt(fila, 0).toString());
            txtNombre.setText(tableModel.getValueAt(fila, 1).toString());
            txtPrecio.setText(tableModel.getValueAt(fila, 2).toString());
            txtStockMinimo.setText(tableModel.getValueAt(fila, 3).toString());
            txtUnidadMedida.setText(tableModel.getValueAt(fila, 4).toString());
            txtSku.setEditable(false);
            btnEliminar.setEnabled(true);
        }
    }
    
    private void guardarOActualizarProducto() {
        if (txtSku.getText().isBlank() || txtNombre.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "SKU y Nombre son campos obligatorios.");
            return;
        }
        try {
            String sku = txtSku.getText().trim();
            String nombre = txtNombre.getText().trim();
            double precio = Double.parseDouble(txtPrecio.getText().replace(",", "."));
            int stockMinimo = Integer.parseInt(txtStockMinimo.getText().trim());
            String unidad = txtUnidadMedida.getText().trim();
            Producto p = new Producto(sku, nombre, precio, stockMinimo, unidad);
            if (productoDAO.guardar(p)) {
                JOptionPane.showMessageDialog(this, "Operación exitosa.");
                limpiarCampos();
                cargarProductos();
            }
        } catch (NumberFormatException e) {
            mostrarError("Formato numérico inválido en Precio o Stock.");
        } catch (SQLException e) {
            mostrarError("Error de persistencia: " + e.getMessage());
        }
    }
    
    private void eliminarProducto() {
        String sku = txtSku.getText().trim();
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Eliminar producto " + sku + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (productoDAO.eliminar(sku)) {
                    limpiarCampos();
                    cargarProductos();
                }
            } catch (SQLException e) {
                mostrarError("No se puede eliminar: El producto tiene pedidos asociados.");
            }
        }
    }

    private void mostrarError(String msj) {
        JOptionPane.showMessageDialog(this, msj, "Error", JOptionPane.ERROR_MESSAGE);
    }
}