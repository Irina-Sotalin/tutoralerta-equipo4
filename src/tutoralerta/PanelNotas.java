package tutoralerta;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** HU-B3: registrar las notas de una evaluación para todo el curso y volver a abrirlas. */
public class PanelNotas extends JPanel {
    private static final long serialVersionUID = 1L;

    private final Repositorio repo;
    private final JTextField txtNombre = new JTextField(18);
    private final JTextField txtFecha = new JTextField(Validador.formatear(LocalDate.now()), 10);
    private final JButton btnCargar = new JButton("Cargar estudiantes");
    private final JButton btnGuardar = new JButton("Guardar evaluación");
    private final JButton btnNueva = new JButton("Nueva evaluación");
    private final JButton btnAbrir = new JButton("Abrir");
    private final DefaultComboBoxModel<Evaluacion> modeloCombo = new DefaultComboBoxModel<>();
    private final JComboBox<Evaluacion> combo = new JComboBox<>(modeloCombo);
    private final JLabel lblEstado = new JLabel(" ");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Cédula", "Apellidos", "Nombres",
                    "Nota (0 a " + Validador.NOTA_MAX.toPlainString() + ")", "Promedio"},
            0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return columna == 3;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private Integer idAbierta = null; // null = evaluación nueva

    public PanelNotas(Repositorio repo) {
        this.repo = repo;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel abrir = new JPanel(new FlowLayout(FlowLayout.LEFT));
        abrir.add(new JLabel("Evaluaciones guardadas:"));
        abrir.add(combo);
        abrir.add(btnAbrir);
        abrir.add(btnNueva);

        JPanel datos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datos.add(new JLabel("Nombre de la evaluación:"));
        datos.add(txtNombre);
        datos.add(new JLabel("Fecha (dd/MM/aaaa):"));
        datos.add(txtFecha);
        datos.add(btnCargar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(abrir, BorderLayout.NORTH);
        norte.add(datos, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sur.add(btnGuardar);
        sur.add(lblEstado);
        add(sur, BorderLayout.SOUTH);

        tabla.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        btnCargar.addActionListener(e -> Mensajes.proteger(this, this::cargarEstudiantes));
        btnGuardar.addActionListener(e -> Mensajes.proteger(this, this::guardar));
        btnNueva.addActionListener(e -> nueva());
        btnAbrir.addActionListener(e -> Mensajes.proteger(this, this::abrir));
        refrescar();
    }

    /** Actualiza la lista de evaluaciones guardadas. */
    public void refrescar() {
        modeloCombo.removeAllElements();
        for (Evaluacion e : repo.listarEvaluaciones()) {
            modeloCombo.addElement(e);
        }
        if (idAbierta != null) {
            Evaluacion abierta = repo.buscarEvaluacion(idAbierta);
            if (abierta != null) {
                for (int i = 0; i < modeloCombo.getSize(); i++) {
                    if (modeloCombo.getElementAt(i).getId() == idAbierta) {
                        combo.setSelectedIndex(i);
                    }
                }
            }
        }
    }

    private void nueva() {
        idAbierta = null;
        txtNombre.setText("");
        txtFecha.setText(Validador.formatear(LocalDate.now()));
        modelo.setRowCount(0);
        estado("Nueva evaluación: escriba el nombre y la fecha, y pulse \"Cargar estudiantes\".", Color.DARK_GRAY);
    }

    private void cargarEstudiantes() {
        Validador.parseFecha(txtFecha.getText());
        llenarTabla(idAbierta == null ? new HashMap<String, BigDecimal>() : repo.notasDe(idAbierta));
    }

    private void llenarTabla(Map<String, BigDecimal> guardadas) {
        List<Estudiante> lista = repo.listarEstudiantes();
        if (lista.isEmpty()) {
            modelo.setRowCount(0);
            throw new ValidacionException("No hay estudiantes registrados. Registre estudiantes primero.");
        }
        modelo.setRowCount(0);
        for (Estudiante e : lista) {
            BigDecimal nota = guardadas.get(e.getCedula());
            BigDecimal promedio = repo.promedioDe(e.getCedula());
            modelo.addRow(new Object[]{
                    e.getCedula(),
                    e.getApellidos(),
                    e.getNombres(),
                    nota == null ? "" : nota.toPlainString(),
                    promedio == null ? "-" : promedio.toPlainString()
            });
        }
    }

    private void abrir() {
        Evaluacion seleccionada = (Evaluacion) combo.getSelectedItem();
        if (seleccionada == null) {
            throw new ValidacionException("Todavía no hay evaluaciones guardadas.");
        }
        idAbierta = seleccionada.getId();
        txtNombre.setText(seleccionada.getNombre());
        txtFecha.setText(Validador.formatear(seleccionada.getFecha()));
        llenarTabla(repo.notasDe(idAbierta));
        estado("Evaluación abierta. Puede corregir las notas y guardar de nuevo.", Color.DARK_GRAY);
    }

    private void guardar() {
        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing();
        }
        if (modelo.getRowCount() == 0) {
            throw new ValidacionException("Primero escriba el nombre y la fecha y pulse \"Cargar estudiantes\".");
        }
        LocalDate fecha = Validador.parseFecha(txtFecha.getText());
        Map<String, String> notas = new HashMap<>();
        for (int i = 0; i < modelo.getRowCount(); i++) {
            notas.put(String.valueOf(modelo.getValueAt(i, 0)), String.valueOf(modelo.getValueAt(i, 3)));
        }
        idAbierta = repo.guardarEvaluacion(idAbierta, txtNombre.getText(), fecha, notas);
        refrescar();
        llenarTabla(repo.notasDe(idAbierta));
        estado("Evaluación guardada. Puede verla de nuevo con \"Abrir\".", new Color(0, 120, 0));
        Mensajes.info(this, "La evaluación se guardó con las notas de todo el curso.");
    }

    private void estado(String texto, Color color) {
        lblEstado.setForeground(color);
        lblEstado.setText(texto);
    }
}