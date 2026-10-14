package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import persistencia.ReporteDAO;
import persistencia.ReporteDAOImpl;
import util.ExporterExcel;

public class GestionReportePanel extends JPanel {

    // Variables y Constructor

    private final ReporteDAO reporteDAO = new ReporteDAOImpl();
    private JButton btnConsultarStock;
    private JButton btnExportarExcel;
    private JTable tablaReporte;
    private DefaultTableModel tableModel;
    private JLabel lblTituloReporte; 
    private JLabel lblTotalProductos;
    private JLabel lblTotalCriticos;

    public GestionReportePanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        inicializarModeloTabla();
        inicializarComponentes();
        configurarEstiloYAnchosTabla();
        add(crearPanelControles(), BorderLayout.NORTH); 
        add(new JScrollPane(tablaReporte), BorderLayout.CENTER);
        add(crearPanelResumenInferior(), BorderLayout.SOUTH);
        agregarListeners();
        cargarStockConsolidado(); 
    }

    private void inicializarModeloTabla() {
        String[] columnas = {"ID Ítem", "Nombre / Descripción", "Stock total", "Mínimo recomendado", "Estado"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 2 || columnIndex == 3) return Integer.class;
                return String.class;
            }
        };
        tablaReporte = new JTable(tableModel);
        tablaReporte.setFillsViewportHeight(true);
        tablaReporte.setRowHeight(28);
    }

    private void inicializarComponentes() {
        btnConsultarStock = new JButton("🔍 Actualizar reporte");
        btnConsultarStock.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnExportarExcel = new JButton("Exportar a Excel");
        btnExportarExcel.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTituloReporte = new JLabel("Reporte de stock consolidado multi-sucursal", SwingConstants.LEFT);
        lblTituloReporte.setFont(new Font("Segoe UI", Font.BOLD, 18)); 
        lblTotalProductos = new JLabel("Total Productos: 0");
        lblTotalProductos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalCriticos = new JLabel("En alerta crítica: 0");
        lblTotalCriticos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalCriticos.setForeground(new Color(255, 100, 100));
    }

    private void configurarEstiloYAnchosTabla() {
        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer renderDerecha = new DefaultTableCellRenderer();
        renderDerecha.setHorizontalAlignment(SwingConstants.RIGHT);
        DefaultTableCellRenderer renderIzquierda = new DefaultTableCellRenderer();
        renderIzquierda.setHorizontalAlignment(SwingConstants.LEFT);
        DefaultTableCellRenderer renderEstado = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(getFont().deriveFont(Font.BOLD));
                if (value != null) {
                    String estado = value.toString();
                    if (estado.contains("CRÍTICO")) {
                        c.setForeground(new Color(255, 110, 110));
                    } else {
                        c.setForeground(new Color(110, 255, 110));
                    }
                }
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                } else {
                    c.setBackground(table.getBackground());
                }
                return c;
            }
        };
        tablaReporte.getColumnModel().getColumn(0).setCellRenderer(renderCentro);
        tablaReporte.getColumnModel().getColumn(1).setCellRenderer(renderIzquierda);
        tablaReporte.getColumnModel().getColumn(2).setCellRenderer(renderDerecha);
        tablaReporte.getColumnModel().getColumn(3).setCellRenderer(renderDerecha);
        tablaReporte.getColumnModel().getColumn(4).setCellRenderer(renderEstado);
        tablaReporte.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablaReporte.getColumnModel().getColumn(1).setPreferredWidth(450);
        tablaReporte.getColumnModel().getColumn(2).setPreferredWidth(110);
        tablaReporte.getColumnModel().getColumn(3).setPreferredWidth(140);
        tablaReporte.getColumnModel().getColumn(4).setPreferredWidth(120);
    }

    private JPanel crearPanelControles() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBotones.add(btnConsultarStock);
        panelBotones.add(btnExportarExcel);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Herramientas de consulta"),
            new EmptyBorder(5, 5, 5, 5)
        ));
        panel.add(lblTituloReporte, BorderLayout.WEST);
        panel.add(panelBotones, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearPanelResumenInferior() {
        JPanel panelResumen = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 10));
        panelResumen.setBorder(BorderFactory.createTitledBorder("Resumen de inventario"));
        panelResumen.add(lblTotalProductos);
        panelResumen.add(lblTotalCriticos);
        return panelResumen;
    }

    private void agregarListeners() {
        btnConsultarStock.addActionListener(e -> cargarStockConsolidado());
        btnExportarExcel.addActionListener(e -> exportarReporteExcel());
    }

    private void exportarReporteExcel() {
        if (tablaReporte.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos en el reporte para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ExporterExcel.exportarTabla(tablaReporte, "Reporte_Stock_Consolidado");
    }

    public void cargarStockConsolidado() {
        int contadorCriticos = 0;
        int totalProductos = 0;
        try {
            List<String> sucursales = reporteDAO.obtenerNombresSucursales();
            List<String> columnas = new ArrayList<>();
            columnas.add("ID Ítem");
            columnas.add("Nombre / Descripción");
            columnas.add("Stock total");
            columnas.addAll(sucursales);
            columnas.add("Mínimo recomendado");
            tableModel = new DefaultTableModel(columnas.toArray(), 0) {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
                @Override
                public Class<?> getColumnClass(int col) {
                    if (col == 1) return String.class;
                    return Integer.class;
                }
            };
            tablaReporte.setModel(tableModel);
            List<Object[]> datosReporte = reporteDAO.consultarStockPorSucursal(sucursales);
            totalProductos = datosReporte.size();
            for (Object[] fila : datosReporte) {
                tableModel.addRow(fila);
            }
            // Mismo criterio que el dashboard: producto/sucursal con stock por debajo del mínimo (incluye agotados)
            contadorCriticos = reporteDAO.contarStockCritico(null);

            configurarEstiloYAnchosDinamicos(sucursales.size());
            lblTotalProductos.setText("Total Productos Ofertados: " + totalProductos);
            lblTotalCriticos.setText("En alerta crítica: " + contadorCriticos);
            if (datosReporte.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron ítems de stock para reportar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al generar el reporte de stock: " + e.getMessage(), "Error de DB/Datos", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private void configurarEstiloYAnchosDinamicos(int cantidadSucursales) {
        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer renderDerecha = new DefaultTableCellRenderer();
        renderDerecha.setHorizontalAlignment(SwingConstants.RIGHT);
        DefaultTableCellRenderer renderIzquierda = new DefaultTableCellRenderer();
        renderIzquierda.setHorizontalAlignment(SwingConstants.LEFT);
        DefaultTableCellRenderer renderSucursales = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (value != null && value instanceof Number) {
                    int stockSucursal = ((Number) value).intValue();
                    int totalColumnas = table.getColumnCount();
                    int columnaMinimo = totalColumnas - 1; 
                    Object minimoValue = table.getValueAt(row, columnaMinimo);
                    if (minimoValue != null && minimoValue instanceof Number) {
                        int stockMinimo = ((Number) minimoValue).intValue();
                        if (stockSucursal < stockMinimo) {
                            c.setForeground(new Color(255, 110, 110));
                            setFont(getFont().deriveFont(Font.BOLD));
                        } else {
                            c.setForeground(new Color(110, 255, 110));
                            setFont(getFont().deriveFont(Font.PLAIN));
                        }
                    }
                    if (stockSucursal == 0) {
                        c.setForeground(new Color(140, 140, 140));
                        setFont(getFont().deriveFont(Font.PLAIN));
                    }
                }
                c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return c;
            }
        };
        int totalColumnas = tablaReporte.getColumnCount();
        tablaReporte.getColumnModel().getColumn(0).setCellRenderer(renderCentro);
        tablaReporte.getColumnModel().getColumn(1).setCellRenderer(renderIzquierda);
        tablaReporte.getColumnModel().getColumn(2).setCellRenderer(renderDerecha);
        for (int i = 0; i < cantidadSucursales; i++) {
            tablaReporte.getColumnModel().getColumn(3 + i).setCellRenderer(renderSucursales);
        }
        tablaReporte.getColumnModel().getColumn(totalColumnas - 1).setCellRenderer(renderDerecha); 
        tablaReporte.getColumnModel().getColumn(0).setPreferredWidth(90);
        tablaReporte.getColumnModel().getColumn(1).setPreferredWidth(350);
        tablaReporte.getColumnModel().getColumn(2).setPreferredWidth(100);
        for (int i = 0; i < cantidadSucursales; i++) {
            tablaReporte.getColumnModel().getColumn(3 + i).setPreferredWidth(120);
        }
        tablaReporte.getColumnModel().getColumn(totalColumnas - 1).setPreferredWidth(140);
    }
}