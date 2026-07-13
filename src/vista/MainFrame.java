package vista;
import modelo.Usuario;
import modelo.SesionUsuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainFrame extends JFrame {

    // Variables y Constructor

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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1366, 768);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());
        setJMenuBar(crearMenuBar());
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
        add(tabbedPane, BorderLayout.CENTER);
        construirModulosSegunRol(usuarioActual.getRol());
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
        itemAcercaDe.addActionListener(e -> JOptionPane.showMessageDialog(this, "AFD System v1.0\nDesarrollado por Lucas"));
        menuAyuda.add(itemAcercaDe);
        menuBar.add(menuAyuda);
        return menuBar;
    }

    private void construirModulosSegunRol(String rol) {
        try {
            tabbedPane.removeAll();
            System.out.println("Iniciando carga de módulos para el rol: " + rol + " en Sucursal ID: " + idSucursalSesion);
            tabbedPane.addTab("Ventas", new GestionPedidosPanel(idSucursalSesion)); 
            tabbedPane.addTab("Operaciones", new GestionOperativaPanel()); 
            if (rol.equalsIgnoreCase("ADMIN") || rol.equalsIgnoreCase("GERENTE") || rol.equalsIgnoreCase("VENDEDOR")) {
                tabbedPane.addTab("Inventario", new GestionInventarioPanel()); 
            }
            if (rol.equalsIgnoreCase("ADMIN") || rol.equalsIgnoreCase("GERENTE")) {
                tabbedPane.addTab("Emitir OC", new GestionOrdenDeCompraPanel());
                tabbedPane.addTab("Operaciones OC", new GestionOperativaOCPanel());
                tabbedPane.addTab("Proveedores", new GestionProveedoresPanel());
                tabbedPane.addTab("Facturación", new GestionFacturacionPanel());
            }
            tabbedPane.addTab("Clientes", new GestionClientesPanel()); 
            if (rol.equalsIgnoreCase("ADMIN") || rol.equalsIgnoreCase("GERENTE")) {
                tabbedPane.addTab("Productos", new GestionProductosPanel()); 
            }
            if (rol.equalsIgnoreCase("ADMIN")) {
                tabbedPane.addTab("Usuarios", new GestionUsuarioPanel());
            }
            if (rol.equalsIgnoreCase("ADMIN") || rol.equalsIgnoreCase("GERENTE")) {
                tabbedPane.addTab("Reportes", new GestionReportePanel());
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

    private JPanel crearStatusBar(Usuario user) {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setPreferredSize(new Dimension(this.getWidth(), 35));
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        JLabel lblUser = new JLabel("  👤 Usuario: " + user.getNombre() + " (" + user.getRol() + ")  |  📍 ID Sucursal: " + idSucursalSesion + "  |  📅 " + fecha);
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
}