package vista;

import modelo.Proveedor;
import persistencia.ProveedorDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class GestionProveedoresPanel extends JPanel {
    
    private JTextField txtId, txtNombre, txtCuit, txtContacto; 
    private JButton btnGuardar, btnEliminar, btnLimpiar;
    private JTable tablaProveedores;
    private DefaultTableModel tableModel;
    private ProveedorDAO proveedorDAO;

    public GestionProveedoresPanel() {
        proveedorDAO = new ProveedorDAO();
        
        // Layout principal simétrico
        setLayout(new BorderLayout(15, 15)); 
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        inicializarComponentes();
        
        // Estructura de tabla limpia y nativa según tu base de datos
        tableModel = new DefaultTableModel(new Object[]{"ID", "Nombre / Razón social", "CUIT", "Contacto (teléfono/email)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        tablaProveedores = new JTable(tableModel);
        tablaProveedores.setRowHeight(22); // Altura de celda estándar para tablas oscuras de tu app
        tablaProveedores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tablaProveedores);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Listado de proveedores"));

        // Acoplamos los paneles respetando el flujo de la app
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        agregarListeners();
        cargarProveedores();
        txtId.setEditable(false);
        btnEliminar.setEnabled(false);
    }
    
    private void inicializarComponentes() {
        // Inicialización de campos limpia (FlatLaf se encarga del look dark de forma nativa)
        txtId = new JTextField();
        txtNombre = new JTextField();
        txtCuit = new JTextField();
        txtContacto = new JTextField();
        
        // Botonera centralizada
        btnGuardar = new JButton("Guardar / actualizar");
        btnEliminar = new JButton("Eliminar proveedor"); // CORREGIDO: Texto conceptual unificado
        btnLimpiar = new JButton("Limpiar / nuevo");
    }

    private JPanel crearPanelSuperior() {
        JPanel panelNorte = new JPanel(new BorderLayout(0, 15));

        // Grid de 2 filas y 4 columnas idéntico al panel de Clientes
        JPanel panelFormulario = new JPanel(new GridLayout(2, 4, 15, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Detalles del proveedor"));

        Font labelFont = new Font("SansSerif", Font.PLAIN, 12);
        
        // Fila 1
        JLabel lblId = new JLabel("ID (Auto):");
        lblId.setFont(labelFont);
        panelFormulario.add(lblId);
        panelFormulario.add(txtId);
        
        JLabel lblNombre = new JLabel("Nombre completo:");
        lblNombre.setFont(labelFont);
        panelFormulario.add(lblNombre);
        panelFormulario.add(txtNombre);
        
        // Fila 2 - CORREGIDO: Etiquetas limpias basadas en las columnas de tu DB
        JLabel lblCuit = new JLabel("CUIT:");
        lblCuit.setFont(labelFont);
        panelFormulario.add(lblCuit);
        panelFormulario.add(txtCuit);
        
        JLabel lblContacto = new JLabel("Dirección / contacto:");
        lblContacto.setFont(labelFont);
        panelFormulario.add(lblContacto);
        panelFormulario.add(txtContacto);
        
        // Panel horizontal para albergar la botonera al centro
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        
        Dimension buttonSize = new Dimension(150, 30);
        btnGuardar.setPreferredSize(buttonSize);
        btnEliminar.setPreferredSize(buttonSize);
        btnLimpiar.setPreferredSize(buttonSize);
        
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        
        panelNorte.add(panelFormulario, BorderLayout.CENTER);
        panelNorte.add(panelBotones, BorderLayout.SOUTH);
        
        return panelNorte;
    }
    
    private void agregarListeners() {
        btnGuardar.addActionListener(e -> guardarOActualizarProveedor());
        btnEliminar.addActionListener(e -> eliminarProveedor());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        
        tablaProveedores.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaProveedores.getSelectedRow();
                if (fila != -1) {
                    cargarFilaSeleccionada(fila);
                }
            }
        });
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtCuit.setText("");
        txtContacto.setText("");
        tablaProveedores.clearSelection();
        btnEliminar.setEnabled(false);
        txtCuit.setEditable(true);
    }
    
    private void cargarFilaSeleccionada(int fila) {
        txtId.setText(String.valueOf(tableModel.getValueAt(fila, 0)));
        txtNombre.setText(String.valueOf(tableModel.getValueAt(fila, 1)));
        txtCuit.setText(String.valueOf(tableModel.getValueAt(fila, 2)));
        txtContacto.setText(String.valueOf(tableModel.getValueAt(fila, 3)));
        
        txtCuit.setEditable(false); 
        btnEliminar.setEnabled(true);
    }
    
    private void cargarProveedores() {
        tableModel.setRowCount(0); 
        try {
            List<Proveedor> proveedores = proveedorDAO.listarTodos();
            for (Proveedor p : proveedores) {
                tableModel.addRow(new Object[]{p.getId(), p.getNombre(), p.getCuit(), p.getContacto()});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar proveedores: " + e.getMessage(), "Error de DB", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void guardarOActualizarProveedor() {
        String nombre = txtNombre.getText().trim();
        String cuit = txtCuit.getText().trim();
        String contacto = txtContacto.getText().trim();
        
        if (nombre.isEmpty() || cuit.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y CUIT son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Proveedor proveedor;
            if (txtId.getText().isEmpty()) {
                proveedor = new Proveedor(nombre, cuit, contacto); 
            } else {
                int id = Integer.parseInt(txtId.getText());
                proveedor = new Proveedor(id, nombre, cuit, contacto);
            }
            
            boolean exito = proveedorDAO.guardarOActualizar(proveedor);

            if (exito) {
                JOptionPane.showMessageDialog(this, "Proveedor guardado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarProveedores();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo guardar/actualizar el proveedor.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de base de datos. Asegúrese de que el CUIT no esté duplicado.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error de formato en los identificadores.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarProveedor() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un proveedor para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(txtId.getText());
        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar al proveedor: " + txtNombre.getText() + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (proveedorDAO.eliminar(id)) {
                    JOptionPane.showMessageDialog(this, "Proveedor eliminado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarProveedores();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el proveedor.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error de integridad. Verifique que no existan Órdenes de Compra vinculadas a este proveedor.", 
                    "Error de clave foránea", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}