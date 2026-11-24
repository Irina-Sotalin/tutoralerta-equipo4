package tutoralerta;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** HU-B2: tomar asistencia de una clase. Todos aparecen presentes; se desmarca a los ausentes. */
public class PanelAsistencia extends JPanel {
    private static final long serialVersionUID = 1L;

    private final Repositorio repo;
    private final JTextField txtFecha = new JTextField(Validador.formatear(LocalDate.now()), 10);
    private final JButton btnCargar = new JButton("Cargar lista");
    private final JButton btnGuardar = new JButton("Guardar asistencia");
    private final JLabel lblAviso = new JLabel(" ");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Presente", "Cédula", "Apellidos", "Nombres"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public Class<?> getColumnClass(int columna) {
            return columna == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return columna == 0;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final DefaultTableModel modeloSesiones = new DefaultTableModel(
            new Object[]{"Fecha", "Presentes", "Ausentes", "% de asistencia"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public PanelAsistencia(Repositorio repo) {
        this.repo = repo;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel norte = new JPanel(new FlowLayout(FlowLayout.LEFT));
        norte.add(new JLabel("Fecha de la clase (dd/MM/aaaa):"));
        norte.add(txtFecha);
        norte.add(btnCargar);
        norte.add(lblAviso);
        add(norte, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(4, 4));
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnGuardar);
        botones.add(new JLabel("Desmarque a quienes NO asistieron y luego guarde."));
        centro.add(botones, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        JScrollPane scrollSesiones = new JScrollPane(new JTable(modeloSesiones));
        scrollSesiones.setBorder(BorderFactory.createTitledBorder("Sesiones registradas"));
        scrollSesiones.setPreferredSize(new Dimension(100, 150));
        add(scrollSesiones, BorderLayout.SOUTH);

        tabla.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        btnCargar.addActionListener(e -> Mensajes.proteger(this, this::cargarLista));
        txtFecha.addActionListener(e -> Mensajes.proteger(this, this::cargarLista));
        btnGuardar.addActionListener(e -> Mensajes.proteger(this, this::guardar));
        refrescar();
    }

    /** Actualiza la tabla de sesiones registradas. */
    public void refrescar() {
        modeloSesiones.setRowCount(0);
        for (Map.Entry<LocalDate, Map<String, Boolean>> s : repo.sesiones().entrySet()) {
            int presentes = 0;
            for (boolean p : s.getValue().values()) {
                if (p) {
                    presentes++;
                }
            }
            int total = s.getValue().size();
            modeloSesiones.addRow(new Object[]{Validador.formatear(s.getKey()), presentes, total - presentes,
                    total == 0 ? "-" : Math.round(presentes * 100.0 / total) + " %"});
        }
    }

    private void cargarLista() {
        LocalDate fecha = Validador.parseFecha(txtFecha.getText());
        List<Estudiante> lista = repo.listarEstudiantes();
        if (lista.isEmpty()) {
            modelo.setRowCount(0);
            throw new ValidacionException("No hay estudiantes registrados. Registre estudiantes primero.");
        }
        modelo.setRowCount(0);
        for (Estudiante e : lista) {
            modelo.addRow(new Object[]{Boolean.TRUE, e.getCedula(), e.getApellidos(), e.getNombres()});
        }
        if (repo.existeSesion(fecha)) {
            lblAviso.setForeground(new Color(180, 0, 0));
            lblAviso.setText("Ya existe una sesión con esta fecha: no se podrá guardar otra.");
        } else {
            lblAviso.setForeground(Color.DARK_GRAY);
            lblAviso.setText(" ");
        }
    }

    private void guardar() {
        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing();
        }
        if (modelo.getRowCount() == 0) {
            throw new ValidacionException("Primero elija la fecha y pulse \"Cargar lista\".");
        }
        LocalDate fecha = Validador.parseFecha(txtFecha.getText());

	if (repo.existeSesion(fecha)) {
    	throw new ValidacionException(
        "Ya existe una sesión registrada para esta fecha."
    	);
	}

	Map<String, Boolean> asistencia = new LinkedHashMap<>();
	int presentes = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            boolean presente = Boolean.TRUE.equals(modelo.getValueAt(i, 0));
            asistencia.put(String.valueOf(modelo.getValueAt(i, 1)), presente);
            if (presente) {
                presentes++;
            }
        }
        repo.guardarSesion(fecha, asistencia);
        modelo.setRowCount(0);
        lblAviso.setText(" ");
        refrescar();
        Mensajes.info(this, "Asistencia del " + Validador.formatear(fecha) + " guardada: "
                + presentes + " presentes y " + (asistencia.size() - presentes) + " ausentes.");
    }
}
