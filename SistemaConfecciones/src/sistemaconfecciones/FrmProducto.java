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

public class FrmProducto extends JFrame {

    // Componentes de la interfaz
    private JTextField txtCodigo, txtNombre, txtDescripcion, txtTalla, txtColor, txtPrecio, txtStock;
    private JComboBox<String> cbEstado;
    private JButton btnGuardar, btnModificar, btnEliminar, btnNuevo;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private int idProductoSeleccionado = -1; // Almacena el ID seleccionado de la tabla

    public FrmProducto() {
        initComponents();
        this.setLocationRelativeTo(null); // Centrar en pantalla
        listarProductos(); // Cargar datos de MySQL al abrir
    }

    private void initComponents() {
        setTitle("Gestión de Productos Confeccionados - HU02 (Iteración 1)");
        setSize(950, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- PANEL DE FORMULARIO (IZQUIERDA) ---
        JPanel panelFormulario = new JPanel(new GridLayout(10, 2, 8, 8));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Producto"));

        panelFormulario.add(new JLabel("Código:"));
        txtCodigo = new JTextField();
        panelFormulario.add(txtCodigo);

        panelFormulario.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelFormulario.add(txtNombre);

        panelFormulario.add(new JLabel("Descripción:"));
        txtDescripcion = new JTextField();
        panelFormulario.add(txtDescripcion);

        panelFormulario.add(new JLabel("Talla (S/M/L/XL):"));
        txtTalla = new JTextField();
        panelFormulario.add(txtTalla);

        panelFormulario.add(new JLabel("Color:"));
        txtColor = new JTextField();
        panelFormulario.add(txtColor);

        panelFormulario.add(new JLabel("Precio (S/):"));
        txtPrecio = new JTextField();
        panelFormulario.add(txtPrecio);

        panelFormulario.add(new JLabel("Stock:"));
        txtStock = new JTextField();
        panelFormulario.add(txtStock);

        panelFormulario.add(new JLabel("Estado:"));
        cbEstado = new JComboBox<>(new String[]{"Activo", "Inactivo"});
        panelFormulario.add(cbEstado);

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
            new String[]{"ID", "Código", "Nombre", "Talla", "Color", "Precio", "Stock", "Estado"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Desactivar edición directa en celdas
            }
        };
        
        tablaProductos = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaProductos);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Listado de Productos"));

        add(scrollTabla, BorderLayout.CENTER);

        // --- EVENTOS ---
        btnGuardar.addActionListener((ActionEvent e) -> guardarProducto());
        btnModificar.addActionListener((ActionEvent e) -> modificarProducto());
        btnEliminar.addActionListener((ActionEvent e) -> eliminarProducto());
        btnNuevo.addActionListener((ActionEvent e) -> limpiarCampos());

        // Cargar fila seleccionada en las cajas de texto
        tablaProductos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tablaProductos.getSelectedRow();
                if (fila >= 0) {
                    idProductoSeleccionado = Integer.parseInt(tablaProductos.getValueAt(fila, 0).toString());
                    txtCodigo.setText(tablaProductos.getValueAt(fila, 1).toString());
                    txtNombre.setText(tablaProductos.getValueAt(fila, 2).toString());
                    txtTalla.setText(tablaProductos.getValueAt(fila, 3).toString());
                    txtColor.setText(tablaProductos.getValueAt(fila, 4).toString());
                    txtPrecio.setText(tablaProductos.getValueAt(fila, 5).toString());
                    txtStock.setText(tablaProductos.getValueAt(fila, 6).toString());
                    cbEstado.setSelectedItem(tablaProductos.getValueAt(fila, 7).toString());
                }
            }
        });
    }

    // 1. READ (Listar)
    public void listarProductos() {
        modeloTabla.setRowCount(0);
        String sql = "SELECT id_producto, codigo, nombre, talla, color, precio, stock, estado FROM productos";

        try (Connection con = Conexion.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Object[] fila = new Object[8];
                fila[0] = rs.getInt("id_producto");
                fila[1] = rs.getString("codigo");
                fila[2] = rs.getString("nombre");
                fila[3] = rs.getString("talla");
                fila[4] = rs.getString("color");
                fila[5] = rs.getBigDecimal("precio");
                fila[6] = rs.getInt("stock");
                fila[7] = rs.getString("estado");
                modeloTabla.addRow(fila);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al listar productos: " + e.getMessage());
        }
    }

    // 2. CREATE (Guardar)
    private void guardarProducto() {
        if (txtCodigo.getText().isEmpty() || txtNombre.getText().isEmpty() || txtPrecio.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor completa los campos requeridos (Código, Nombre, Precio).");
            return;
        }

        String sql = "INSERT INTO productos (codigo, nombre, descripcion, talla, color, precio, stock, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtCodigo.getText());
            ps.setString(2, txtNombre.getText());
            ps.setString(3, txtDescripcion.getText());
            ps.setString(4, txtTalla.getText());
            ps.setString(5, txtColor.getText());
            ps.setDouble(6, Double.parseDouble(txtPrecio.getText()));
            ps.setInt(7, txtStock.getText().isEmpty() ? 0 : Integer.parseInt(txtStock.getText()));
            ps.setString(8, cbEstado.getSelectedItem().toString());

            int res = ps.executeUpdate();
            if (res > 0) {
                JOptionPane.showMessageDialog(this, "Producto registrado correctamente.");
                limpiarCampos();
                listarProductos();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar producto: " + e.getMessage());
        }
    }

    // 3. UPDATE (Modificar)
    private void modificarProducto() {
        if (idProductoSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla para modificar.");
            return;
        }

        String sql = "UPDATE productos SET codigo=?, nombre=?, descripcion=?, talla=?, color=?, precio=?, stock=?, estado=? WHERE id_producto=?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtCodigo.getText());
            ps.setString(2, txtNombre.getText());
            ps.setString(3, txtDescripcion.getText());
            ps.setString(4, txtTalla.getText());
            ps.setString(5, txtColor.getText());
            ps.setDouble(6, Double.parseDouble(txtPrecio.getText()));
            ps.setInt(7, Integer.parseInt(txtStock.getText()));
            ps.setString(8, cbEstado.getSelectedItem().toString());
            ps.setInt(9, idProductoSeleccionado);

            int res = ps.executeUpdate();
            if (res > 0) {
                JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.");
                limpiarCampos();
                listarProductos();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al modificar producto: " + e.getMessage());
        }
    }

    // 4. DELETE (Eliminar)
    private void eliminarProducto() {
        if (idProductoSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de eliminar este producto?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM productos WHERE id_producto=?";

            try (Connection con = Conexion.getConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, idProductoSeleccionado);

                int res = ps.executeUpdate();
                if (res > 0) {
                    JOptionPane.showMessageDialog(this, "Producto eliminado correctamente.");
                    limpiarCampos();
                    listarProductos();
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar producto: " + e.getMessage());
            }
        }
    }

    private void limpiarCampos() {
        idProductoSeleccionado = -1;
        txtCodigo.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtTalla.setText("");
        txtColor.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        cbEstado.setSelectedIndex(0);
        tablaProductos.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmProducto().setVisible(true));
    }
}