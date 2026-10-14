package vista;

import modelo.Usuario;
import modelo.SesionUsuario;
import persistencia.UsuarioDAO;
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

public class GestionUsuarioPanel extends JPanel {

    private JTextField txtId, txtNombre, txtUsuario, txtBuscar;
    private JPasswordField txtContrasena;
    private JComboBox<String> cmbRol;
    private JButton btnGuardar, btnEliminar, btnLimpiar, btnExportarExcel, btnRecargar;
    private JTable tablaUsuarios;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final String[] ROLES = {"VENDEDOR", "GERENTE", "ADMIN"};

    public GestionUsuarioPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        inicializarComponentes();

        // Configuración de la Tabla de Usuarios
        tableModel = new DefaultTableModel(new Object[]{"ID", "Nombre Completo", "Usuario", "Rol"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 0) ? Integer.class : String.class;
            }
        };
        tablaUsuarios = new JTable(tableModel);
        tablaUsuarios.setRowHeight(24);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // TableRowSorter para filtrado dinámico
        sorter = new TableRowSorter<>(tableModel);
        tablaUsuarios.setRowSorter(sorter);

        // Ajuste de ancho para columna ID
        tablaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaUsuarios.getColumnModel().getColumn(0).setMaxWidth(60);
        tablaUsuarios.getColumnModel().getColumn(0).setMinWidth(30);

        JScrollPane scrollTabla = new JScrollPane(tablaUsuarios);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Listado de usuarios registrados"));

        // Panel cuerpo principal (Izquierda + Centro)
        JPanel panelCuerpo = new JPanel(new BorderLayout(10, 0));
        JPanel panelIzquierdo = crearPanelLateralIzquierdo();
        panelIzquierdo.setPreferredSize(new Dimension(380, 0));

        panelCuerpo.add(panelIzquierdo, BorderLayout.WEST);
        panelCuerpo.add(scrollTabla, BorderLayout.CENTER);

        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(panelCuerpo, BorderLayout.CENTER);

        agregarListeners();
        cargarUsuarios();

        txtId.setEditable(false);
        btnEliminar.setEnabled(false);
    }

    private void inicializarComponentes() {
        txtId = new JTextField();
        txtNombre = new JTextField();
        txtUsuario = new JTextField();
        txtContrasena = new JPasswordField();
        txtBuscar = new JTextField();
        cmbRol = new JComboBox<>(ROLES);

        // Dimensión estándar de botones (32px de alto)
        Dimension btnSize = new Dimension(160, 32);

        btnGuardar = new JButton("Guardar / Actualizar");
        btnLimpiar = new JButton("Limpiar / Nuevo");
        btnEliminar = new JButton("Eliminar Usuario");
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
        JLabel lblBuscar = new JLabel("Nombre / Usuario / Rol:");
        lblBuscar.setFont(labelFont);
        panelBusqueda.add(lblBuscar, gbcB);

        gbcB.gridy = 1;
        panelBusqueda.add(txtBuscar, gbcB);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weighty = 0.0;
        panelIzquierdo.add(panelBusqueda, gbc);

        // --- SECCIÓN 2: DETALLES DE CUENTA Y PERFIL ---
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder("Detalles de cuenta y perfil"));
        GridBagConstraints gbcF = new GridBagConstraints();
        gbcF.insets = new Insets(5, 6, 5, 6);
        gbcF.fill = GridBagConstraints.HORIZONTAL;
        gbcF.weightx = 1.0;

        // Rol
        gbcF.gridx = 0; gbcF.gridy = 0;
        JLabel lblRol = new JLabel("Rol de sistema:");
        lblRol.setFont(labelFont);
        panelForm.add(lblRol, gbcF);

        gbcF.gridy = 1;
        panelForm.add(cmbRol, gbcF);

        // Nombre Completo
        gbcF.gridy = 2;
        JLabel lblNombre = new JLabel("Nombre Completo:");
        lblNombre.setFont(labelFont);
        panelForm.add(lblNombre, gbcF);

        gbcF.gridy = 3;
        panelForm.add(txtNombre, gbcF);

        // Usuario (login)
        gbcF.gridy = 4;
        JLabel lblUsuario = new JLabel("Usuario (login):");
        lblUsuario.setFont(labelFont);
        panelForm.add(lblUsuario, gbcF);

        gbcF.gridy = 5;
        panelForm.add(txtUsuario, gbcF);

        // Contraseña
        gbcF.gridy = 6;
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(labelFont);
        panelForm.add(lblPass, gbcF);

        gbcF.gridy = 7;
        panelForm.add(txtContrasena, gbcF);

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
        btnGuardar.addActionListener(e -> guardarOActualizarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnRecargar.addActionListener(e -> cargarUsuarios());
        btnExportarExcel.addActionListener(e -> exportarExcel());

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

        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaVista = tablaUsuarios.getSelectedRow();
                if (filaVista != -1) {
                    cargarFilaSeleccionadaAlFormulario(filaVista);
                    btnEliminar.setEnabled(true);
                } else {
                    btnEliminar.setEnabled(false);
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

    private void exportarExcel() {
        if (tablaUsuarios.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay usuarios registrados para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ExporterExcel.exportarTabla(tablaUsuarios, "Listado_Usuarios");
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
            mostrarMensajeError("Error de Base de Datos", "Error al cargar usuarios: " + e.getMessage());
        }
    }

    private void cargarFilaSeleccionadaAlFormulario(int filaVista) {
        int filaModelo = tablaUsuarios.convertRowIndexToModel(filaVista);
        txtId.setText(String.valueOf(tableModel.getValueAt(filaModelo, 0)));
        txtNombre.setText(String.valueOf(tableModel.getValueAt(filaModelo, 1)));
        txtUsuario.setText(String.valueOf(tableModel.getValueAt(filaModelo, 2)));
        cmbRol.setSelectedItem(String.valueOf(tableModel.getValueAt(filaModelo, 3)));
        txtContrasena.setText("");
    }

    private void guardarOActualizarUsuario() {
        String nombre = txtNombre.getText().trim();
        String userStr = txtUsuario.getText().trim();
        String pass = new String(txtContrasena.getPassword());
        String rol = cmbRol.getSelectedItem().toString();

        if (nombre.isEmpty() || userStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y Usuario son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = txtId.getText().isEmpty() ? 0 : Integer.parseInt(txtId.getText());
        if (id == 0 && pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La contraseña es obligatoria para nuevos usuarios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario nuevoU = new Usuario(id, nombre, rol);
        nuevoU.setUsuario(userStr);
        nuevoU.setPassword(pass);

        try {
            if (usuarioDAO.guardarOActualizar(nuevoU)) {
                JOptionPane.showMessageDialog(this, "Usuario guardado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarUsuarios();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo guardar/actualizar el usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            mostrarMensajeError("Error", "El nombre de usuario ya existe o hubo un problema en la base de datos.");
        }
    }

    private void eliminarUsuario() {
        if (txtId.getText().isEmpty()) return;

        int idAEliminar = Integer.parseInt(txtId.getText());

        // Control de seguridad: No permitir auto-eliminación de la cuenta en sesión activo
        if (SesionUsuario.getUsuario() != null && idAEliminar == SesionUsuario.getUsuario().getId()) {
            JOptionPane.showMessageDialog(this, "No puedes eliminar tu propia cuenta de usuario activa.", "Acción denegada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de eliminar al usuario '" + txtUsuario.getText() + "'?\nEsta acción es irreversible.",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (usuarioDAO.eliminar(idAEliminar)) {
                    JOptionPane.showMessageDialog(this, "Usuario eliminado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarUsuarios();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el usuario.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                mostrarMensajeError("Error de DB", "No se pudo eliminar el usuario: " + e.getMessage());
            }
        }
    }

    private void mostrarMensajeError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }
}