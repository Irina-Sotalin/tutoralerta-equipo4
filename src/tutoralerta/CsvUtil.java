package tutoralerta;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Lectura y escritura de archivos CSV (UTF-8, separador coma) sin librerías externas. */
public final class CsvUtil {
    private CsvUtil() { }

    /** Lee todas las filas de datos (se salta el encabezado). Si el archivo no existe devuelve una lista vacía. */
    public static List<String[]> leer(Path archivo) throws IOException {
        List<String[]> filas = new ArrayList<>();
        if (!Files.exists(archivo)) {
            return filas;
        }
        List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
        for (int i = 1; i < lineas.size(); i++) { // la línea 0 es el encabezado
            String linea = lineas.get(i);
            if (!linea.trim().isEmpty()) {
                filas.add(partir(linea));
            }
        }
        return filas;
    }

    /** Escribe el archivo completo de forma segura: primero un temporal y luego lo reemplaza. */
    public static void escribir(Path archivo, String encabezado, List<String[]> filas) throws IOException {
        Files.createDirectories(archivo.toAbsolutePath().getParent());
        List<String> lineas = new ArrayList<>();
        lineas.add(encabezado);
        for (String[] fila : filas) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < fila.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(escapar(fila[i]));
            }
            lineas.add(sb.toString());
        }
        Path temporal = archivo.resolveSibling(archivo.getFileName() + ".tmp");
        Files.write(temporal, lineas, StandardCharsets.UTF_8);
        Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
    }

    static String[] partir(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (entreComillas) {
                if (c == '"') {
                    if (i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                        actual.append('"');
                        i++;
                    } else {
                        entreComillas = false;
                    }
                } else {
                    actual.append(c);
                }
            } else if (c == '"') {
                entreComillas = true;
            } else if (c == ',') {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos.toArray(new String[0]);
    }

    static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
