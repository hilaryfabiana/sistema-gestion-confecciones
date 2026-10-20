package sistemaconfecciones;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class FrmCliente extends JFrame {

    // Componentes de la interfaz
    private JTextField txtCodigo, txtRazonSocial, txtRucDni, txtTelefono, txtCorreo, txtDireccion;
    private JComboBox<String> cbTipoCliente;
    private JButton btnGuardar, btnModificar, btnEliminar, btnNuevo;
    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private int idClienteSeleccionado = -1; // Almacena el ID del cliente seleccionado para editar/eliminar

    public FrmCliente() {
        initComponents();
        this.setLocationRelativeTo(null); // Centrar en pantalla
        listarClientes(); // Cargar los datos de MySQL en la tabla al abrir
    }

    private void initComponents() {
        setTitle("Gestión de Clientes - Empresa de Confecciones (Iteración 1)");
        setSize(950, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- PANEL DE FORMULARIO (IZQUIERDA) ---
        JPanel panelFormulario = new JPanel(new GridLayout(10, 2, 8, 8));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));

        panelFormulario.add(new JLabel("Código:"));
        txtCodigo = new JTextField();
        panelFormulario.add(txtCodigo);

        panelFormulario.add(new JLabel("Razón Social / Nombre:"));
        txtRazonSocial = new JTextField();
        panelFormulario.add(txtRazonSocial);

        panelFormulario.add(new JLabel("RUC / DNI:"));
        txtRucDni = new JTextField();
        panelFormulario.add(txtRucDni);

        panelFormulario.add(new JLabel("Teléfono:"));
        txtTelefono = new JTextField();
        panelFormulario.add(txtTelefono);

        panelFormulario.add(new JLabel("Correo:"));
        txtCorreo = new JTextField();
        panelFormulario.add(txtCorreo);

        panelFormulario.add(new JLabel("Dirección:"));
        txtDireccion = new JTextField();
        panelFormulario.add(txtDireccion);

        panelFormulario.add(new JLabel("Tipo Cliente:"));
        cbTipoCliente = new JComboBox<>(new String[]{"Principal", "Temporada", "Casual"});
        panelFormulario.add(cbTipoCliente);

        // Botones del CRUD
        btnGuardar = new JButton("Guardar");
        btnModificar = new JButton("Modificar");
        btnEliminar = new JButton("Eliminar");
        btnNuevo = new JButton("Nuevo / Limpiar");

        panelFormulario.add(btnGuardar);
        panelFormulario.add(btnModificar);
        panelFormulario.add(btnEliminar);
        panelFormulario.add(btnNuevo);

        add(panelFormulario, BorderLayout.WEST);

        // --- TABLA DE DATOS (CENTRO) ---
        modeloTabla = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "Código", "Nombre/Razón Social", "RUC/DNI", "Teléfono", "Correo", "Dirección", "Tipo"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Desactivar edición directa sobre la tabla
            }
        };

        tablaClientes = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaClientes);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Listado de Clientes"));

        add(scrollTabla, BorderLayout.CENTER);

        // --- EVENTOS DE BOTONES ---
        btnGuardar.addActionListener((ActionEvent e) -> guardarCliente());
        btnModificar.addActionListener((ActionEvent e) -> modificarCliente());
        btnEliminar.addActionListener((ActionEvent e) -> eliminarCliente());
        btnNuevo.addActionListener((ActionEvent e) -> limpiarCampos());

        // Evento para seleccionar cliente haciendo clic en la tabla
        tablaClientes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tablaClientes.getSelectedRow();
                if (fila >= 0) {
                    idClienteSeleccionado = Integer.parseInt(tablaClientes.getValueAt(fila, 0).toString());
                    txtCodigo.setText(tablaClientes.getValueAt(fila, 1).toString());
                    txtRazonSocial.setText(tablaClientes.getValueAt(fila, 2).toString());
                    txtRucDni.setText(tablaClientes.getValueAt(fila, 3).toString());
                    txtTelefono.setText(tablaClientes.getValueAt(fila, 4).toString());
                    txtCorreo.setText(tablaClientes.getValueAt(fila, 5).toString());
                    txtDireccion.setText(tablaClientes.getValueAt(fila, 6).toString());
                    cbTipoCliente.setSelectedItem(tablaClientes.getValueAt(fila, 7).toString());
                }
            }
        });
    }

    // 1. READ (Consultar MySQL y llenar la tabla)
    public void listarClientes() {
        modeloTabla.setRowCount(0); // Limpiar tabla antes de cargar
        String sql = "SELECT id_cliente, codigo, razon_social_nombres, doc_identidad, telefono, correo, direccion, tipo_cliente FROM clientes";

        try (Connection con = Conexion.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Object[] fila = new Object[8];
                fila[0] = rs.getInt("id_cliente");
                fila[1] = rs.getString("codigo");
                fila[2] = rs.getString("razon_social_nombres");
                fila[3] = rs.getString("doc_identidad");
                fila[4] = rs.getString("telefono");
                fila[5] = rs.getString("correo");
                fila[6] = rs.getString("direccion");
                fila[7] = rs.getString("tipo_cliente");
                modeloTabla.addRow(fila);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al listar clientes: " + e.getMessage());
        }
    }

    // 2. CREATE (Insertar un cliente en MySQL)
    private void guardarCliente() {
        if (txtCodigo.getText().isEmpty() || txtRazonSocial.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese al menos el Código y la Razón Social.");
            return;
        }

        String sql = "INSERT INTO clientes (codigo, razon_social_nombres, doc_identidad, telefono, correo, direccion, tipo_cliente) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtCodigo.getText());
            ps.setString(2, txtRazonSocial.getText());
            ps.setString(3, txtRucDni.getText());
            ps.setString(4, txtTelefono.getText());
            ps.setString(5, txtCorreo.getText());
            ps.setString(6, txtDireccion.getText());
            ps.setString(7, cbTipoCliente.getSelectedItem().toString());

            int res = ps.executeUpdate();
            if (res > 0) {
                JOptionPane.showMessageDialog(this, "Cliente registrado exitosamente.");
                limpiarCampos();
                listarClientes(); // Actualizar tabla
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
        }
    }

    // 3. UPDATE (Modificar cliente existente en MySQL)
    private void modificarCliente() {
        if (idClienteSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente de la tabla para modificar.");
            return;
        }

        String sql = "UPDATE clientes SET codigo=?, razon_social_nombres=?, doc_identidad=?, telefono=?, correo=?, direccion=?, tipo_cliente=? WHERE id_cliente=?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtCodigo.getText());
            ps.setString(2, txtRazonSocial.getText());
            ps.setString(3, txtRucDni.getText());
            ps.setString(4, txtTelefono.getText());
            ps.setString(5, txtCorreo.getText());
            ps.setString(6, txtDireccion.getText());
            ps.setString(7, cbTipoCliente.getSelectedItem().toString());
            ps.setInt(8, idClienteSeleccionado);

            int res = ps.executeUpdate();
            if (res > 0) {
                JOptionPane.showMessageDialog(this, "Cliente actualizado exitosamente.");
                limpiarCampos();
                listarClientes(); // Actualizar tabla
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al modificar: " + e.getMessage());
        }
    }

    // 4. DELETE (Eliminar cliente en MySQL)
    private void eliminarCliente() {
        if (idClienteSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente de la tabla para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de eliminar este cliente?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM clientes WHERE id_cliente=?";

            try (Connection con = Conexion.getConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, idClienteSeleccionado);

                int res = ps.executeUpdate();
                if (res > 0) {
                    JOptionPane.showMessageDialog(this, "Cliente eliminado exitosamente.");
                    limpiarCampos();
                    listarClientes(); // Actualizar tabla
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
            }
        }
    }

    private void limpiarCampos() {
        idClienteSeleccionado = -1;
        txtCodigo.setText("");
        txtRazonSocial.setText("");
        txtRucDni.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");
        cbTipoCliente.setSelectedIndex(0);
        tablaClientes.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmCliente().setVisible(true));
    }
}