package vista;

import modelo.Usuario;
import modelo.SesionUsuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.formdev.flatlaf.FlatDarkLaf;

public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;

    public MainFrame() {
        // 1. Recuperamos el usuario de la sesión global
        Usuario usuarioActual = SesionUsuario.getUsuario();
        
        if (usuarioActual == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }

        // 2. Configuración básica de la ventana
        setTitle("SISTEMA AFD - Gestión Logística y Comercial");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1366, 768);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());

        // 3. AGREGAR BARRA DE MENÚ SUPERIOR
        setJMenuBar(crearMenuBar());

        // 4. Panel de pestañas
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
        add(tabbedPane, BorderLayout.CENTER);
        
        // 5. Construimos los módulos según el rol del usuario logueado
        construirModulosSegunRol(usuarioActual.getRol());
        
        // 6. Barra inferior (Status Bar)
        add(crearStatusBar(usuarioActual), BorderLayout.SOUTH);
    }

    private JMenuBar crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menú Archivo
        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.setMnemonic(KeyEvent.VK_A);

        // Ítem Cerrar Sesión
        JMenuItem itemLogout = new JMenuItem("Cerrar Sesión");
        itemLogout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        itemLogout.addActionListener(e -> cerrarSesion());
        
        // Ítem Salir
        JMenuItem itemSalir = new JMenuItem("Salir del Sistema");
        itemSalir.addActionListener(e -> System.exit(0));

        menuArchivo.add(itemLogout);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        menuBar.add(menuArchivo);
        
        // Menú Ayuda
        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem itemAcercaDe = new JMenuItem("Acerca de AFD");
        itemAcercaDe.addActionListener(e -> JOptionPane.showMessageDialog(this, "AFD System v1.0\nDesarrollado por Lucas"));
        menuAyuda.add(itemAcercaDe);
        menuBar.add(menuAyuda);

        return menuBar;
    }

    private void construirModulosSegunRol(String rol) {
        try {
            // Limpiamos pestañas por seguridad si se llama este método más de una vez
            tabbedPane.removeAll();
            
            System.out.println("Iniciando carga de módulos para el rol: " + rol);
            
            // --- MÓDULOS COMUNES (Vendedor, Gerente, Admin) ---
            tabbedPane.addTab("🍕 Ventas", new GestionPedidosPanel()); 
            tabbedPane.addTab("👥 Clientes", new GestionClientesPanel()); // Ahora visible para todos
            tabbedPane.addTab("🚚 Operaciones", new GestionOperativaPanel()); 

            // --- LÓGICA DE PERMISOS ---
            
            // 1. ADMIN y GERENTE: Tienen acceso a Productos, Proveedores, Órdenes de Compra, Inventario y Reportes
            if (rol.equalsIgnoreCase("ADMIN") || rol.equalsIgnoreCase("GERENTE")) {
                // 1. ABM Puro (Nombre, Precio, Descripción)
                tabbedPane.addTab("📦 Productos", new GestionProductosPanel()); 
                
                // 🏢 ABM de Proveedores
                tabbedPane.addTab("🏢 Proveedores", new GestionProveedoresPanel());
                
                // 📑 AGREGAMOS LA GESTIÓN DE COMPRAS AQUÍ:
                tabbedPane.addTab("📑 Órdenes de Compra", new GestionOrdenDeCompraPanel());
                
                // 2. Control de Stock (Ver cantidades, alertas de stock bajo)
                tabbedPane.addTab("📊 Inventario", new GestionInventarioPanel()); 
                
                tabbedPane.addTab("📈 Reportes", new GestionReportePanel());
            }

            // 2. SOLO EL ADMIN: Gestión de Usuarios del sistema
            if (rol.equalsIgnoreCase("ADMIN")) {
                tabbedPane.addTab("⚙️ Usuarios", new GestionUsuarioPanel());
            }

            // Refrescar la interfaz para asegurar que las pestañas se dibujen
            tabbedPane.revalidate();
            tabbedPane.repaint();
            
            System.out.println("Módulos cargados exitosamente.");

        } catch (Exception e) {
            System.err.println("CRÍTICO: Error durante la instanciación de los paneles.");
            e.printStackTrace(); 
            JOptionPane.showMessageDialog(this, 
                "Error fatal al construir la interfaz.\nDetalle: " + e.getMessage(), 
                "Error de Sistema", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel crearStatusBar(Usuario user) {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setPreferredSize(new Dimension(this.getWidth(), 35));
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        JLabel lblUser = new JLabel("  👤 Usuario: " + user.getNombre() + " (" + user.getRol() + ")  |  📅 " + fecha);
        lblUser.setFont(new Font("SansSerif", Font.PLAIN, 12));

        statusPanel.add(lblUser, BorderLayout.WEST);
        return statusPanel;
    }

    private void cerrarSesion() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Desea cerrar la sesión actual?", "Cerrar Sesión", 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SesionUsuario.setUsuario(null);
            this.dispose();
            new LoginFrame().setVisible(true);
        }
    }

    public static void main(String[] args) {
        try {
            // Aplicamos el tema oscuro de FlatLaf antes de lanzar la App
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Error al aplicar FlatLaf");
        }
        
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}