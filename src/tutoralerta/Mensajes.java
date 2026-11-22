package tutoralerta;

import java.awt.Component;
import javax.swing.JOptionPane;

/** Ventanas de mensaje y protección contra errores inesperados (la aplicación no debe caerse). */
public final class Mensajes {
    private Mensajes() { }

    public static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Revise los datos", JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Listo", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Ejecuta la acción y muestra cualquier error como mensaje en lugar de cerrar el programa. */
    public static void proteger(Component padre, Runnable accion) {
        try {
            accion.run();
        } catch (ValidacionException e) {
            aviso(padre, e.getMessage());
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(padre, "Ocurrió un error inesperado: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
