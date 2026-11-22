package tutoralerta;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

/** Ventana principal de TutorAlerta con una pestaña por historia del sprint 1. */
public class VentanaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;

    public VentanaPrincipal(Repositorio repo) {
        super("TutorAlerta - Registro de asistencia y notas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(820, 560));

        PanelEstudiantes estudiantes = new PanelEstudiantes(repo);
        PanelAsistencia asistencia = new PanelAsistencia(repo);
        PanelNotas notas = new PanelNotas(repo);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("1. Estudiantes", estudiantes);
        pestanas.addTab("2. Asistencia", asistencia);
        pestanas.addTab("3. Notas", notas);
        pestanas.addChangeListener(e -> {
            estudiantes.refrescar();
            asistencia.refrescar();
            notas.refrescar();
        });

        JLabel pie = new JLabel("Datos guardados en: " + repo.getCarpeta().toAbsolutePath());
        pie.setBorder(BorderFactory.createEmptyBorder(2, 10, 6, 10));
        add(pestanas, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
        pack();
        setSize(900, 640);
        setLocationRelativeTo(null);
    }
}
