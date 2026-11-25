package tutoralerta;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** HU-B1: registrar estudiantes, ver la lista ordenada por apellido y editar sus datos (no la cédula). */
public class PanelEstudiantes extends JPanel {
    private static final long serialVersionUID = 1L;

    private final Repositorio repo;
    private final JTextField txtCedula = new JTextField(12);
    private final JTextField txtNombres = new JTextField(20);
    private final JTextField txtApellidos = new JTextField(20);
    private final JTextField txtCorreo = new JTextField(24);
    private final JButton btnRegistrar = new JButton("Registrar estudiante");
    private final JButton btnGuardar = new JButton("Guardar cambios");
    private final JButton btnNuevo = new JButton("Limpiar / Nuevo");
    private final JLabel lblEstado = new JLabel(" ");
    private final JLabel lblTotal = new JLabel(" ");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Cédula", "Apellidos", "Nombres", "Correo"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private boolean actualizando = false;

    public PanelEstudiantes(Repositorio repo) {
        this.repo = repo;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del estudiante"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        agregarCampo(formulario, g, 0, "Cédula (10 dígitos):", txtCedula);
        agregarCampo(formulario, g, 1, "Nombres:", txtNombres);
        agregarCampo(formulario, g, 2, "Apellidos:", txtApellidos);
        agregarCampo(formulario, g, 3, "Correo:", txtCorreo);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnRegistrar);
        botones.add(btnGuardar);
        botones.add(btnNuevo);
        g.gridx = 1;
        g.gridy = 4;
        formulario.add(botones, g);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(formulario, BorderLayout.CENTER);
        norte.add(lblEstado, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(false);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout());
        pie.add(new JLabel("Seleccione un estudiante de la lista para editar sus nombres, apellidos o correo."),
                BorderLayout.WEST);
        pie.add(lblTotal, BorderLayout.EAST);
        add(pie, BorderLayout.SOUTH);

        btnRegistrar.addActionListener(e -> Mensajes.proteger(this, this::registrar));
        btnGuardar.addActionListener(e -> Mensajes.proteger(this, this::guardarCambios));
        btnNuevo.addActionListener(e -> limpiar());
        txtCorreo.addActionListener(e -> Mensajes.proteger(this, () -> { // Enter en el último campo
            if (btnRegistrar.isEnabled()) {
                registrar();
            } else {
                guardarCambios();
            }
        }));
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !actualizando) {
                seleccionar();
            }
        });
        limpiar();
        refrescar();
    }

    private void agregarCampo(JPanel panel, GridBagConstraints g, int fila, String etiqueta, JTextField campo) {
        g.gridx = 0;
        g.gridy = fila;
        panel.add(new JLabel(etiqueta), g);
        g.gridx = 1;
        panel.add(campo, g);
    }

    /** Vuelve a leer la lista desde el repositorio. */
    public void refrescar() {
        actualizando = true;
        modelo.setRowCount(0);
        for (Estudiante e : repo.listarEstudiantes()) {
            modelo.addRow(new Object[]{e.getCedula(), e.getApellidos(), e.getNombres(), e.getCorreo()});
        }
        actualizando = false;
        lblTotal.setText("Total de estudiantes: " + modelo.getRowCount());
    }

    private void registrar() {
        repo.agregarEstudiante(txtCedula.getText(), txtNombres.getText(), txtApellidos.getText(),
                txtCorreo.getText());
        refrescar();
        limpiar();
        estado("Estudiante registrado correctamente.", new Color(0, 120, 0));
    }

    private void guardarCambios() {
        repo.editarEstudiante(txtCedula.getText(), txtNombres.getText(), txtApellidos.getText(),
                txtCorreo.getText());
        refrescar();
        limpiar();
        estado("Cambios guardados correctamente.", new Color(0, 120, 0));
    }

    private void seleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        txtCedula.setText(String.valueOf(modelo.getValueAt(fila, 0)));
        txtApellidos.setText(String.valueOf(modelo.getValueAt(fila, 1)));
        txtNombres.setText(String.valueOf(modelo.getValueAt(fila, 2)));
        txtCorreo.setText(String.valueOf(modelo.getValueAt(fila, 3)));
        txtCedula.setEditable(false); // la cédula no se puede cambiar
        txtCedula.setBackground(new Color(235, 235, 235)); // se ve bloqueada
        btnRegistrar.setEnabled(false);
        btnGuardar.setEnabled(true);
        estado("Editando: la cédula no se puede modificar.", Color.DARK_GRAY);
    }

    private void limpiar() {
        actualizando = true;
        tabla.clearSelection();
        actualizando = false;
        txtCedula.setText("");
        txtNombres.setText("");
        txtApellidos.setText("");
        txtCorreo.setText("");
        txtCedula.setEditable(true);
        txtCedula.setBackground(Color.WHITE);
        btnRegistrar.setEnabled(true);
        btnGuardar.setEnabled(false);
    }

    private void estado(String texto, Color color) {
        lblEstado.setForeground(color);
        lblEstado.setText(texto);
    }
}
