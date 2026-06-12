package vista;

import modelo.Usuario;
import modelo.SesionUsuario; // Para saber quién está operando
import persistencia.UsuarioDAO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class GestionUsuarioPanel extends JPanel { 

    private JTextField txtId, txtNombre, txtUsuario;
    private JPasswordField txtContrasena;
    private JComboBox<String> cmbRol;
    private JButton btnGuardar, btnEliminar, btnLimpiar;
    private JTable tablaUsuarios;
    private DefaultTableModel tableModel;
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    
    // CORRECCIÓN 1: Roles estandarizados para que coincidan con la DB y MainFrame
    private static final String[] ROLES = {"VENDEDOR", "GERENTE", "ADMIN"};
    
    public GestionUsuarioPanel() { 
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        inicializarComponentes();
        configurarTabla();
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);
        agregarListeners();
        cargarUsuarios();
        limpiarCampos(); 
    }

    private void inicializarComponentes() {
        txtId = new JTextField(5);
        txtNombre = new JTextField(25);
        txtUsuario = new JTextField(20);
        txtContrasena = new JPasswordField(20);
        cmbRol = new JComboBox<>(ROLES);
        txtId.setEditable(false);

        btnGuardar = new JButton("💾 Guardar/actualizar");
        btnEliminar = new JButton("🗑️ Eliminar usuario");
        btnLimpiar = new JButton("✨ Limpiar/nuevo");

        btnEliminar.setEnabled(false); 
    }

    private JPanel crearPanelSuperior() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        JPanel panelCampos = new JPanel(new GridLayout(3, 4, 15, 8)); 
        panelCampos.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Detalles de cuenta y perfil", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("SansSerif", Font.BOLD, 14)
        ));
        
        panelCampos.add(new JLabel("ID (Auto):"));
        panelCampos.add(txtId);
        panelCampos.add(new JLabel("Rol:"));
        panelCampos.add(cmbRol);
        
        panelCampos.add(new JLabel("Nombre completo:"));
        panelCampos.add(txtNombre);
        panelCampos.add(new JLabel("Usuario (login):"));
        panelCampos.add(txtUsuario);
        
        panelCampos.add(new JLabel("Contraseña:"));
        panelCampos.add(txtContrasena);
        panelCampos.add(new JLabel("")); 
        panelCampos.add(new JLabel("")); 

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        panelPrincipal.add(panelCampos, BorderLayout.NORTH);
        panelPrincipal.add(panelBotones, BorderLayout.CENTER);
        
        return panelPrincipal;
    }

    private void configurarTabla() {
        tableModel = new DefaultTableModel(new Object[]{"ID", "Nombre", "Usuario", "Rol"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 0) ? Integer.class : String.class;
            }
        };
        tablaUsuarios = new JTable(tableModel);
        
        tablaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaUsuarios.getColumnModel().getColumn(0).setMaxWidth(70);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void agregarListeners() {
        btnGuardar.addActionListener(e -> guardarOActualizarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (tablaUsuarios.getSelectedRow() != -1) {
                    cargarFilaSeleccionadaAlFormulario();
                    btnEliminar.setEnabled(true);
                } else {
                    btnEliminar.setEnabled(false);
                }
            }
        });
    }
    
    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtUsuario.setText("");
        txtContrasena.setText("");
        cmbRol.setSelectedIndex(0);
        tablaUsuarios.clearSelection();
        btnEliminar.setEnabled(false);
    }
    
    private void cargarUsuarios() {
        tableModel.setRowCount(0); 
        try {
            List<Usuario> usuarios = usuarioDAO.listarTodos();
            for (Usuario u : usuarios) {
                tableModel.addRow(new Object[]{u.getId(), u.getNombre(), u.getUsuario(), u.getRol()});
            }
        } catch (SQLException e) {
            mostrarMensajeError("Error de DB", "Error al cargar usuarios: " + e.getMessage());
        }
    }
    
    private void cargarFilaSeleccionadaAlFormulario() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila >= 0) {
            txtId.setText(String.valueOf(tableModel.getValueAt(fila, 0)));
            txtNombre.setText(String.valueOf(tableModel.getValueAt(fila, 1)));
            txtUsuario.setText(String.valueOf(tableModel.getValueAt(fila, 2)));
            cmbRol.setSelectedItem(String.valueOf(tableModel.getValueAt(fila, 3)));
            txtContrasena.setText("");
        }
    }

    private void eliminarUsuario() {
        if (txtId.getText().isEmpty()) return;

        int idAEliminar = Integer.parseInt(txtId.getText());
        
        // CORRECCIÓN 2: Protección de Seguridad
        // Evitamos que el usuario logueado se elimine a sí mismo
        if (idAEliminar == SesionUsuario.getUsuario().getId()) {
            JOptionPane.showMessageDialog(this, "No puedes eliminar tu propia cuenta de usuario.", "Acción denegada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de eliminar este usuario?\nEsta acción es irreversible.", 
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (usuarioDAO.eliminar(idAEliminar)) {
                    JOptionPane.showMessageDialog(this, "Usuario eliminado.");
                    limpiarCampos();
                    cargarUsuarios();
                }
            } catch (SQLException e) {
                mostrarMensajeError("Error de DB", e.getMessage());
            }
        }
    }

    private void guardarOActualizarUsuario() {
        String nombre = txtNombre.getText().trim();
        String userStr = txtUsuario.getText().trim();
        String pass = new String(txtContrasena.getPassword());
        String rol = cmbRol.getSelectedItem().toString();
        
        if (nombre.isEmpty() || userStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y Usuario son obligatorios.");
            return;
        }

        int id = txtId.getText().isEmpty() ? 0 : Integer.parseInt(txtId.getText());
        
        // Si es nuevo (ID 0), la contraseña es obligatoria
        if (id == 0 && pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La contraseña es obligatoria para nuevos usuarios.");
            return;
        }

        Usuario nuevoU = new Usuario(id, nombre, rol);
        nuevoU.setUsuario(userStr);
        nuevoU.setPassword(pass);

        try {
            if (usuarioDAO.guardarOActualizar(nuevoU)) {
                JOptionPane.showMessageDialog(this, "¡Listo!");
                limpiarCampos();
                cargarUsuarios();
            }
        } catch (SQLException e) {
            mostrarMensajeError("Error", "El usuario ya existe o hubo un error en la base de datos.");
        }
    }
    
    private void mostrarMensajeError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }
}