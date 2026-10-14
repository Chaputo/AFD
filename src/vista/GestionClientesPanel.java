package vista;

import modelo.Cliente;
import persistencia.ClienteDAO;
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

public class GestionClientesPanel extends JPanel {

    private JTextField txtId, txtNombre, txtTelefono, txtDireccion, txtBuscar;
    private JButton btnGuardar, btnEliminar, btnLimpiar, btnExportar, btnRecargar;
    private JTable tablaClientes;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private ClienteDAO clienteDAO;

    public GestionClientesPanel() {
        clienteDAO = new ClienteDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        inicializarComponentes();

        // Tabla de Clientes
        tableModel = new DefaultTableModel(new Object[]{"ID", "Nombre / Razón Social", "Teléfono", "Dirección"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaClientes = new JTable(tableModel);
        tablaClientes.setRowHeight(24);
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Sorter para filtrado / búsqueda en tiempo real
        sorter = new TableRowSorter<>(tableModel);
        tablaClientes.setRowSorter(sorter);

        // Columna ID delgada
        tablaClientes.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaClientes.getColumnModel().getColumn(0).setMaxWidth(60);
        tablaClientes.getColumnModel().getColumn(0).setMinWidth(30);

        JScrollPane scrollTabla = new JScrollPane(tablaClientes);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Listado de clientes"));

        // Panel contenedor principal
        JPanel panelCuerpo = new JPanel(new BorderLayout(10, 0));
        JPanel panelIzquierdo = crearPanelLateralIzquierdo();
        panelIzquierdo.setPreferredSize(new Dimension(380, 0));

        panelCuerpo.add(panelIzquierdo, BorderLayout.WEST);
        panelCuerpo.add(scrollTabla, BorderLayout.CENTER);

        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(panelCuerpo, BorderLayout.CENTER);

        agregarListeners();
        cargarClientes();

        txtId.setEditable(false);
        btnEliminar.setEnabled(false);
    }

    private void inicializarComponentes() {
        txtId = new JTextField();
        txtNombre = new JTextField();
        txtTelefono = new JTextField();
        txtDireccion = new JTextField();
        txtBuscar = new JTextField();

        // Dimensiones estandarizadas (32px de alto)
        Dimension btnSize = new Dimension(160, 32);

        btnGuardar = new JButton("Guardar / Actualizar");
        btnLimpiar = new JButton("Limpiar / Nuevo");
        btnEliminar = new JButton("Eliminar Cliente");
        btnRecargar = new JButton("↻ Refrescar");
        btnExportar = new JButton("Exportar a Excel");

        btnGuardar.setPreferredSize(btnSize);
        btnLimpiar.setPreferredSize(btnSize);
        btnEliminar.setPreferredSize(btnSize);
        btnRecargar.setPreferredSize(btnSize);
        btnExportar.setPreferredSize(btnSize);
    }

    private JPanel crearBarraSuperior() {
        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelNorte.add(btnRecargar);
        panelNorte.add(btnExportar);
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
        JLabel lblBuscar = new JLabel("Cliente / Teléfono / Dirección:");
        lblBuscar.setFont(labelFont);
        panelBusqueda.add(lblBuscar, gbcB);

        gbcB.gridy = 1;
        panelBusqueda.add(txtBuscar, gbcB);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weighty = 0.0;
        panelIzquierdo.add(panelBusqueda, gbc);

        // --- SECCIÓN 2: DETALLES DEL CLIENTE ---
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder("Detalles del Cliente"));
        GridBagConstraints gbcF = new GridBagConstraints();
        gbcF.insets = new Insets(6, 6, 6, 6);
        gbcF.fill = GridBagConstraints.HORIZONTAL;
        gbcF.weightx = 1.0;

        // Nombre
        gbcF.gridx = 0; gbcF.gridy = 0;
        JLabel lblNombre = new JLabel("Nombre Completo:");
        lblNombre.setFont(labelFont);
        panelForm.add(lblNombre, gbcF);

        gbcF.gridy = 1;
        panelForm.add(txtNombre, gbcF);

        // Teléfono
        gbcF.gridy = 2;
        JLabel lblTelefono = new JLabel("Teléfono:");
        lblTelefono.setFont(labelFont);
        panelForm.add(lblTelefono, gbcF);

        gbcF.gridy = 3;
        panelForm.add(txtTelefono, gbcF);

        // Dirección
        gbcF.gridy = 4;
        JLabel lblDireccion = new JLabel("Dirección:");
        lblDireccion.setFont(labelFont);
        panelForm.add(lblDireccion, gbcF);

        gbcF.gridy = 5;
        panelForm.add(txtDireccion, gbcF);

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
        btnGuardar.addActionListener(e -> guardarOActualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnRecargar.addActionListener(e -> cargarClientes());
        btnExportar.addActionListener(e -> exportarExcel());

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

        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaClientes.getSelectedRow();
                if (fila != -1) {
                    cargarFilaSeleccionada(fila);
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
        if (tablaClientes.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay clientes en la tabla para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ExporterExcel.exportarTabla(tablaClientes, "Reporte_Clientes");
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtDireccion.setText("");
        tablaClientes.clearSelection();
        btnEliminar.setEnabled(false);
    }

    private void cargarFilaSeleccionada(int filaVista) {
        int filaModelo = tablaClientes.convertRowIndexToModel(filaVista);
        txtId.setText(String.valueOf(tableModel.getValueAt(filaModelo, 0)));
        txtNombre.setText(String.valueOf(tableModel.getValueAt(filaModelo, 1)));
        
        Object tel = tableModel.getValueAt(filaModelo, 2);
        txtTelefono.setText(tel != null ? tel.toString() : "");

        Object dir = tableModel.getValueAt(filaModelo, 3);
        txtDireccion.setText(dir != null ? dir.toString() : "");

        btnEliminar.setEnabled(true);
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
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre completo es obligatorio.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int id = txtId.getText().isEmpty() ? 0 : Integer.parseInt(txtId.getText());
            Cliente cliente = new Cliente(id, nombre, txtTelefono.getText().trim(), txtDireccion.getText().trim());
            if (clienteDAO.guardarOActualizar(cliente)) {
                JOptionPane.showMessageDialog(this, "Cliente guardado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarClientes();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo guardar/actualizar el cliente.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            mostrarError("Error al procesar cliente", e);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error de formato en el identificador.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "¿Está seguro de eliminar al cliente: " + txtNombre.getText() + "?", 
            "Confirmar eliminación", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (clienteDAO.eliminar(id)) {
                    JOptionPane.showMessageDialog(this, "Cliente eliminado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarClientes();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el cliente.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                mostrarError("Error de integridad. Verifique que no existan ventas registradas a nombre de este cliente", e);
            }
        }
    }

    private void mostrarError(String mensaje, Exception e) {
        JOptionPane.showMessageDialog(this, mensaje + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}