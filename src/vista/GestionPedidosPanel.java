package vista;

import modelo.Cliente;
import modelo.Pedido;
import modelo.DetallePedido;
import modelo.Producto;
import modelo.Usuario;
import modelo.SesionUsuario;
import persistencia.ClienteDAO;
import persistencia.PedidoDAO;
import persistencia.ProductoDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GestionPedidosPanel extends JPanel {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    
    private JTextField txtBuscarCliente, txtNombreCliente, txtIdCliente;
    private JTextField txtNombreProducto, txtCantidadProducto;
    private JButton btnAgregar, btnQuitar, btnMas, btnMenos;
    private JTable tablaCarrito;
    private JTable tablaBusqueda;
    private TableRowSorter<DefaultTableModel> sorter;
    private DefaultTableModel modelCarrito;
    private JLabel lblTotal;
    private JButton btnFinalizarVenta;
    
    // NUEVO: Selector de método de pago
    private JComboBox<String> comboMetodoPago;

    private Cliente clienteActual = null;
    private Producto productoSeleccionado = null;
    private double totalVenta = 0.0;
    
    private int idUsuarioActual;
    private int idSucursalActual;
    private final int ID_CLIENTE_GENERICO = 1;

    public GestionPedidosPanel() {
        Usuario actual = SesionUsuario.getUsuario();
        this.idUsuarioActual = (actual != null) ? actual.getId() : 1;
        this.idSucursalActual = 1; 

        setLayout(new BorderLayout(10, 10));
        
        btnAgregar = new JButton("Agregar");
        btnQuitar = new JButton("Eliminar seleccionado");
        btnMas = new JButton("+");
        btnMenos = new JButton("-");
        txtCantidadProducto = new JTextField("1", 3);
        txtCantidadProducto.setHorizontalAlignment(JTextField.CENTER);

        add(crearPanelCliente(), BorderLayout.NORTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, crearPanelProductos(), crearPanelCarrito());
        splitPane.setResizeWeight(0.4); 
        add(splitPane, BorderLayout.CENTER);
        
        add(crearPanelFooter(), BorderLayout.SOUTH);
        
        agregarListeners();
        limpiarCampos();
    }

    private JPanel crearPanelCliente() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Información del cliente"));

        txtBuscarCliente = new JTextField(12);
        txtNombreCliente = new JTextField(18);
        txtNombreCliente.setEditable(false);
        txtIdCliente = new JTextField(4);
        txtIdCliente.setEditable(false);

        panel.add(new JLabel("Buscar (Tel/Nom):"));
        panel.add(txtBuscarCliente);
        panel.add(new JLabel("ID:"));
        panel.add(txtIdCliente);
        panel.add(new JLabel("Cliente:"));
        panel.add(txtNombreCliente);

        return panel;
    }

    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Inventario de productos"));

        txtNombreProducto = new JTextField(20);
        txtNombreProducto.setToolTipText("Escribe para filtrar productos...");

        DefaultTableModel modeloBase = new DefaultTableModel(new Object[]{"SKU", "Nombre", "Precio"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaBusqueda = new JTable(modeloBase);
        sorter = new TableRowSorter<>(modeloBase);
        tablaBusqueda.setRowSorter(sorter);
        
        cargarTodosLosProductos(modeloBase);

        txtNombreProducto.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            private void filtrar() {
                String texto = txtNombreProducto.getText();
                sorter.setRowFilter(texto.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + texto, 1));
            }
        });

        tablaBusqueda.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int filaView = tablaBusqueda.getSelectedRow();
                if (filaView != -1) {
                    int filaModel = tablaBusqueda.convertRowIndexToModel(filaView);
                    String sku = tablaBusqueda.getModel().getValueAt(filaModel, 0).toString();
                    try {
                        productoSeleccionado = productoDAO.buscarPorSku(sku);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        panel.add(txtNombreProducto, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaBusqueda), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelInferior.add(new JLabel("Cant:"));
        panelInferior.add(btnMenos);
        panelInferior.add(txtCantidadProducto);
        panelInferior.add(btnMas);
        panelInferior.add(btnAgregar);
        
        panel.add(panelInferior, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelCarrito() {
        modelCarrito = new DefaultTableModel(new Object[]{"ID", "SKU", "Producto", "Cant.", "Precio U.", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaCarrito = new JTable(modelCarrito);
        tablaCarrito.getColumnModel().getColumn(0).setMinWidth(0);
        tablaCarrito.getColumnModel().getColumn(0).setMaxWidth(0);

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSur.add(btnQuitar);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Detalle de la venta"));
        panel.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);
        panel.add(panelSur, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        lblTotal = new JLabel("TOTAL: $0.00", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 22));
        
        // NUEVO: Implementación del selector de pago
        String[] metodos = {"Efectivo", "Débito", "Crédito", "Mercado Pago", "Pedidos Ya"};
        comboMetodoPago = new JComboBox<>(metodos);
        comboMetodoPago.setFont(new Font("SansSerif", Font.PLAIN, 14));
        comboMetodoPago.setPreferredSize(new Dimension(150, 35));

        btnFinalizarVenta = new JButton("Finalizar venta");
        btnFinalizarVenta.setPreferredSize(new Dimension(250, 45));
        btnFinalizarVenta.setBackground(new Color(40, 120, 200));
        btnFinalizarVenta.setForeground(Color.WHITE);
        btnFinalizarVenta.setFont(new Font("SansSerif", Font.BOLD, 16));

        // Panel para agrupar Método de Pago y Total
        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        panelDerecho.add(new JLabel("Método de pago:"));
        panelDerecho.add(comboMetodoPago);
        panelDerecho.add(lblTotal);

        panel.add(panelDerecho, BorderLayout.CENTER);
        panel.add(btnFinalizarVenta, BorderLayout.SOUTH);

        return panel;
    }

    private void agregarListeners() {
        btnAgregar.addActionListener(e -> agregarAlCarrito());
        btnQuitar.addActionListener(e -> quitarDelCarrito());
        btnFinalizarVenta.addActionListener(e -> intentarFinalizarVenta());
        
        btnMas.addActionListener(e -> incrementarCantidad(1));
        btnMenos.addActionListener(e -> incrementarCantidad(-1));

        txtBuscarCliente.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { buscarCliente(); }
        });
    }

    private void cargarTodosLosProductos(DefaultTableModel modelo) {
        try {
            List<Producto> lista = productoDAO.listarTodos(); 
            for (Producto p : lista) {
                modelo.addRow(new Object[]{p.getSku(), p.getNombre(), p.getPrecio()});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void seleccionarProductoDesdeTabla(DefaultTableModel modelo, int fila) {
        String sku = modelo.getValueAt(fila, 0).toString();
        try {
            productoSeleccionado = productoDAO.buscarPorSku(sku);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void agregarAlCarrito() {
        int filaSeleccionada = tablaBusqueda.getSelectedRow();
        if (filaSeleccionada != -1) {
            int filaModelo = tablaBusqueda.convertRowIndexToModel(filaSeleccionada);
            seleccionarProductoDesdeTabla((DefaultTableModel) tablaBusqueda.getModel(), filaModelo);
        }

        if (productoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccioná un producto.");
            return;
        }

        try {
            int cant = Integer.parseInt(txtCantidadProducto.getText().trim());
            if (cant <= 0) throw new NumberFormatException();

            boolean existe = false;
            for (int i = 0; i < modelCarrito.getRowCount(); i++) {
                String skuEnCarrito = modelCarrito.getValueAt(i, 1).toString();
                if (skuEnCarrito.equals(productoSeleccionado.getSku())) {
                    int cantActual = Integer.parseInt(modelCarrito.getValueAt(i, 3).toString());
                    int nuevaCant = cantActual + cant;
                    double nuevoSubtotal = nuevaCant * productoSeleccionado.getPrecio();
                    modelCarrito.setValueAt(nuevaCant, i, 3);
                    modelCarrito.setValueAt(nuevoSubtotal, i, 5);
                    existe = true;
                    break;
                }
            }

            if (!existe) {
                double subtotal = productoSeleccionado.getPrecio() * cant;
                modelCarrito.addRow(new Object[]{
                    productoSeleccionado.getId(),
                    productoSeleccionado.getSku(),
                    productoSeleccionado.getNombre(),
                    cant,
                    productoSeleccionado.getPrecio(),
                    subtotal
                });
            }
            actualizarTotal();
            productoSeleccionado = null; 
            tablaBusqueda.clearSelection();
            txtCantidadProducto.setText("1");
            txtNombreProducto.setText("");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida.");
        }
    }

    private void quitarDelCarrito() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila >= 0) {
            modelCarrito.removeRow(fila);
            actualizarTotal();
        }
    }

    private void actualizarTotal() {
        totalVenta = 0.0;
        for (int i = 0; i < modelCarrito.getRowCount(); i++) {
            totalVenta += (double) modelCarrito.getValueAt(i, 5); 
        }
        lblTotal.setText(String.format("TOTAL: $%.2f", totalVenta));
    }

    private void buscarCliente() {
    String termino = txtBuscarCliente.getText().trim();
    
    // SI EL BUSCADOR ESTÁ VACÍO: Volvemos al estado por defecto "Sin cliente"
    if (termino.isEmpty()) {
        clienteActual = null;
        txtIdCliente.setText(String.valueOf(ID_CLIENTE_GENERICO));
        txtNombreCliente.setText("Sin cliente");
        return;
    }
    
    try {
        List<Cliente> encontrado = clienteDAO.buscar(termino);
        if (!encontrado.isEmpty()) {
            clienteActual = encontrado.get(0);
            txtIdCliente.setText(String.valueOf(clienteActual.getId()));
            txtNombreCliente.setText(clienteActual.getNombre());
        } else {
            // SI ESCRIBIÓ ALGO QUE NO EXISTE: También podés dejar "Sin cliente" o lo que prefieras
            clienteActual = null;
            txtIdCliente.setText(String.valueOf(ID_CLIENTE_GENERICO));
            txtNombreCliente.setText("Sin cliente"); 
        }
    } catch (SQLException e) { 
        e.printStackTrace(); 
    }
}

    private void intentarFinalizarVenta() {
        if (modelCarrito.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Confirmar Venta por $" + String.format("%.2f", totalVenta) + "?", "Finalizar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ejecutarTransaccionVenta();
        }
    }

    private void ejecutarTransaccionVenta() {
        try {
            int clienteId = (clienteActual != null) ? clienteActual.getId() : ID_CLIENTE_GENERICO;
            
            // NUEVO: Lógica de estado según el pago
            String metodoSeleccionado = comboMetodoPago.getSelectedItem().toString();
            String estadoInicial = metodoSeleccionado.equalsIgnoreCase("Pedidos Ya") ? "Pendiente" : "Finalizado";

            // Creamos el pedido con el nuevo atributo de pago y estado
            Pedido pedido = new Pedido();
            pedido.setIdCliente(clienteId);
            pedido.setIdUsuario(idUsuarioActual);
            pedido.setIdSucursal(idSucursalActual);
            pedido.setFechaPedido(LocalDateTime.now());
            pedido.setTotalPedido(totalVenta);
            pedido.setEstadoEntrega(estadoInicial);
            pedido.setTipoPago(metodoSeleccionado);

            List<DetallePedido> detalles = new ArrayList<>();
            for (int i = 0; i < tablaCarrito.getRowCount(); i++) {
                String skuProd = tablaCarrito.getValueAt(i, 1).toString().trim(); 
                int cant = Integer.parseInt(tablaCarrito.getValueAt(i, 3).toString());
                double precio = Double.parseDouble(tablaCarrito.getValueAt(i, 4).toString());
                detalles.add(new DetallePedido(0, 0, skuProd, precio, cant));
            }

            if (pedidoDAO.registrarVenta(pedido, detalles, idSucursalActual)) {
                JOptionPane.showMessageDialog(this, "Venta procesada como " + estadoInicial);
                limpiarCampos();
            }
            
        } catch (Exception e) {
            e.printStackTrace(); 
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void incrementarCantidad(int delta) {
        try {
            int cant = Integer.parseInt(txtCantidadProducto.getText());
            if (cant + delta >= 1) txtCantidadProducto.setText(String.valueOf(cant + delta));
        } catch (NumberFormatException e) { txtCantidadProducto.setText("1"); }
    }

    private void limpiarCampos() {
    modelCarrito.setRowCount(0);
    clienteActual = null;
    productoSeleccionado = null;
    txtBuscarCliente.setText("");
    
    // Ajustamos para que la pantalla inicial use tu ID 1 corporativo
    txtIdCliente.setText(String.valueOf(ID_CLIENTE_GENERICO));
    txtNombreCliente.setText("Sin cliente");
    
    txtNombreProducto.setText("");
    txtCantidadProducto.setText("1");
    if (comboMetodoPago != null) comboMetodoPago.setSelectedIndex(0); // Opcional: resetea el método de pago a Efectivo
    actualizarTotal();
}
}