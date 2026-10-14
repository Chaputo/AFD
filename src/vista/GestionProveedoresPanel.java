package vista;

import modelo.Proveedor;
import persistencia.ProveedorDAO;
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

public class GestionProveedoresPanel extends JPanel {

    private JTextField txtId, txtNombre, txtCuit, txtContacto, txtBuscar; 
    private JButton btnGuardar, btnEliminar, btnLimpiar, btnExportarExcel, btnRecargar;
    private JTable tablaProveedores;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private ProveedorDAO proveedorDAO;

    public GestionProveedoresPanel() {
        proveedorDAO = new ProveedorDAO();
        setLayout(new BorderLayout(10, 10)); 
        setBorder(new EmptyBorder(10, 10, 10, 10));

        inicializarComponentes();
        
        // Tabla de Proveedores
        tableModel = new DefaultTableModel(new Object[]{"ID", "Nombre / Razón social", "CUIT", "Contacto (teléfono/email)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaProveedores = new JTable(tableModel);
        tablaProveedores.setRowHeight(24);
        tablaProveedores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Sorter para filtrado / búsqueda en tiempo real
        sorter = new TableRowSorter<>(tableModel);
        tablaProveedores.setRowSorter(sorter);

        // Columna ID delgada
        tablaProveedores.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaProveedores.getColumnModel().getColumn(0).setMaxWidth(60);
        tablaProveedores.getColumnModel().getColumn(0).setMinWidth(30);

        JScrollPane scrollTabla = new JScrollPane(tablaProveedores);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Listado de proveedores"));

        // Panel contenedor fijo
        JPanel panelCuerpo = new JPanel(new BorderLayout(10, 0));
        JPanel panelIzquierdo = crearPanelLateralIzquierdo();
        panelIzquierdo.setPreferredSize(new Dimension(380, 0));

        panelCuerpo.add(panelIzquierdo, BorderLayout.WEST);
        panelCuerpo.add(scrollTabla, BorderLayout.CENTER);

        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(panelCuerpo, BorderLayout.CENTER);

        agregarListeners();
        cargarProveedores();
        btnEliminar.setEnabled(false);
    }

    private void inicializarComponentes() {
        txtId = new JTextField();
        txtNombre = new JTextField();
        txtCuit = new JTextField();
        txtContacto = new JTextField();
        txtBuscar = new JTextField();
        
        // Botones con las mismas dimensiones (altura de 32px)
        Dimension btnSize = new Dimension(160, 32);

        btnGuardar = new JButton("Guardar / Actualizar");
        btnLimpiar = new JButton("Limpiar / Nuevo");
        btnEliminar = new JButton("Eliminar Proveedor");
        btnRecargar = new JButton("↻ Refrescar");
        btnExportarExcel = new JButton("Exportar a Excel");

        // Homologar dimensiones preferidas en todos los botones
        btnGuardar.setPreferredSize(btnSize);
        btnLimpiar.setPreferredSize(btnSize);
        btnEliminar.setPreferredSize(btnSize);
        btnRecargar.setPreferredSize(btnSize);
        btnExportarExcel.setPreferredSize(btnSize);
    }

    private JPanel crearBarraSuperior() {
        // FlowLayout con alineación a la derecha y espaciado consistente
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
        JLabel lblBuscar = new JLabel("Proveedor / CUIT:");
        lblBuscar.setFont(labelFont);
        panelBusqueda.add(lblBuscar, gbcB);

        gbcB.gridy = 1;
        panelBusqueda.add(txtBuscar, gbcB);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weighty = 0.0;
        panelIzquierdo.add(panelBusqueda, gbc);

        // --- SECCIÓN 2: DETALLES DEL PROVEEDOR ---
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder("Detalles del Proveedor"));
        GridBagConstraints gbcF = new GridBagConstraints();
        gbcF.insets = new Insets(6, 6, 6, 6);
        gbcF.fill = GridBagConstraints.HORIZONTAL;
        gbcF.weightx = 1.0;

        // Nombre
        gbcF.gridx = 0; gbcF.gridy = 0;
        JLabel lblNombre = new JLabel("Nombre / Razón Social:");
        lblNombre.setFont(labelFont);
        panelForm.add(lblNombre, gbcF);

        gbcF.gridy = 1;
        panelForm.add(txtNombre, gbcF);

        // CUIT
        gbcF.gridy = 2;
        JLabel lblCuit = new JLabel("CUIT:");
        lblCuit.setFont(labelFont);
        panelForm.add(lblCuit, gbcF);

        gbcF.gridy = 3;
        panelForm.add(txtCuit, gbcF);

        // Contacto
        gbcF.gridy = 4;
        JLabel lblContacto = new JLabel("Dirección / Contacto:");
        lblContacto.setFont(labelFont);
        panelForm.add(lblContacto, gbcF);

        gbcF.gridy = 5;
        panelForm.add(txtContacto, gbcF);

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
        btnGuardar.addActionListener(e -> guardarOActualizarProveedor());
        btnEliminar.addActionListener(e -> eliminarProveedor());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnRecargar.addActionListener(e -> cargarProveedores());
        btnExportarExcel.addActionListener(e -> exportarProveedoresExcel());

        // Filtrado en tiempo real normalizando acentos y mayúsculas/minúsculas
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

        tablaProveedores.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaProveedores.getSelectedRow();
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

    private void exportarProveedoresExcel() {
        if (tablaProveedores.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay proveedores en la tabla para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ExporterExcel.exportarTabla(tablaProveedores, "Listado_Proveedores");
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

    private void cargarFilaSeleccionada(int filaVista) {
        int filaModelo = tablaProveedores.convertRowIndexToModel(filaVista);
        txtId.setText(String.valueOf(tableModel.getValueAt(filaModelo, 0)));
        txtNombre.setText(String.valueOf(tableModel.getValueAt(filaModelo, 1)));
        txtCuit.setText(String.valueOf(tableModel.getValueAt(filaModelo, 2)));
        Object contacto = tableModel.getValueAt(filaModelo, 3);
        txtContacto.setText(contacto != null ? contacto.toString() : "");
        
        txtCuit.setEditable(true); 
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
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(this, "Ya existe un proveedor con ese CUIT.", "CUIT duplicado", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                JOptionPane.showMessageDialog(this,
                    "No se puede eliminar: el proveedor tiene Órdenes de Compra vinculadas.",
                    "Proveedor en uso", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error de base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}