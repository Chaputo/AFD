package vista;

import modelo.Usuario;
import modelo.SesionUsuario; // Importante para guardar la sesión
import persistencia.UsuarioDAO;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class LoginFrame extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtClave;
    private JButton btnLogin;
    private static final int WIDTH = 450;
    private static final int HEIGHT = 350; // Aumenté un poco el alto para que respire el diseño

    public LoginFrame() {
        setTitle("AFD - Acceso al Sistema");
        setSize(WIDTH, HEIGHT); 
        setLayout(new BorderLayout(10, 10)); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        
        txtUsuario = new JTextField(25);
        txtClave = new JPasswordField(25);
        btnLogin = new JButton("Ingresar");
        
        // Estilo del botón
        btnLogin.setBackground(new Color(60, 179, 113)); 
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        
        // Título
        JLabel lblTituloPrincipal = new JLabel("🔐 AFD  | ACCESO", SwingConstants.CENTER);
        lblTituloPrincipal.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTituloPrincipal.setBorder(new EmptyBorder(20, 0, 10, 0));
        add(lblTituloPrincipal, BorderLayout.NORTH);

        // Panel de Credenciales
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

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panelCredenciales.add(new JLabel("👤 Usuario:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        panelCredenciales.add(txtUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelCredenciales.add(new JLabel("🔑 Clave:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; 
        panelCredenciales.add(txtClave, gbc);

        gbc.gridx = 0; gbc.gridy = 2; 
        gbc.gridwidth = 2; 
        btnLogin.setPreferredSize(new Dimension(300, 40)); 
        panelCredenciales.add(btnLogin, gbc);

        add(panelCredenciales, BorderLayout.CENTER);

        // Listeners
        btnLogin.addActionListener(e -> intentarLogin());
        
        // Hacer que el botón "Ingresar" sea el predeterminado al presionar Enter
        this.getRootPane().setDefaultButton(btnLogin);
    }
 
    private void intentarLogin() {
        String usuarioStr = txtUsuario.getText().trim();
        String claveStr = new String(txtClave.getPassword()); 

        if (usuarioStr.isEmpty() || claveStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor complete todos los campos.");
            return;
        }

        try {
            UsuarioDAO dao = new UsuarioDAO();
            Usuario user = dao.autenticar(usuarioStr, claveStr);

            if (user != null) {
                // --- INTEGRACIÓN CON SESIÓN ---
                SesionUsuario.setUsuario(user); // Guardamos al usuario y su ROL globalmente
                
                // Abrimos el frame principal (Asegúrate de que tu clase se llame MainFrame o VistaPrincipal)
                new MainFrame().setVisible(true); 
                this.dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Usuario o clave incorrectos. Intente de nuevo.", 
                    "⚠️ Acceso Denegado",
                    JOptionPane.ERROR_MESSAGE);
                txtClave.setText("");
                txtClave.requestFocus();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al conectar con la base de datos.", 
                "❌ Error SQL",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // Configuraciones de FlatLaf
            UIManager.put("Button.arc", 18); 
            UIManager.put("Component.arc", 12); 
            UIManager.put("TextComponent.arc", 12);
            FlatDarkLaf.setup(); 
        } catch (Exception ex) {
            System.err.println("Error al inicializar FlatLaf: " + ex);
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}