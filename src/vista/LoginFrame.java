package vista;
import modelo.Usuario;
import modelo.SesionUsuario;
import persistencia.UsuarioDAO;
import persistencia.ConexionDB;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class LoginFrame extends JFrame {

    // Variables y Constructor

    private JTextField txtUsuario;
    private JPasswordField txtClave;
    private JComboBox<SucursalItem> cmbSucursales;
    private JButton btnLogin;
    private static final int WIDTH = 450;
    private static final int HEIGHT = 400;
    private static class SucursalItem {
        int id;
        String nombre;
        public SucursalItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }

    // Ícono de la aplicación (barra de título y barra de tareas)
    static void aplicarIcono(JFrame frame) {
        java.net.URL url = LoginFrame.class.getResource("/icono.png");
        if (url != null) {
            frame.setIconImage(new ImageIcon(url).getImage());
        }
    }

    public LoginFrame() {
        setTitle("AFD - Acceso al sistema");
        aplicarIcono(this);
        setSize(WIDTH, HEIGHT); 
        setLayout(new BorderLayout(10, 10)); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        txtUsuario = new JTextField(25);
        txtClave = new JPasswordField(25);
        cmbSucursales = new JComboBox<>();
        btnLogin = new JButton("Ingresar");
        btnLogin.setBackground(new Color(60, 179, 113)); 
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        JLabel lblTituloPrincipal = new JLabel("🔐 AFD  | ACCESO", SwingConstants.CENTER);
        lblTituloPrincipal.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTituloPrincipal.setBorder(new EmptyBorder(20, 0, 10, 0));
        add(lblTituloPrincipal, BorderLayout.NORTH);
        JPanel panelCredenciales = new JPanel(new GridBagLayout());
        panelCredenciales.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Ingreso", 
            TitledBorder.CENTER, 
            TitledBorder.TOP, 
            new Font("SansSerif", Font.BOLD, 14), 
            Color.WHITE));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0;
        panelCredenciales.add(new JLabel("👤 Usuario:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        panelCredenciales.add(txtUsuario, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelCredenciales.add(new JLabel("🔑 Clave:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; 
        panelCredenciales.add(txtClave, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panelCredenciales.add(new JLabel("📍 Sucursal:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        panelCredenciales.add(cmbSucursales, gbc);
        gbc.gridx = 0; gbc.gridy = 3; 
        gbc.gridwidth = 2; 
        gbc.weightx = 0;
        btnLogin.setPreferredSize(new Dimension(300, 40)); 
        panelCredenciales.add(btnLogin, gbc);
        add(panelCredenciales, BorderLayout.CENTER);
        cargarSucursalesDB();
        btnLogin.addActionListener(e -> intentarLogin());
        this.getRootPane().setDefaultButton(btnLogin);
    }
 
    private void cargarSucursalesDB() {
        try (Connection conn = ConexionDB.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_sucursal, nombre FROM sucursales WHERE activa = 1 ORDER BY nombre")) {
            while (rs.next()) {
                cmbSucursales.addItem(new SucursalItem(rs.getInt("id_sucursal"), rs.getString("nombre")));
            }
        } catch (SQLException ex) {
            System.err.println("Error al poblar combo de sucursales en Login: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, ex.getMessage(), "❌ Sin conexión a la base de datos", JOptionPane.ERROR_MESSAGE);
        }
        if (cmbSucursales.getItemCount() == 0) {
            cmbSucursales.addItem(new SucursalItem(1, "Bella Vista (Casa Matriz)"));
        }
    }

    private void intentarLogin() {
        String usuarioStr = txtUsuario.getText().trim();
        String claveStr = new String(txtClave.getPassword()); 
        if (usuarioStr.isEmpty() || claveStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor complete todos los campos.");
            return;
        }
        SucursalItem sucursalSeleccionada = (SucursalItem) cmbSucursales.getSelectedItem();
        if (sucursalSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione una sucursal de trabajo.");
            return;
        }
        try {
            UsuarioDAO dao = new UsuarioDAO();
            Usuario user = dao.autenticar(usuarioStr, claveStr);
            if (user != null) {
                SesionUsuario.setUsuario(user);
                new MainFrame(sucursalSeleccionada.id).setVisible(true); 
                this.dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Usuario o clave incorrectos. Intente de nuevo.", 
                    "⚠️ Acceso denegado",
                    JOptionPane.ERROR_MESSAGE);
                txtClave.setText("");
                txtClave.requestFocus();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al conectar con la base de datos:\n" + ex.getMessage(),
                "❌ Error SQL",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.put("Button.arc", 18); 
            UIManager.put("Component.arc", 12); 
            UIManager.put("TextComponent.arc", 12);
            FlatDarkLaf.setup(); 
        } catch (Exception ex) {
            System.err.println("Error al inicializar FlatLaf: " + ex);
        }
        // Red de seguridad: cualquier error no controlado se muestra al usuario en vez de perderse en la consola
        Thread.setDefaultUncaughtExceptionHandler((hilo, ex) -> {
            ex.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                "Ocurrió un error inesperado:\n" + ex, "Error", JOptionPane.ERROR_MESSAGE));
        });
        // Si está configurado un servidor local (db.local.properties), se levanta antes de abrir el login
        ConexionDB.iniciarServidorSiHaceFalta();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}