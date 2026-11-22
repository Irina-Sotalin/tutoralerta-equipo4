package tutoralerta;

/** Error de regla de negocio o de validación. Su mensaje se muestra tal cual al usuario. */
public class ValidacionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
