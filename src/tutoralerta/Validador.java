package tutoralerta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Todas las validaciones de campos y reglas de formato en un solo lugar. */
public final class Validador {
    private Validador() { }

    public static final BigDecimal NOTA_MIN = BigDecimal.ZERO;

    /**
     * Nota máxima. La regla del negocio dice "de 0 a 20" (calificación sobre 20),
     * pero el criterio de aceptación de HU-B3 dice "0 a 10". Confirmar con la docente.
     * Si fuera 10, cambiar este valor a new BigDecimal("10") y volver a compilar.
     */
    public static final BigDecimal NOTA_MAX = new BigDecimal("20");

    private static final DateTimeFormatter LECTURA =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter PANTALLA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String formatear(LocalDate fecha) {
        return fecha.format(PANTALLA);
    }

    /** Fecha escrita por el usuario en formato dd/MM/aaaa. */
    public static LocalDate parseFecha(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new ValidacionException("La fecha es obligatoria (formato dd/MM/aaaa).");
        }
        try {
            return LocalDate.parse(texto.trim(), LECTURA);
        } catch (DateTimeParseException e) {
            throw new ValidacionException("La fecha \"" + texto.trim()
                    + "\" no es válida. Use el formato dd/MM/aaaa, por ejemplo 18/11/2026.");
        }
    }

    /** Fecha guardada en los CSV (formato ISO aaaa-MM-dd). Devuelve null si no se puede leer. */
    public static LocalDate parseIso(String texto) {
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    public static void validarEstudiante(String cedula, String nombres, String apellidos, String correo) {
        if (cedula.isEmpty()) {
            throw new ValidacionException("La cédula es obligatoria.");
        }
        if (!cedula.matches("\\d{10}")) {
            throw new ValidacionException("La cédula debe tener exactamente 10 dígitos numéricos. No se guardó.");
        }
        if (nombres.isEmpty()) {
            throw new ValidacionException("Los nombres son obligatorios.");
        }
        if (apellidos.isEmpty()) {
            throw new ValidacionException("Los apellidos son obligatorios.");
        }
        if (correo.isEmpty()) {
            throw new ValidacionException("El correo es obligatorio.");
        }
        if (!correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new ValidacionException("El correo no tiene un formato válido (ejemplo: nombre@epn.edu.ec).");
        }
    }

    /** Convierte el texto de una nota a número. Lanza ValidacionException con el motivo si no es válida. */
    public static BigDecimal parseNota(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new ValidacionException("está vacía.");
        }
        String s = texto.trim().replace(',', '.');
        if (!s.matches("-?\\d+(\\.\\d+)?")) {
            throw new ValidacionException("debe ser un número, sin letras ni símbolos.");
        }
        BigDecimal valor = new BigDecimal(s);
        if (valor.compareTo(NOTA_MIN) < 0 || valor.compareTo(NOTA_MAX) > 0) {
            throw new ValidacionException("debe estar entre 0 y " + NOTA_MAX.toPlainString() + ".");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new ValidacionException("puede tener como máximo 2 decimales.");
        }
        return valor.setScale(2);
    }
}
