package vista;

import modelo.*;
import persistencia.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GestionOrdenDeCompraPanel extends JPanel implements ActionListener {

    // Variables y Constructor

    private JComboBox<Proveedor> cmbProveedor;
    private JComboBox<Producto> cmbProducto;
    private JTextField txtCantidad;
    private JTextField txtPrecioUnitario;
    private JButton btnAgregarLinea;
    private JButton btnEliminarLinea;
    private JButton btnGuardarOC;
    private JTable tblLineasOC;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotal;
    private List<LineaOC> lineasOCActuales = new ArrayList<>();
    private OrdenDeCompraDAO ocDAO = new OrdenDeCompraDAO();

    public GestionOrdenDeCompraPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        inicializarInterfaz();
        cargarDatosDesdeBaseDatos();
    }

    private void inicializarInterfaz() {

        JPanel panelNorteContenedor = new JPanel(new BorderLayout(0, 15));
        JPanel panelCampos = new JPanel(new GridLayout(2, 4, 15, 10));
        panelCampos.setBorder(BorderFactory.createTitledBorder("Detalles de la orden de compra"));
        Font labelFont = new Font("SansSerif", Font.PLAIN, 12);
        JLabel lblProv = new JLabel("Proveedor:");
        lblProv.setFont(labelFont);
        panelCampos.add(lblProv);
        cmbProveedor = new JComboBox<>();
        panelCampos.add(cmbProveedor);
        JLabel lblProd = new JLabel("Producto / artículo:");
        lblProd.setFont(labelFont);
        panelCampos.add(lblProd);
        cmbProducto = new JComboBox<>();
        panelCampos.add(cmbProducto);
        JLabel lblCant = new JLabel("Cantidad:");
        lblCant.setFont(labelFont);
        panelCampos.add(lblCant);
        txtCantidad = new JTextField();
        panelCampos.add(txtCantidad);
        JLabel lblPrecio = new JLabel("Precio unitario compra:");
        lblPrecio.setFont(labelFont);
        panelCampos.add(lblPrecio);
        txtPrecioUnitario = new JTextField();
        panelCampos.add(txtPrecioUnitario);
        JPanel panelBotonera = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        btnAgregarLinea = new JButton("Agregar ítem");
        btnEliminarLinea = new JButton("Eliminar seleccionado");
        btnGuardarOC = new JButton("Guardar / emitir OC");
        Dimension buttonSize = new Dimension(160, 30);
        btnAgregarLinea.setPreferredSize(buttonSize);
        btnEliminarLinea.setPreferredSize(buttonSize);
        btnGuardarOC.setPreferredSize(buttonSize);
        btnAgregarLinea.addActionListener(this);
        btnEliminarLinea.addActionListener(this);
        btnGuardarOC.addActionListener(this);
        panelBotonera.add(btnAgregarLinea);
        panelBotonera.add(btnEliminarLinea);
        panelBotonera.add(btnGuardarOC);
        panelNorteContenedor.add(panelCampos, BorderLayout.CENTER);
        panelNorteContenedor.add(panelBotonera, BorderLayout.SOUTH);
        add(panelNorteContenedor, BorderLayout.NORTH);
        JPanel panelTablaContenedor = new JPanel(new BorderLayout(5, 5));
        panelTablaContenedor.setBorder(BorderFactory.createTitledBorder("Detalle de ítems agregados"));
        String[] columnas = {"SKU", "Descripción producto", "Cantidad", "Precio U.", "Subtotal"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblLineasOC = new JTable(modeloTabla);
        tblLineasOC.setRowHeight(22);
        JScrollPane scrollPane = new JScrollPane(tblLineasOC);
        JPanel panelFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        lblTotal = new JLabel("TOTAL OC: $0.00");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelFooter.add(lblTotal);
        panelTablaContenedor.add(scrollPane, BorderLayout.CENTER);
        panelTablaContenedor.add(panelFooter, BorderLayout.SOUTH);
        add(panelTablaContenedor, BorderLayout.CENTER);
    }

    private void cargarDatosDesdeBaseDatos() {
        try {
            ProveedorDAO provDAO = new ProveedorDAO();
            cmbProveedor.removeAllItems();
            for (Proveedor p : provDAO.listarTodos()) {
                cmbProveedor.addItem(p);
            }
            ProductoDAO prodDAO = new ProductoDAO();
            cmbProducto.removeAllItems();
            for (Producto prod : prodDAO.listarTodos()) {
                cmbProducto.addItem(prod);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos iniciales: " + e.getMessage(), "Error DB", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void actualizarTotal() {
        double total = lineasOCActuales.stream().mapToDouble(LineaOC::getSubtotal).sum();
        lblTotal.setText(String.format("TOTAL OC: $%.2f", total));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAgregarLinea) {
            agregarLineaALaOC();
        } else if (e.getSource() == btnEliminarLinea) {
            eliminarLineaSeleccionada();
        } else if (e.getSource() == btnGuardarOC) {
            guardarOrdenDeCompra();
        }
    }

    private void agregarLineaALaOC() {
        try {
            Producto prod = (Producto) cmbProducto.getSelectedItem();
            if (prod == null || txtCantidad.getText().isEmpty() || txtPrecioUnitario.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete los datos del ítem.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int cant = Integer.parseInt(txtCantidad.getText().trim());
            double precio = Double.parseDouble(txtPrecioUnitario.getText().trim());
            if (cant <= 0 || precio <= 0) {
                JOptionPane.showMessageDialog(this, "Los valores deben ser mayores a cero.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double subtotal = cant * precio;
            LineaOC linea = new LineaOC(0, 0, prod.getSku(), cant, (float) precio, subtotal);
            lineasOCActuales.add(linea);
            modeloTabla.addRow(new Object[]{
                prod.getSku(),
                prod.getNombre(),
                cant,
                String.format("$%.2f", precio),
                String.format("$%.2f", subtotal)
            });
            actualizarTotal();
            txtCantidad.setText("");
            txtPrecioUnitario.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Verifique los formatos numéricos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarLineaSeleccionada() {
        int fila = tblLineasOC.getSelectedRow();
        if (fila >= 0) {
            lineasOCActuales.remove(fila);
            modeloTabla.removeRow(fila);
            actualizarTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un ítem del listado.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void guardarOrdenDeCompra() {
        if (lineasOCActuales.isEmpty() || cmbProveedor.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Formulario incompleto o sin ítems.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Proveedor prov = (Proveedor) cmbProveedor.getSelectedItem();
        int idUsuario = (SesionUsuario.getUsuario() != null) ? SesionUsuario.getUsuario().getId() : 1;
        double total = lineasOCActuales.stream().mapToDouble(LineaOC::getSubtotal).sum();
        OrdenDeCompra oc = new OrdenDeCompra(0, prov.getId(), idUsuario, LocalDateTime.now(), total, "EMITIDA", lineasOCActuales);
        try {
            if (ocDAO.guardarOCCompleta(oc)) {
                JOptionPane.showMessageDialog(this, "Orden de Compra registrada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                lineasOCActuales.clear();
                modeloTabla.setRowCount(0);
                actualizarTotal();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo insertar la Orden de Compra.", "Error de persistencia", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar en la base de datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}