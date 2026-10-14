package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import persistencia.DashboardDAO;
import persistencia.DashboardDAOImpl;
import util.ExporterExcel;

public class GestionDashboardPanel extends JPanel {

    private final DashboardDAO dashboardDAO = new DashboardDAOImpl();

    // Filtros
    private JComboBox<ItemSucursal> cmbSucursal;
    private JComboBox<ItemMes> cmbMes;

    // KPIs
    private JLabel lblKpiVentas;
    private JLabel lblKpiTicketPromedio;
    private JLabel lblKpiCriticos;
    private JLabel lblKpiPedidos;

    // Contenedores de Gráficos
    private JPanel panelGraficoTicketPromedio;
    private JPanel panelGraficoDiaSemana;
    private JPanel panelGraficoHorarios;
    private JPanel panelGraficoTopProductos;

    // Clase auxiliar para Sucursales
    private static class ItemSucursal {
        private final Integer id;
        private final String nombre;

        public ItemSucursal(Integer id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public Integer getId() { return id; }

        @Override
        public String toString() { return nombre; }
    }

    // Clase auxiliar para Meses
    private static class ItemMes {
        private final Integer numeroMes; // 1 = Enero, 12 = Diciembre, null = Todo el año
        private final String nombre;

        public ItemMes(Integer numeroMes, String nombre) {
            this.numeroMes = numeroMes;
            this.nombre = nombre;
        }

        public Integer getNumeroMes() { return numeroMes; }

        @Override
        public String toString() { return nombre; }
    }

    public GestionDashboardPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setBackground(new Color(45, 45, 45));

        // Encabezado
        add(crearHeader(), BorderLayout.NORTH);

        // Cuerpo directo (sin JScrollPane)
        JPanel panelCuerpo = new JPanel(new BorderLayout(8, 8));
        panelCuerpo.setOpaque(false);
        panelCuerpo.add(crearPanelKPIs(), BorderLayout.NORTH);
        panelCuerpo.add(crearGridGraficos(), BorderLayout.CENTER);

        add(panelCuerpo, BorderLayout.CENTER);

        cargarSucursales();
        cargarComboMeses();
        cargarDatosDashboard();
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titulo = new JLabel("Tablero de Control Gerencial (Dashboard)");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelAcciones.setOpaque(false);

        // Selector de Sucursal
        JLabel lblSucursal = new JLabel("Sucursal:");
        lblSucursal.setForeground(Color.WHITE);
        lblSucursal.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        cmbSucursal = new JComboBox<>();
        cmbSucursal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbSucursal.setPreferredSize(new Dimension(160, 26));
        cmbSucursal.addActionListener(e -> cargarDatosDashboard());

        // Selector de Mes
        JLabel lblMes = new JLabel("Mes:");
        lblMes.setForeground(Color.WHITE);
        lblMes.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        cmbMes = new JComboBox<>();
        cmbMes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbMes.setPreferredSize(new Dimension(130, 26));
        cmbMes.addActionListener(e -> cargarDatosDashboard());

        // Botón Actualizar
        JButton btnActualizar = new JButton("Actualizar Datos");
        btnActualizar.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnActualizar.addActionListener(e -> cargarDatosDashboard());

        // Botón Exportar a Excel
        JButton btnExportar = new JButton("Exportar a Excel");
        btnExportar.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnExportar.addActionListener(e -> exportarResumenDashboard());

        panelAcciones.add(lblSucursal);
        panelAcciones.add(cmbSucursal);
        panelAcciones.add(lblMes);
        panelAcciones.add(cmbMes);
        panelAcciones.add(btnActualizar);
        panelAcciones.add(btnExportar);

        header.add(titulo, BorderLayout.WEST);
        header.add(panelAcciones, BorderLayout.EAST);
        return header;
    }

    private void cargarComboMeses() {
        cmbMes.removeAllItems();
        cmbMes.addItem(new ItemMes(null, "Todo el Año"));
        cmbMes.addItem(new ItemMes(1, "Enero"));
        cmbMes.addItem(new ItemMes(2, "Febrero"));
        cmbMes.addItem(new ItemMes(3, "Marzo"));
        cmbMes.addItem(new ItemMes(4, "Abril"));
        cmbMes.addItem(new ItemMes(5, "Mayo"));
        cmbMes.addItem(new ItemMes(6, "Junio"));
        cmbMes.addItem(new ItemMes(7, "Julio"));
        cmbMes.addItem(new ItemMes(8, "Agosto"));
        cmbMes.addItem(new ItemMes(9, "Septiembre"));
        cmbMes.addItem(new ItemMes(10, "Octubre"));
        cmbMes.addItem(new ItemMes(11, "Noviembre"));
        cmbMes.addItem(new ItemMes(12, "Diciembre"));

        // Selecciona Agosto por defecto si coincide con el mes actual
        cmbMes.setSelectedIndex(8); 
    }

    private void cargarSucursales() {
        cmbSucursal.removeAllItems();
        cmbSucursal.addItem(new ItemSucursal(0, "Todas las sucursales"));

        try {
            Map<Integer, String> sucursales = dashboardDAO.obtenerSucursales();
            for (Map.Entry<Integer, String> entry : sucursales.entrySet()) {
                cmbSucursal.addItem(new ItemSucursal(entry.getKey(), entry.getValue()));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error cargando sucursales: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Integer getSucursalSeleccionadaId() {
        ItemSucursal item = (ItemSucursal) cmbSucursal.getSelectedItem();
        if (item != null && item.getId() > 0) {
            return item.getId();
        }
        return null;
    }

    private Integer getMesSeleccionado() {
        ItemMes item = (ItemMes) cmbMes.getSelectedItem();
        return (item != null) ? item.getNumeroMes() : null;
    }

    private JPanel crearPanelKPIs() {
        JPanel panelKPIs = new JPanel(new GridLayout(1, 4, 10, 0));
        panelKPIs.setOpaque(false);

        lblKpiVentas = new JLabel("$0.00", SwingConstants.CENTER);
        lblKpiTicketPromedio = new JLabel("$0.00", SwingConstants.CENTER);
        lblKpiCriticos = new JLabel("0", SwingConstants.CENTER);
        lblKpiPedidos = new JLabel("0", SwingConstants.CENTER);

        panelKPIs.add(crearTarjetaKPI("Ventas del Período", lblKpiVentas, new Color(46, 204, 113)));
        panelKPIs.add(crearTarjetaKPI("Ticket Promedio", lblKpiTicketPromedio, new Color(52, 152, 219)));
        panelKPIs.add(crearTarjetaKPI("Alertas Stock Crítico", lblKpiCriticos, new Color(231, 76, 60)));
        panelKPIs.add(crearTarjetaKPI("Pedidos Pendientes", lblKpiPedidos, new Color(241, 196, 15)));

        return panelKPIs;
    }

    private JPanel crearTarjetaKPI(String titulo, JLabel lblValor, Color colorBorde) {
        JPanel card = new JPanel(new BorderLayout(2, 2));
        card.setBackground(new Color(60, 63, 65));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, colorBorde),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitulo.setForeground(new Color(180, 180, 180));

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValor.setForeground(Color.WHITE);

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearGridGraficos() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        grid.setOpaque(false);

        panelGraficoTicketPromedio = crearContenedorGrafico("Evolución Ticket Promedio ($)");
        panelGraficoDiaSemana = crearContenedorGrafico("Ventas por Día de la Semana");
        panelGraficoHorarios = crearContenedorGrafico("Horarios de Mayor Concurrencia");
        panelGraficoTopProductos = crearContenedorGrafico("Top 5 Productos Más Vendidos");

        grid.add(panelGraficoTicketPromedio);
        grid.add(panelGraficoDiaSemana);
        grid.add(panelGraficoHorarios);
        grid.add(panelGraficoTopProductos);

        return grid;
    }

    private JPanel crearContenedorGrafico(String titulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(60, 63, 65));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                new EmptyBorder(6, 6, 6, 6)
        ));

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.LEFT);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 4, 0));

        panel.add(lblTitulo, BorderLayout.NORTH);
        return panel;
    }

    public void cargarDatosDashboard() {
        try {
            Integer idSucursal = getSucursalSeleccionadaId();
            Integer mes = getMesSeleccionado();
            NumberFormat fmtMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

            // 1. KPIs
            double ventasMes = dashboardDAO.obtenerVentasTotalesMes(idSucursal, mes);
            double ticketProm = dashboardDAO.obtenerTicketPromedio(idSucursal, mes);
            int criticos = dashboardDAO.obtenerAlertasStockCritico(idSucursal);
            int pendientes = dashboardDAO.obtenerPedidosPendientes(idSucursal);

            lblKpiVentas.setText(fmtMoneda.format(ventasMes));
            lblKpiTicketPromedio.setText(fmtMoneda.format(ticketProm));
            lblKpiCriticos.setText(String.valueOf(criticos));
            lblKpiPedidos.setText(String.valueOf(pendientes));

            // 2. Gráfico 1: Evolución Ticket Promedio ($)
            Map<String, Double> ticketPromMeses = dashboardDAO.obtenerTicketPromedioUltimosMeses(idSucursal);
            DefaultCategoryDataset dsTicket = new DefaultCategoryDataset();
            if (ticketPromMeses != null) {
                ticketPromMeses.forEach((m, monto) -> dsTicket.addValue(monto, "Ticket Prom.", m));
            }
            JFreeChart chartTicket = ChartFactory.createLineChart(null, "Mes", "Monto ($)", dsTicket);
            estilitarGraficoOscuro(chartTicket);
            actualizarPanel(panelGraficoTicketPromedio, new ChartPanel(chartTicket));

            // 3. Gráfico 2: Ventas por Día de la Semana
            Map<String, Integer> ventasDia = dashboardDAO.obtenerVentasPorDiaSemana(idSucursal, mes);
            DefaultCategoryDataset dsDia = new DefaultCategoryDataset();
            if (ventasDia != null) {
                ventasDia.forEach((dia, cant) -> dsDia.addValue(cant, "Ventas", dia));
            }
            JFreeChart chartDia = ChartFactory.createBarChart(null, "Día", "Cantidad", dsDia);
            estilitarGraficoOscuro(chartDia);
            actualizarPanel(panelGraficoDiaSemana, new ChartPanel(chartDia));

            // 4. Gráfico 3: Horarios Más Concurridos
            Map<String, Integer> horarios = dashboardDAO.obtenerHorariosConcurridos(idSucursal, mes);
            DefaultCategoryDataset dsHorarios = new DefaultCategoryDataset();
            if (horarios != null) {
                horarios.forEach((hora, cant) -> dsHorarios.addValue(cant, "Transacciones", hora));
            }
            JFreeChart chartHorarios = ChartFactory.createLineChart(null, "Hora", "Transacciones", dsHorarios);
            estilitarGraficoOscuro(chartHorarios);
            
            CategoryPlot plotHorarios = (CategoryPlot) chartHorarios.getPlot();
            plotHorarios.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
            
            actualizarPanel(panelGraficoHorarios, new ChartPanel(chartHorarios));

            // 5. Gráfico 4: Top 5 Productos Más Vendidos
            Map<String, Integer> topProd = dashboardDAO.obtenerTop5ProductosVendidosMes(idSucursal, mes);
            DefaultCategoryDataset dsTop = new DefaultCategoryDataset();
            if (topProd != null) {
                topProd.forEach((prod, cant) -> dsTop.addValue(cant, "Vendidos", prod));
            }
            JFreeChart chartTop = ChartFactory.createBarChart(null, "Producto", "Cant.", dsTop, PlotOrientation.HORIZONTAL, false, true, false);
            estilitarGraficoOscuro(chartTop);
            actualizarPanel(panelGraficoTopProductos, new ChartPanel(chartTop));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error cargando métricas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

            private void exportarResumenDashboard() {
        DefaultTableModel modelResumen = new DefaultTableModel(new Object[]{"Métrica / Indicador", "Valor"}, 0);
        
        modelResumen.addRow(new Object[]{"Sucursal", cmbSucursal.getSelectedItem().toString()});
        modelResumen.addRow(new Object[]{"Mes", cmbMes.getSelectedItem().toString()});
        modelResumen.addRow(new Object[]{"Ventas del Período", lblKpiVentas.getText()});
        modelResumen.addRow(new Object[]{"Ticket Promedio", lblKpiTicketPromedio.getText()});
        modelResumen.addRow(new Object[]{"Alertas Stock Crítico", " " + lblKpiCriticos.getText()});
        modelResumen.addRow(new Object[]{"Pedidos Pendientes", " " + lblKpiPedidos.getText()});

        JTable tablaResumen = new JTable(modelResumen);
        ExporterExcel.exportarTabla(tablaResumen, "Resumen_Dashboard");
    }

    private void actualizarPanel(JPanel panelContenedor, ChartPanel chartPanel) {
        Component headerExistente = null;
        if (panelContenedor.getComponentCount() > 0) {
            headerExistente = panelContenedor.getComponent(0);
        }

        panelContenedor.removeAll();

        if (headerExistente != null) {
            panelContenedor.add(headerExistente, BorderLayout.NORTH);
        }
        panelContenedor.add(chartPanel, BorderLayout.CENTER);

        panelContenedor.revalidate();
        panelContenedor.repaint();
    }

    private void estilitarGraficoOscuro(JFreeChart chart) {
        Color fondoOscuro = new Color(60, 63, 65);
        Color textoClaro = new Color(220, 220, 220);

        chart.setBackgroundPaint(fondoOscuro);

        if (chart.getPlot() instanceof CategoryPlot) {
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setBackgroundPaint(fondoOscuro);
            plot.setDomainGridlinePaint(new Color(90, 90, 90));
            plot.setRangeGridlinePaint(new Color(90, 90, 90));
            plot.getDomainAxis().setTickLabelPaint(textoClaro);
            plot.getDomainAxis().setLabelPaint(textoClaro);
            plot.getRangeAxis().setTickLabelPaint(textoClaro);
            plot.getRangeAxis().setLabelPaint(textoClaro);
        } else if (chart.getPlot() instanceof PiePlot) {
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setBackgroundPaint(fondoOscuro);
            plot.setLabelBackgroundPaint(fondoOscuro);
            plot.setLabelPaint(textoClaro);
        }

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(fondoOscuro);
            chart.getLegend().setItemPaint(textoClaro);
        }
    }
}