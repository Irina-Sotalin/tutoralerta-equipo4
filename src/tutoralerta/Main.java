package tutoralerta;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada. Los datos se leen de la carpeta "datos" que está junto al .jar. */
public final class Main {
    private Main() { }

    static Path carpetaDatos() {
        String manual = System.getProperty("datos.dir");
        if (manual != null && !manual.isEmpty()) {
            return Paths.get(manual);
        }
        try {
            Path origen = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(origen)) {
                return origen.toAbsolutePath().getParent().resolve("datos"); // dist/datos junto al .jar
            }
        } catch (URISyntaxException | RuntimeException e) {
            // se usa la carpeta por defecto
        }
        return Paths.get("dist", "datos"); // al ejecutar desde el IDE
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // se queda con el aspecto por defecto
        }
        SwingUtilities.invokeLater(() -> {
            try {
                Repositorio repo = new Repositorio(carpetaDatos());
                new VentanaPrincipal(repo).setVisible(true);
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(null,
                        "No se pudieron abrir los datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
