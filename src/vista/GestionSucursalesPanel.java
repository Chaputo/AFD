package vista;

import modelo.Sucursal;
import persistencia.SucursalDAO;
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

public class GestionSucursalesPanel extends JPanel {

    private JTextField txtId, txtNombre, txtDireccion, txtBuscar;
    private JButton btnGuardar, btnEliminar, btnLimpiar, btnExportarExcel, btnRecargar;
    private JTable tablaSucursales;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private SucursalDAO sucursalDAO;

    public GestionSucursalesPanel() {
        sucursalDAO = new SucursalDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        inicializarComponentes();

        // Tabla de Sucursales
        tableModel = new DefaultTableModel(new Object[]{"ID", "Nombre de Sucursal", "Dirección", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaSucursales = new JTable(tableModel);
        tablaSucursales.setRowHeight(24);
        tablaSucursales.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Sorter para filtrado / búsqueda en tiempo real
        sorter = new TableRowSorter<>(tableModel);
        tablaSucursales.setRowSorter(sorter);

        // Columna ID delgada
        tablaSucursales.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaSucursales.getColumnModel().getColumn(0).setMaxWidth(60);
        tablaSucursales.getColumnModel().getColumn(0).setMinWidth(30);

        JScrollPane scrollTabla = new JScrollPane(tablaSucursales);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Listado de sucursales"));

        // Panel contenedor fijo
        JPanel panelCuerpo = new JPanel(new BorderLayout(10, 0));
        JPanel panelIzquierdo = crearPanelLateralIzquierdo();
        panelIzquierdo.setPreferredSize(new Dimension(380, 0));

        panelCuerpo.add(panelIzquierdo, BorderLayout.WEST);
        panelCuerpo.add(scrollTabla, BorderLayout.CENTER);

        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(panelCuerpo, BorderLayout.CENTER);

        agregarListeners();
        cargarSucursales();

        txtId.setEditable(false);
        btnEliminar.setEnabled(false);
    }

    private void inicializarComponentes() {
        txtId = new JTextField();
        txtNombre = new JTextField();
        txtDireccion = new JTextField();
        txtBuscar = new JTextField();

        Dimension btnSize = new Dimension(160, 32);

        btnGuardar = new JButton("Guardar / Actualizar");
        btnLimpiar = new JButton("Limpiar / Nuevo");
        btnEliminar = new JButton("Desactivar Sucursal");
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
        JLabel lblBuscar = new JLabel("Nombre / Dirección:");
        lblBuscar.setFont(labelFont);
        panelBusqueda.add(lblBuscar, gbcB);

        gbcB.gridy = 1;
        panelBusqueda.add(txtBuscar, gbcB);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weighty = 0.0;
        panelIzquierdo.add(panelBusqueda, gbc);

        // --- SECCIÓN 2: DETALLES DE LA SUCURSAL ---
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder("Detalles de la Sucursal"));
        GridBagConstraints gbcF = new GridBagConstraints();
        gbcF.insets = new Insets(6, 6, 6, 6);
        gbcF.fill = GridBagConstraints.HORIZONTAL;
        gbcF.weightx = 1.0;

        // Nombre
        gbcF.gridx = 0; gbcF.gridy = 0;
        JLabel lblNombre = new JLabel("Nombre de Sucursal:");
        lblNombre.setFont(labelFont);
        panelForm.add(lblNombre, gbcF);

        gbcF.gridy = 1;
        panelForm.add(txtNombre, gbcF);

        // Dirección
        gbcF.gridy = 2;
        JLabel lblDireccion = new JLabel("Dirección:");
        lblDireccion.setFont(labelFont);
        panelForm.add(lblDireccion, gbcF);

        gbcF.gridy = 3;
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
        btnGuardar.addActionListener(e -> guardarOActualizarSucursal());
        btnEliminar.addActionListener(e -> cambiarEstadoSucursal());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnRecargar.addActionListener(e -> cargarSucursales());
        btnExportarExcel.addActionListener(e -> exportarExcel());

        // Búsqueda rápida ignorando acentos y mayúsculas
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

        tablaSucursales.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaSucursales.getSelectedRow();
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
        if (tablaSucursales.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay sucursales registradas para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ExporterExcel.exportarTabla(tablaSucursales, "Listado_Sucursales");
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtDireccion.setText("");
        tablaSucursales.clearSelection();
        btnEliminar.setText("Desactivar Sucursal");
        btnEliminar.setEnabled(false);
    }

    private void cargarFilaSeleccionada(int filaVista) {
        int filaModelo = tablaSucursales.convertRowIndexToModel(filaVista);
        txtId.setText(String.valueOf(tableModel.getValueAt(filaModelo, 0)));
        txtNombre.setText(String.valueOf(tableModel.getValueAt(filaModelo, 1)));

        Object dir = tableModel.getValueAt(filaModelo, 2);
        txtDireccion.setText(dir != null ? dir.toString() : "");

        // El botón activa o desactiva según el estado de la sucursal elegida
        btnEliminar.setText(ESTADO_ACTIVA.equals(tableModel.getValueAt(filaModelo, 3)) ? "Desactivar Sucursal" : "Activar Sucursal");
        btnEliminar.setEnabled(true);
    }

    public void cargarSucursales() {
        tableModel.setRowCount(0);
        try {
            List<Sucursal> sucursales = sucursalDAO.listarTodas();
            for (Sucursal s : sucursales) {
                tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getNombre(),
                    s.getDireccion(),
                    s.isActiva() ? ESTADO_ACTIVA : ESTADO_DESACTIVADA
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar sucursales: " + e.getMessage(), "Error de DB", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void guardarOActualizarSucursal() {
        String nombre = txtNombre.getText().trim();
        String direccion = txtDireccion.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de la sucursal es obligatorio.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Sucursal sucursal;
            if (txtId.getText().isEmpty()) {
                sucursal = new Sucursal(nombre, direccion);
            } else {
                int id = Integer.parseInt(txtId.getText());
                sucursal = new Sucursal(id, nombre, direccion);
            }

            boolean exito = sucursalDAO.guardarOActualizar(sucursal);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Sucursal guardada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarSucursales();
                notificarModificacion();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo guardar/actualizar la sucursal.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de base de datos al guardar la sucursal.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error de formato en los identificadores.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static final String ESTADO_ACTIVA = "Activa";
    private static final String ESTADO_DESACTIVADA = "Desactivada";

    private void cambiarEstadoSucursal() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una sucursal.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        String nombreSucursal = txtNombre.getText().trim();
        boolean desactivar = btnEliminar.getText().startsWith("Desactivar");

        // No se puede desactivar la sucursal en la que se está trabajando
        Window ventana = SwingUtilities.getWindowAncestor(this);
        if (desactivar && ventana instanceof MainFrame && ((MainFrame) ventana).getIdSucursalSesion() == id) {
            JOptionPane.showMessageDialog(this, "No se puede desactivar la sucursal de la sesión actual.", "Acción denegada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String mensaje = desactivar
            ? "¿Desactivar la sucursal \"" + nombreSucursal + "\"?\n\n"
              + "Dejará de mostrarse en el sistema (login, ventas, stock, reportes y dashboard).\n"
              + "Sus datos se conservan y vuelven a verse si la reactivás."
            : "¿Activar la sucursal \"" + nombreSucursal + "\"?\n\nVolverá a estar disponible en todo el sistema.";
        int confirm = JOptionPane.showConfirmDialog(this, mensaje, desactivar ? "Desactivar sucursal" : "Activar sucursal",
            JOptionPane.YES_NO_OPTION, desactivar ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            if (sucursalDAO.cambiarEstado(id, !desactivar)) {
                JOptionPane.showMessageDialog(this, "Sucursal " + (desactivar ? "desactivada." : "activada."), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarSucursales();
                notificarModificacion();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo cambiar el estado de la sucursal.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se pudo cambiar el estado", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void notificarModificacion() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (parentWindow instanceof MainFrame) {
            ((MainFrame) parentWindow).notificarCambioSucursales();
        }
    }
}