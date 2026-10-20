package sistemaconfecciones;

import javax.swing.*;
import java.awt.*;

public class FrmMenu extends JFrame {

    private JButton btnClientes, btnProductos, btnMateriasPrimas, btnSalir;

    public FrmMenu() {
        initComponents();
        this.setLocationRelativeTo(null); // Centrar pantalla
    }

    private void initComponents() {
        setTitle("Sistema de Gestión - Empresa de Confecciones");
        setSize(500, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Título superior
        JLabel lblTitulo = new JLabel("SISTEMA DE GESTIÓN DE CONFECCIONES", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        add(lblTitulo, BorderLayout.NORTH);

        // Panel de botones central
        JPanel panelBotones = new JPanel(new GridLayout(4, 1, 12, 12));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 50, 20, 50));

        btnClientes = new JButton("Gestión de clientes");
        btnProductos = new JButton("Gestión de productos");
        btnMateriasPrimas = new JButton("Gestión de materias primas");
        btnSalir = new JButton("Salir del Sistema");

        btnClientes.setFont(new Font("Arial", Font.PLAIN, 14));
        btnProductos.setFont(new Font("Arial", Font.PLAIN, 14));
        btnMateriasPrimas.setFont(new Font("Arial", Font.PLAIN, 14));
        btnSalir.setFont(new Font("Arial", Font.PLAIN, 14));

        panelBotones.add(btnClientes);
        panelBotones.add(btnProductos);
        panelBotones.add(btnMateriasPrimas);
        panelBotones.add(btnSalir);

        add(panelBotones, BorderLayout.CENTER);

        // --- EVENTOS AL DAR CLIC ---
        btnClientes.addActionListener(e -> new FrmCliente().setVisible(true));
        btnProductos.addActionListener(e -> new FrmProducto().setVisible(true));
        btnMateriasPrimas.addActionListener(e -> new FrmMateriaPrima().setVisible(true));
        btnSalir.addActionListener(e -> System.exit(0));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmMenu().setVisible(true));
    }
}