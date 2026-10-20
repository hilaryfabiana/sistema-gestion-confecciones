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

public class FrmMateriaPrima extends JFrame {

    // Componentes de la interfaz
    private JTextField txtCodigo, txtNombre, txtDescripcion, txtStock, txtPrecioUnitario;
    private JComboBox<String> cbUnidadMedida;
    private JButton btnGuardar, btnModificar, btnEliminar, btnNuevo;
    private JTable tablaMateriasPrimas;
    private DefaultTableModel modeloTabla;
    private int idMateriaPrimaSeleccionada = -1; // Almacena el ID seleccionado

    public FrmMateriaPrima() {
        initComponents();
        this.setLocationRelativeTo(null); // Centrar en pantalla
        listarMateriasPrimas(); // Cargar datos de MySQL al abrir
    }

    private void initComponents() {
        setTitle("Gestión de Materias Primas - HU03 (Iteración 2)");
        setSize(950, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- PANEL DE FORMULARIO (IZQUIERDA) ---
        JPanel panelFormulario = new JPanel(new GridLayout(9, 2, 8, 8));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos de la Materia Prima"));

        panelFormulario.add(new JLabel("Código:"));
        txtCodigo = new JTextField();
        panelFormulario.add(txtCodigo);

        panelFormulario.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelFormulario.add(txtNombre);

        panelFormulario.add(new JLabel("Descripción:"));
        txtDescripcion = new JTextField();
        panelFormulario.add(txtDescripcion);

        panelFormulario.add(new JLabel("Unidad de Medida:"));
        cbUnidadMedida = new JComboBox<>(new String[]{"Metros", "Conos", "Millares", "Unidades", "Rollos", "Kilos"});
        panelFormulario.add(cbUnidadMedida);

        panelFormulario.add(new JLabel("Stock Inicial:"));
        txtStock = new JTextField();
        panelFormulario.add(txtStock);

        panelFormulario.add(new JLabel("Precio Unitario (S/):"));
        txtPrecioUnitario = new JTextField();
        panelFormulario.add(txtPrecioUnitario);

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
            new String[]{"ID", "Código", "Nombre", "Descripción", "Unidad Medida", "Stock", "Precio Unit."}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Desactivar edición directa sobre las celdas
            }
        };

        tablaMateriasPrimas = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaMateriasPrimas);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Listado de Materias Primas"));

        add(scrollTabla, BorderLayout.CENTER);

        // --- EVENTOS DE BOTONES ---
        btnGuardar.addActionListener((ActionEvent e) -> guardarMateriaPrima());
        btnModificar.addActionListener((ActionEvent e) -> modificarMateriaPrima());
        btnEliminar.addActionListener((ActionEvent e) -> eliminarMateriaPrima());
        btnNuevo.addActionListener((ActionEvent e) -> limpiarCampos());

        // Evento para cargar la fila seleccionada en los campos de texto
        tablaMateriasPrimas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tablaMateriasPrimas.getSelectedRow();
                if (fila >= 0) {
                    idMateriaPrimaSeleccionada = Integer.parseInt(tablaMateriasPrimas.getValueAt(fila, 0).toString());
                    txtCodigo.setText(tablaMateriasPrimas.getValueAt(fila, 1).toString());
                    txtNombre.setText(tablaMateriasPrimas.getValueAt(fila, 2).toString());
                    txtDescripcion.setText(tablaMateriasPrimas.getValueAt(fila, 3).toString());
                    cbUnidadMedida.setSelectedItem(tablaMateriasPrimas.getValueAt(fila, 4).toString());
                    txtStock.setText(tablaMateriasPrimas.getValueAt(fila, 5).toString());
                    txtPrecioUnitario.setText(tablaMateriasPrimas.getValueAt(fila, 6).toString());
                }
            }
        });
    }

    // 1. READ (Listar de MySQL)
    public void listarMateriasPrimas() {
        modeloTabla.setRowCount(0);
        String sql = "SELECT id_materia_prima, codigo, nombre, descripcion, unidad_medida, stock, precio_unitario FROM materias_primas";

        try (Connection con = Conexion.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Object[] fila = new Object[7];
                fila[0] = rs.getInt("id_materia_prima");
                fila[1] = rs.getString("codigo");
                fila[2] = rs.getString("nombre");
                fila[3] = rs.getString("descripcion");
                fila[4] = rs.getString("unidad_medida");
                fila[5] = rs.getBigDecimal("stock");
                fila[6] = rs.getBigDecimal("precio_unitario");
                modeloTabla.addRow(fila);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al listar materias primas: " + e.getMessage());
        }
    }

    // 2. CREATE (Guardar en MySQL)
    private void guardarMateriaPrima() {
        if (txtCodigo.getText().isEmpty() || txtNombre.getText().isEmpty() || txtPrecioUnitario.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor completa los campos requeridos (Código, Nombre, Precio).");
            return;
        }

        String sql = "INSERT INTO materias_primas (codigo, nombre, descripcion, unidad_medida, stock, precio_unitario) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtCodigo.getText());
            ps.setString(2, txtNombre.getText());
            ps.setString(3, txtDescripcion.getText());
            ps.setString(4, cbUnidadMedida.getSelectedItem().toString());
            ps.setDouble(5, txtStock.getText().isEmpty() ? 0.0 : Double.parseDouble(txtStock.getText()));
            ps.setDouble(6, Double.parseDouble(txtPrecioUnitario.getText()));

            int res = ps.executeUpdate();
            if (res > 0) {
                JOptionPane.showMessageDialog(this, "Materia prima registrada correctamente.");
                limpiarCampos();
                listarMateriasPrimas();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingresa valores numéricos válidos en Stock y Precio.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar materia prima: " + e.getMessage());
        }
    }

    // 3. UPDATE (Modificar en MySQL)
    private void modificarMateriaPrima() {
        if (idMateriaPrimaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una materia prima de la tabla para modificar.");
            return;
        }

        String sql = "UPDATE materias_primas SET codigo=?, nombre=?, descripcion=?, unidad_medida=?, stock=?, precio_unitario=? WHERE id_materia_prima=?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtCodigo.getText());
            ps.setString(2, txtNombre.getText());
            ps.setString(3, txtDescripcion.getText());
            ps.setString(4, cbUnidadMedida.getSelectedItem().toString());
            ps.setDouble(5, Double.parseDouble(txtStock.getText()));
            ps.setDouble(6, Double.parseDouble(txtPrecioUnitario.getText()));
            ps.setInt(7, idMateriaPrimaSeleccionada);

            int res = ps.executeUpdate();
            if (res > 0) {
                JOptionPane.showMessageDialog(this, "Materia prima actualizada correctamente.");
                limpiarCampos();
                listarMateriasPrimas();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al modificar materia prima: " + e.getMessage());
        }
    }

    // 4. DELETE (Eliminar de MySQL)
    private void eliminarMateriaPrima() {
        if (idMateriaPrimaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una materia prima de la tabla para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de eliminar esta materia prima?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM materias_primas WHERE id_materia_prima=?";

            try (Connection con = Conexion.getConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, idMateriaPrimaSeleccionada);

                int res = ps.executeUpdate();
                if (res > 0) {
                    JOptionPane.showMessageDialog(this, "Materia prima eliminada correctamente.");
                    limpiarCampos();
                    listarMateriasPrimas();
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar materia prima: " + e.getMessage());
            }
        }
    }

    private void limpiarCampos() {
        idMateriaPrimaSeleccionada = -1;
        txtCodigo.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        cbUnidadMedida.setSelectedIndex(0);
        txtStock.setText("");
        txtPrecioUnitario.setText("");
        tablaMateriasPrimas.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmMateriaPrima().setVisible(true));
    }
}