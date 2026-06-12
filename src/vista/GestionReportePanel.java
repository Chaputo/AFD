package vista;

import persistencia.ReporteDAO;
import persistencia.ReporteDAOImpl;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GestionReportePanel extends JPanel {

    private final ReporteDAO reporteDAO = new ReporteDAOImpl();

    private JButton btnConsultarStock;
    private JTable tablaReporte;
    private DefaultTableModel tableModel;
    private JLabel lblTituloReporte; 
    
    // Nuevas etiquetas para la barra de resumen (KPIs)
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
        add(crearPanelResumenInferior(), BorderLayout.SOUTH); // Barra inferior agregada

        agregarListeners();
        cargarStockConsolidado(); 
    }

    private void inicializarModeloTabla() {
        String[] columnas = {"ID Ítem", "Nombre / Descripción", "Stock Total", "Mínimo Recomendado", "Estado"};
        
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
        tablaReporte.setRowHeight(28); // Un poquito más alto para dar aire al diseño
    }

    private void inicializarComponentes() {
        btnConsultarStock = new JButton("🔍 Actualizar Reporte");
        btnConsultarStock.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        lblTituloReporte = new JLabel("Reporte de Stock Consolidado Multi-Sucursal", SwingConstants.LEFT);
        lblTituloReporte.setFont(new Font("Segoe UI", Font.BOLD, 18)); 
        
        // Inicializar labels del resumen
        lblTotalProductos = new JLabel("Total Productos: 0");
        lblTotalProductos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        lblTotalCriticos = new JLabel("En Alerta Crítica: 0");
        lblTotalCriticos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalCriticos.setForeground(new Color(255, 100, 100)); // Rojo suave para modo oscuro
    }

    private void configurarEstiloYAnchosTabla() {
        // Renderizadores de alineación
        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        
        DefaultTableCellRenderer renderDerecha = new DefaultTableCellRenderer();
        renderDerecha.setHorizontalAlignment(SwingConstants.RIGHT);
        
        DefaultTableCellRenderer renderIzquierda = new DefaultTableCellRenderer();
        renderIzquierda.setHorizontalAlignment(SwingConstants.LEFT);

        // Renderizador personalizado con color para la columna "Estado"
        DefaultTableCellRenderer renderEstado = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(getFont().deriveFont(Font.BOLD)); // Texto en negrita para el estado
                
                if (value != null) {
                    String estado = value.toString();
                    if (estado.contains("CRÍTICO")) {
                        c.setForeground(new Color(255, 110, 110)); // Rojo vivo
                    } else {
                        c.setForeground(new Color(110, 255, 110)); // Verde vivo
                    }
                }
                
                // Si la fila está seleccionada, mantenemos el color de selección de FlatLaf de fondo
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                } else {
                    c.setBackground(table.getBackground());
                }
                return c;
            }
        };

        // Asignar alineaciones a las columnas
        tablaReporte.getColumnModel().getColumn(0).setCellRenderer(renderCentro);     // ID
        tablaReporte.getColumnModel().getColumn(1).setCellRenderer(renderIzquierda);  // Nombre
        tablaReporte.getColumnModel().getColumn(2).setCellRenderer(renderDerecha);    // Stock Total
        tablaReporte.getColumnModel().getColumn(3).setCellRenderer(renderDerecha);    // Mínimo
        tablaReporte.getColumnModel().getColumn(4).setCellRenderer(renderEstado);     // Estado con color

        // Asignar anchos proporcionales (Evita que queden todas iguales)
        tablaReporte.getColumnModel().getColumn(0).setPreferredWidth(80);   // ID Ítem
        tablaReporte.getColumnModel().getColumn(1).setPreferredWidth(450);  // Nombre / Descripción (El más amplio)
        tablaReporte.getColumnModel().getColumn(2).setPreferredWidth(110);  // Stock Total
        tablaReporte.getColumnModel().getColumn(3).setPreferredWidth(140);  // Mínimo Recomendado
        tablaReporte.getColumnModel().getColumn(4).setPreferredWidth(120);  // Estado
    }

    private JPanel crearPanelControles() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBotones.add(btnConsultarStock);
        
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Herramientas de Consulta"),
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        panel.add(lblTituloReporte, BorderLayout.WEST);
        panel.add(panelBotones, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel crearPanelResumenInferior() {
        JPanel panelResumen = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 10));
        panelResumen.setBorder(BorderFactory.createTitledBorder("Resumen de Inventario"));
        
        panelResumen.add(lblTotalProductos);
        panelResumen.add(lblTotalCriticos);
        
        return panelResumen;
    }

    private void agregarListeners() {
        btnConsultarStock.addActionListener(e -> cargarStockConsolidado());
    }

    private void cargarStockConsolidado() {
    int contadorCriticos = 0;
    int totalProductos = 0;

    try {
        // 1. Buscamos las sucursales vivas a través del DAO
        List<String> sucursales = reporteDAO.obtenerNombresSucursales();

        // 2. Definimos las cabeceras de columnas dinámicas
        List<String> columnas = new ArrayList<>();
        columnas.add("ID Ítem");
        columnas.add("Nombre / Descripción");
        columnas.add("Stock Total");
        columnas.addAll(sucursales); // Metemos las sucursales al medio
        columnas.add("Mínimo Recomendado");
        columnas.add("Estado");

        // 3. Reseteamos el modelo de datos de la tabla
        tableModel = new DefaultTableModel(columnas.toArray(), 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
            @Override
            public Class<?> getColumnClass(int col) {
                // Columna de Nombre (1) y Estado (última) son Strings, las demás numéricas
                if (col == 1 || col == getColumnCount() - 1) return String.class;
                return Integer.class;
            }
        };
        tablaReporte.setModel(tableModel);

        // 4. Traemos las filas procesadas del Query PIVOT
        List<Object[]> datosReporte = reporteDAO.consultarStockPorSucursal(sucursales);
        totalProductos = datosReporte.size();

        for (Object[] fila : datosReporte) {
            // Evaluamos la última celda del array para saber si incrementamos el KPI crítico
            String estado = (String) fila[fila.length - 1];
            if (estado.contains("CRÍTICO")) {
                contadorCriticos++;
            }
            tableModel.addRow(fila);
        }

        // 5. Re-ajustamos anchos y renderizadores dinámicos en la UI
        configurarEstiloYAnchosDinamicos(sucursales.size());

        // Actualizamos las etiquetas inferiores de control
        lblTotalProductos.setText("Total Productos Ofertados: " + totalProductos);
        lblTotalCriticos.setText("En Alerta Crítica: " + contadorCriticos);

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

        // Renderizador para el semáforo de la columna "Estado"
        DefaultTableCellRenderer renderEstado = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(getFont().deriveFont(Font.BOLD));
                
                if (value != null) {
                    if (value.toString().contains("CRÍTICO")) {
                        c.setForeground(new Color(255, 110, 110)); // Rojo suave para FlatLaf Dark
                    } else {
                        c.setForeground(new Color(110, 255, 110)); // Verde suave
                    }
                }
                c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return c;
            }
        };

        // Renderizador para las columnas intermedias de las sucursales (Atenúa los ceros)
        DefaultTableCellRenderer renderSucursales = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                
                if (value != null && (int) value == 0) {
                    c.setForeground(new Color(140, 140, 140)); // Gris apagado si no hay stock ahí
                } else {
                    c.setForeground(table.getForeground()); // Color de texto normal de FlatLaf
                }
                c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return c;
            }
        };

        int totalColumnas = tablaReporte.getColumnCount();
        
        // 1. Aplicamos los alineaciones a las columnas fijas iniciales
        tablaReporte.getColumnModel().getColumn(0).setCellRenderer(renderCentro);    // SKU / ID
        tablaReporte.getColumnModel().getColumn(1).setCellRenderer(renderIzquierda); // Nombre
        tablaReporte.getColumnModel().getColumn(2).setCellRenderer(renderDerecha);   // Stock Total
        
        // 2. Aplicamos el renderizador especial a las N columnas dinámicas de las sucursales
        for (int i = 0; i < cantidadSucursales; i++) {
            tablaReporte.getColumnModel().getColumn(3 + i).setCellRenderer(renderSucursales);
        }
        
        // 3. Aplicamos renderizadores a las últimas dos columnas fijas
        tablaReporte.getColumnModel().getColumn(totalColumnas - 2).setCellRenderer(renderDerecha); // Mínimo
        tablaReporte.getColumnModel().getColumn(totalColumnas - 1).setCellRenderer(renderEstado);  // Estado

        // ================= ASIGNACIÓN DE ANCHOS PROPORCIONALES =================
        tablaReporte.getColumnModel().getColumn(0).setPreferredWidth(90);  // ID Ítem (SKU)
        tablaReporte.getColumnModel().getColumn(1).setPreferredWidth(350); // Nombre / Descripción
        tablaReporte.getColumnModel().getColumn(2).setPreferredWidth(100); // Stock Total
        
        // Ancho para cada una de las sucursales del medio
        for (int i = 0; i < cantidadSucursales; i++) {
            tablaReporte.getColumnModel().getColumn(3 + i).setPreferredWidth(120);
        }
        
        tablaReporte.getColumnModel().getColumn(totalColumnas - 2).setPreferredWidth(140); // Mínimo Recomendado
        tablaReporte.getColumnModel().getColumn(totalColumnas - 1).setPreferredWidth(120); // Estado
    }
}