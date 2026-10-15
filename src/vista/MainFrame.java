package vista;

import modelo.Usuario;
import modelo.SesionUsuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private final int idSucursalSesion;

    public MainFrame(int idSucursalSesion) {
        this.idSucursalSesion = idSucursalSesion;
        Usuario usuarioActual = SesionUsuario.getUsuario();
        if (usuarioActual == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        setTitle("Sistema AFD - Gestión logística y comercial");
        LoginFrame.aplicarIcono(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1366, 768);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());
        setJMenuBar(crearMenuBar());
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
        add(tabbedPane, BorderLayout.CENTER);
        construirModulosSegunRol(usuarioActual.getRol());
        // Las pestañas de consulta se recargan al entrar, para mostrar lo hecho en otras pestañas
        tabbedPane.addChangeListener(e -> refrescarPestaniaActual());
        add(crearStatusBar(usuarioActual), BorderLayout.SOUTH);
    }

    private JMenuBar crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.setMnemonic(KeyEvent.VK_A);
        JMenuItem itemLogout = new JMenuItem("Cerrar sesión");
        itemLogout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        itemLogout.addActionListener(e -> cerrarSesion());
        JMenuItem itemSalir = new JMenuItem("Salir del sistema");
        itemSalir.addActionListener(e -> System.exit(0));
        menuArchivo.add(itemLogout);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);
        menuBar.add(menuArchivo);
        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem itemAcercaDe = new JMenuItem("Acerca de AFD");
        itemAcercaDe.addActionListener(e -> mostrarAcercaDe());
        menuAyuda.add(itemAcercaDe);
        menuBar.add(menuAyuda);
        return menuBar;
    }

    public int getIdSucursalSesion() {
        return idSucursalSesion;
    }

    private void refrescarPestaniaActual() {
        Component c = tabbedPane.getSelectedComponent();
        if (c instanceof GestionPedidosPanel) ((GestionPedidosPanel) c).recargarClientes();
        else if (c instanceof HistorialMovimientosPanel) ((HistorialMovimientosPanel) c).cargarHistorialMovimientos();
        else if (c instanceof GestionInventarioPanel) ((GestionInventarioPanel) c).refrescar();
        else if (c instanceof GestionFacturacionPanel) ((GestionFacturacionPanel) c).cargarMovimientosFacturacion();
        else if (c instanceof GestionReportePanel) ((GestionReportePanel) c).cargarStockConsolidado();
        else if (c instanceof GestionDashboardPanel) ((GestionDashboardPanel) c).cargarDatosDashboard();
    }

    private void mostrarAcercaDe() {
        String mensaje = "<html><body style='width: 300px'>"
            + "<h2 style='margin-bottom: 2px'>AFD System</h2>"
            + "<span style='color: gray'>Versión 1.0.0</span>"
            + "<p style='margin-top: 10px'>Sistema de gestión comercial para negocios con varias sucursales: "
            + "pedidos, ventas, stock, órdenes de compra, proveedores, clientes y reportes.</p>"
            + "<p style='margin-top: 10px'><b>Desarrollado por</b><br>Lucas Acosta y Franco Verdu</p>"
            + "<p style='margin-top: 10px; color: gray'>Trabajo final · " + java.time.Year.now() + "</p>"
            + "</body></html>";
        java.net.URL url = MainFrame.class.getResource("/icono.png");
        Icon icono = (url != null)
            ? new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH))
            : null;
        JOptionPane.showMessageDialog(this, mensaje, "Acerca de AFD", JOptionPane.INFORMATION_MESSAGE, icono);
    }

    private void construirModulosSegunRol(String rol) {
    try {
        tabbedPane.removeAll();
        System.out.println("Iniciando carga de módulos para el rol: " + rol + " en Sucursal ID: " + idSucursalSesion);

        // 1. Pedidos (Siempre primero)
        tabbedPane.addTab("Pedidos", new GestionPedidosPanel(idSucursalSesion));

        // 2. Operaciones
        tabbedPane.addTab("Operaciones", new GestionOperativaPanel());

        // 3. Inventario
        if (rol.equalsIgnoreCase("ADMIN") || rol.equalsIgnoreCase("GERENTE") || rol.equalsIgnoreCase("VENDEDOR")) {
            tabbedPane.addTab("Inventario", new GestionInventarioPanel());
        }

        // Modulos de Gestion para ADMIN y GERENTE
        if (rol.equalsIgnoreCase("ADMIN") || rol.equalsIgnoreCase("GERENTE")) {
            // 4. Emitir OC
            tabbedPane.addTab("Emitir OC", new GestionOrdenDeCompraPanel());
            // 5. Operaciones OC
            tabbedPane.addTab("Operaciones OC", new GestionOperativaOCPanel());
            // 6. Proveedores
            tabbedPane.addTab("Proveedores", new GestionProveedoresPanel());
        }

        // 7. Clientes (Para todos)
        tabbedPane.addTab("Clientes", new GestionClientesPanel());

        // 8. Productos
        if (rol.equalsIgnoreCase("ADMIN") || rol.equalsIgnoreCase("GERENTE")) {
            tabbedPane.addTab("Productos", new GestionProductosPanel());
        }

        // Exclusivos de ADMIN
        if (rol.equalsIgnoreCase("ADMIN")) {
            // 9. Usuarios
            tabbedPane.addTab("Usuarios", new GestionUsuarioPanel());
            // 10. Sucursales
            tabbedPane.addTab("Sucursales", new GestionSucursalesPanel());
        }

        // 11. Ventas
        if (rol.equalsIgnoreCase("ADMIN") || rol.equalsIgnoreCase("GERENTE")) {
            tabbedPane.addTab("Ventas", new GestionFacturacionPanel());
            // 12. Reportes
            tabbedPane.addTab("Reportes", new GestionReportePanel());
            // 13. Dashboard
            tabbedPane.addTab("Dashboard", new GestionDashboardPanel());
            // 14. Auditoría (Antes llamado "Movimientos Stock")
            tabbedPane.addTab("Auditoría", new HistorialMovimientosPanel());
        }

        tabbedPane.revalidate();
        tabbedPane.repaint();
        System.out.println("Módulos cargados exitosamente.");

    } catch (Exception e) {
        System.err.println("CRÍTICO: Error durante la instanciación de los paneles.");
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Error fatal al construir la interfaz.\nDetalle: " + e.getMessage(),
                "Error de sistema",
                JOptionPane.ERROR_MESSAGE);
    }
}

    public void notificarCambioSucursales() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component c = tabbedPane.getComponentAt(i);

            // Refresca el Dashboard si está activo
            if (c instanceof GestionDashboardPanel) {
                ((GestionDashboardPanel) c).cargarSucursales();
                ((GestionDashboardPanel) c).cargarDatosDashboard();
            }

            // Refresca el combo en Inventario
            if (c instanceof GestionInventarioPanel) { 
                ((GestionInventarioPanel) c).cargarComboSucursales(); 
            }

            // Refresca el combo en Historial de Movimientos
            if (c instanceof HistorialMovimientosPanel) { 
                ((HistorialMovimientosPanel) c).cargarComboSucursales(); 
                ((HistorialMovimientosPanel) c).cargarHistorialMovimientos(); 
            }

            // Refresca el combo en Facturación
            if (c instanceof GestionFacturacionPanel) { 
                ((GestionFacturacionPanel) c).cargarSucursalesEnCombo(); 
            }

            // Refresca la grilla consolidada en Reportes
            if (c instanceof GestionReportePanel) { 
                ((GestionReportePanel) c).cargarStockConsolidado(); 
            }
        }
    }

    private JPanel crearStatusBar(Usuario user) {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setPreferredSize(new Dimension(this.getWidth(), 35));
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String nombreSucursal = obtenerNombreSucursal(idSucursalSesion);
        JLabel lblUser = new JLabel("  👤 Usuario: " + user.getNombre() + " (" + user.getRol() + ")  |  📍 ID Sucursal: " + nombreSucursal + "  |  📅 " + fecha);
        lblUser.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusPanel.add(lblUser, BorderLayout.WEST);
        return statusPanel;
    }

    private void cerrarSesion() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Desea cerrar la sesión actual?", "Cerrar sesión", 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            SesionUsuario.setUsuario(null);
            this.dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private String obtenerNombreSucursal(int idSucursal) {
        String nombre = "Desconocida";
        String sql = "SELECT nombre FROM sucursales WHERE id_sucursal = ?";
        try (java.sql.Connection conn = persistencia.ConexionDB.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idSucursal);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nombre = rs.getString("nombre");
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al obtener nombre de la sucursal: " + e.getMessage());
        }
        return nombre;
    }
}