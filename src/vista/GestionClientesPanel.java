package vista;

import modelo.Cliente;
import persistencia.ClienteDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class GestionClientesPanel extends JPanel {
    
    private JTextField txtId, txtNombre, txtTelefono, txtDireccion;
    private JButton btnGuardar, btnEliminar, btnLimpiar;
    private JTable tablaClientes;
    private DefaultTableModel tableModel;
    private ClienteDAO clienteDAO;

    public GestionClientesPanel() {
        clienteDAO = new ClienteDAO();
        setLayout(new BorderLayout(15, 15)); 
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        inicializarComponentes();
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearPanelTabla(), BorderLayout.CENTER);
        
        agregarListeners();
        cargarClientes();
    }
    
    private void inicializarComponentes() {
        txtId = new JTextField();
        txtId.setEditable(false);
        txtNombre = new JTextField();
        txtTelefono = new JTextField();
        txtDireccion = new JTextField();
        
        btnGuardar = new JButton("Guardar / Actualizar");
        btnEliminar = new JButton("Eliminar Cliente");
        btnLimpiar = new JButton("Limpiar / Nuevo");

        // Quitamos los colores hardcodeados para que FlatLaf maneje el tema oscuro
        // Pero mantenemos fuentes para resaltar
        Font buttonFont = new Font("SansSerif", Font.BOLD, 12);
        btnGuardar.setFont(buttonFont);
        btnEliminar.setFont(buttonFont);
        btnLimpiar.setFont(buttonFont);
    }

    private JPanel crearPanelSuperior() {
        JPanel panelNorte = new JPanel(new BorderLayout(20, 10));
        
        // Formulario (Igual al de Usuarios en image_1fe8ed.png)
        JPanel panelFormulario = new JPanel(new GridLayout(2, 4, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), "Detalles del Cliente", 
            TitledBorder.LEFT, TitledBorder.TOP));

        panelFormulario.add(new JLabel("ID (Auto):"));
        panelFormulario.add(txtId);
        panelFormulario.add(new JLabel("Nombre Completo:"));
        panelFormulario.add(txtNombre);
        panelFormulario.add(new JLabel("Teléfono:"));
        panelFormulario.add(txtTelefono);
        panelFormulario.add(new JLabel("Dirección:"));
        panelFormulario.add(txtDireccion);

        // Botonera lateral
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        panelNorte.add(panelFormulario, BorderLayout.CENTER);
        panelNorte.add(panelBotones, BorderLayout.SOUTH);
        
        return panelNorte;
    }

    private JScrollPane crearPanelTabla() {
        tableModel = new DefaultTableModel(new Object[]{"ID", "Nombre", "Teléfono", "Dirección"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        tablaClientes = new JTable(tableModel);
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Ocultar columna ID para estética, pero mantener el dato
        tablaClientes.getColumnModel().getColumn(0).setMinWidth(0);
        tablaClientes.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane scroll = new JScrollPane(tablaClientes);
        scroll.setBorder(BorderFactory.createTitledBorder("Listado de Clientes"));
        return scroll;
    }
    
    private void agregarListeners() {
        btnGuardar.addActionListener(e -> guardarOActualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        
        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarFilaSeleccionada();
        });
    }

    private void cargarFilaSeleccionada() {
        int fila = tablaClientes.getSelectedRow();
        if (fila >= 0) {
            txtId.setText(tableModel.getValueAt(fila, 0).toString());
            txtNombre.setText(tableModel.getValueAt(fila, 1).toString());
            txtTelefono.setText(tableModel.getValueAt(fila, 2).toString());
            txtDireccion.setText(tableModel.getValueAt(fila, 3).toString());
        }
    }

    private void cargarClientes() {
        tableModel.setRowCount(0);
        try {
            List<Cliente> lista = clienteDAO.listarTodos();
            for (Cliente c : lista) {
                tableModel.addRow(new Object[]{c.getId(), c.getNombre(), c.getTelefono(), c.getDireccion()});
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar clientes", e);
        }
    }

    private void guardarOActualizar() {
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.");
            return;
        }

        try {
            int id = txtId.getText().isEmpty() ? 0 : Integer.parseInt(txtId.getText());
            Cliente cliente = new Cliente(id, txtNombre.getText(), txtTelefono.getText(), txtDireccion.getText());
            
            if (clienteDAO.guardarOActualizar(cliente)) {
                JOptionPane.showMessageDialog(this, "Datos guardados correctamente.");
                limpiarCampos();
                cargarClientes();
            }
        } catch (SQLException e) {
            mostrarError("Error al procesar cliente", e);
        }
    }

    private void eliminar() {
        if (txtId.getText().isEmpty()) return;
        
        int resp = JOptionPane.showConfirmDialog(this, "¿Eliminar cliente?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.YES_OPTION) {
            try {
                if (clienteDAO.eliminar(Integer.parseInt(txtId.getText()))) {
                    limpiarCampos();
                    cargarClientes();
                }
            } catch (SQLException e) {
                mostrarError("Error al eliminar", e);
            }
        }
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtDireccion.setText("");
        tablaClientes.clearSelection();
    }

    private void mostrarError(String mensaje, Exception e) {
        JOptionPane.showMessageDialog(this, mensaje + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}