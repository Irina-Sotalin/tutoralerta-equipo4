package tutoralerta;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.Collator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Guarda y lee todos los datos en archivos CSV dentro de una carpeta:
 * estudiantes.csv, asistencia.csv, evaluaciones.csv y notas.csv.
 * Cada cambio se escribe en disco de inmediato.
 */
public class Repositorio {
    private static final String ENC_ESTUDIANTES = "cedula,nombres,apellidos,correo";
    private static final String ENC_ASISTENCIA = "fecha,cedula,presente";
    private static final String ENC_EVALUACIONES = "id,nombre,fecha";
    private static final String ENC_NOTAS = "id_evaluacion,cedula,calificacion";

    private interface Escritura {
        void ejecutar() throws IOException;
    }

    private final Path carpeta;
    private final List<Estudiante> estudiantes = new ArrayList<>();
    private final TreeMap<LocalDate, Map<String, Boolean>> sesiones = new TreeMap<>();
    private final List<Evaluacion> evaluaciones = new ArrayList<>();
    private final Map<Integer, Map<String, BigDecimal>> notas = new HashMap<>();

    public Repositorio(Path carpeta) {
        this.carpeta = carpeta;
        cargar();
    }

    public Path getCarpeta() {
        return carpeta;
    }

    // ------------------------------------------------------------------ carga

    public final synchronized void cargar() {
        estudiantes.clear();
        sesiones.clear();
        evaluaciones.clear();
        notas.clear();
        try {
            for (String[] f : CsvUtil.leer(carpeta.resolve("estudiantes.csv"))) {
                if (f.length >= 4) {
                    estudiantes.add(new Estudiante(f[0].trim(), f[1].trim(), f[2].trim(), f[3].trim()));
                }
            }
            for (String[] f : CsvUtil.leer(carpeta.resolve("asistencia.csv"))) {
                if (f.length >= 3) {
                    LocalDate fecha = Validador.parseIso(f[0]);
                    if (fecha != null) {
                        sesiones.computeIfAbsent(fecha, k -> new LinkedHashMap<>())
                                .put(f[1].trim(), f[2].trim().equals("1"));
                    }
                }
            }
            for (String[] f : CsvUtil.leer(carpeta.resolve("evaluaciones.csv"))) {
                if (f.length >= 3) {
                    LocalDate fecha = Validador.parseIso(f[2]);
                    try {
                        if (fecha != null) {
                            evaluaciones.add(new Evaluacion(Integer.parseInt(f[0].trim()), f[1].trim(), fecha));
                        }
                    } catch (NumberFormatException e) {
                        // fila dañada: se ignora
                    }
                }
            }
            for (String[] f : CsvUtil.leer(carpeta.resolve("notas.csv"))) {
                if (f.length >= 3) {
                    try {
                        int id = Integer.parseInt(f[0].trim());
                        notas.computeIfAbsent(id, k -> new LinkedHashMap<>())
                                .put(f[1].trim(), new BigDecimal(f[2].trim()));
                    } catch (NumberFormatException e) {
                        // fila dañada: se ignora
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ------------------------------------------------------- HU-B1 estudiantes

    /** Estudiantes ordenados por apellido (y luego por nombres). */
    public synchronized List<Estudiante> listarEstudiantes() {
        Collator collator = Collator.getInstance(new Locale("es", "EC"));
        List<Estudiante> copia = new ArrayList<>(estudiantes);
        copia.sort((a, b) -> {
            int c = collator.compare(a.getApellidos(), b.getApellidos());
            return c != 0 ? c : collator.compare(a.getNombres(), b.getNombres());
        });
        return copia;
    }

    public synchronized void agregarEstudiante(String cedula, String nombres, String apellidos, String correo) {
        cedula = limpiar(cedula);
        nombres = limpiar(nombres);
        apellidos = limpiar(apellidos);
        correo = limpiar(correo);
        Validador.validarEstudiante(cedula, nombres, apellidos, correo);
        if (indiceDe(cedula) >= 0) {
            throw new ValidacionException("Ya existe un estudiante con la cédula " + cedula + ". No se guardó.");
        }
        estudiantes.add(new Estudiante(cedula, nombres, apellidos, correo));
        persistir(this::guardarEstudiantes);
    }

    /** Edita nombres, apellidos y correo. La cédula identifica al estudiante y no se puede cambiar. */
    public synchronized void editarEstudiante(String cedula, String nombres, String apellidos, String correo) {
        cedula = limpiar(cedula);
        nombres = limpiar(nombres);
        apellidos = limpiar(apellidos);
        correo = limpiar(correo);
        int indice = indiceDe(cedula);
        if (indice < 0) {
            throw new ValidacionException("No existe un estudiante con la cédula " + cedula + ".");
        }
        Validador.validarEstudiante(cedula, nombres, apellidos, correo);
        estudiantes.set(indice, new Estudiante(cedula, nombres, apellidos, correo));
        persistir(this::guardarEstudiantes);
    }

    // --------------------------------------------------------- HU-B2 asistencia

    public synchronized boolean existeSesion(LocalDate fecha) {
        return sesiones.containsKey(fecha);
    }

    /** Sesiones registradas, de la más reciente a la más antigua. Cédula -> presente. */
    public synchronized NavigableMap<LocalDate, Map<String, Boolean>> sesiones() {
        return Collections.unmodifiableNavigableMap(new TreeMap<>(sesiones).descendingMap());
    }

    public synchronized void guardarSesion(LocalDate fecha, Map<String, Boolean> asistencia) {
        if (fecha == null) {
            throw new ValidacionException("La fecha es obligatoria.");
        }
        if (sesiones.containsKey(fecha)) {
            throw new ValidacionException("Ya existe una sesión registrada el " + Validador.formatear(fecha)
                    + ". No se creó otra.");
        }
        if (asistencia == null || asistencia.isEmpty()) {
            throw new ValidacionException("No hay estudiantes en la lista. Registre estudiantes primero.");
        }
        sesiones.put(fecha, new LinkedHashMap<>(asistencia));
        persistir(this::guardarAsistencia);
    }

    // ------------------------------------------------------------ HU-B3 notas

    public synchronized List<Evaluacion> listarEvaluaciones() {
        return new ArrayList<>(evaluaciones);
    }

    public synchronized Evaluacion buscarEvaluacion(int id) {
        for (Evaluacion e : evaluaciones) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }

    /** Notas guardadas de una evaluación: cédula -> calificación. */
    public synchronized Map<String, BigDecimal> notasDe(int idEvaluacion) {
        Map<String, BigDecimal> guardadas = notas.get(idEvaluacion);
        return guardadas == null ? new HashMap<>() : new HashMap<>(guardadas);
    }

    /**
     * Guarda una evaluación con las notas de TODO el curso, o no guarda nada.
     *
     * @param id         null para una evaluación nueva; el id de una existente para corregirla
     * @param notasTexto cédula -> texto escrito por el usuario
     * @return el id de la evaluación guardada
     */
    public synchronized int guardarEvaluacion(Integer id, String nombre, LocalDate fecha,
                                              Map<String, String> notasTexto) {
        nombre = limpiar(nombre);
        if (nombre.isEmpty()) {
            throw new ValidacionException("El nombre de la evaluación es obligatorio.");
        }
        if (fecha == null) {
            throw new ValidacionException("La fecha de la evaluación es obligatoria.");
        }
        if (estudiantes.isEmpty()) {
            throw new ValidacionException("No hay estudiantes registrados. Registre estudiantes primero.");
        }
        for (Evaluacion e : evaluaciones) {
            if (e.getNombre().equalsIgnoreCase(nombre) && (id == null || e.getId() != id.intValue())) {
                throw new ValidacionException("Ya existe una evaluación llamada \"" + nombre + "\".");
            }
        }
        Map<String, BigDecimal> nuevas = new LinkedHashMap<>();
        for (Estudiante est : listarEstudiantes()) {
            String texto = notasTexto == null ? null : notasTexto.get(est.getCedula());
            try {
                nuevas.put(est.getCedula(), Validador.parseNota(texto));
            } catch (ValidacionException ex) {
                throw new ValidacionException("Nota inválida de " + est.getApellidos() + " " + est.getNombres()
                        + ": " + ex.getMessage() + " La evaluación no se guardó.");
            }
        }
        int idFinal;
        if (id == null) {
            int maximo = 0;
            for (Evaluacion e : evaluaciones) {
                maximo = Math.max(maximo, e.getId());
            }
            idFinal = maximo + 1;
            evaluaciones.add(new Evaluacion(idFinal, nombre, fecha));
        } else {
            idFinal = id.intValue();
            int indice = -1;
            for (int i = 0; i < evaluaciones.size(); i++) {
                if (evaluaciones.get(i).getId() == idFinal) {
                    indice = i;
                }
            }
            if (indice < 0) {
                throw new ValidacionException("La evaluación que intenta corregir ya no existe.");
            }
            evaluaciones.set(indice, new Evaluacion(idFinal, nombre, fecha));
        }
        notas.put(idFinal, nuevas);
        persistir(this::guardarEvaluacionesYNotas);
        return idFinal;
    }

    // ------------------------------------------------------------ utilidades

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
    }

    private int indiceDe(String cedula) {
        for (int i = 0; i < estudiantes.size(); i++) {
            if (estudiantes.get(i).getCedula().equals(cedula)) {
                return i;
            }
        }
        return -1;
    }

    /** Ejecuta la escritura; si falla, vuelve a leer del disco para que memoria y archivos coincidan. */
    private void persistir(Escritura escritura) {
        try {
            escritura.ejecutar();
        } catch (IOException e) {
            cargar();
            throw new ValidacionException("No se pudo guardar en el disco (" + e.getMessage()
                    + "). Verifique que la carpeta de datos no esté protegida ni abierta en otro programa.");
        }
    }

    private void guardarEstudiantes() throws IOException {
        List<String[]> filas = new ArrayList<>();
        for (Estudiante e : estudiantes) {
            filas.add(new String[]{e.getCedula(), e.getNombres(), e.getApellidos(), e.getCorreo()});
        }
        CsvUtil.escribir(carpeta.resolve("estudiantes.csv"), ENC_ESTUDIANTES, filas);
    }

    private void guardarAsistencia() throws IOException {
        List<String[]> filas = new ArrayList<>();
        for (Map.Entry<LocalDate, Map<String, Boolean>> sesion : sesiones.entrySet()) {
            for (Map.Entry<String, Boolean> a : sesion.getValue().entrySet()) {
                filas.add(new String[]{sesion.getKey().toString(), a.getKey(), a.getValue() ? "1" : "0"});
            }
        }
        CsvUtil.escribir(carpeta.resolve("asistencia.csv"), ENC_ASISTENCIA, filas);
    }

    private void guardarEvaluacionesYNotas() throws IOException {
        List<String[]> filasEval = new ArrayList<>();
        for (Evaluacion e : evaluaciones) {
            filasEval.add(new String[]{String.valueOf(e.getId()), e.getNombre(), e.getFecha().toString()});
        }
        CsvUtil.escribir(carpeta.resolve("evaluaciones.csv"), ENC_EVALUACIONES, filasEval);
        List<String[]> filasNotas = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, BigDecimal>> ev : notas.entrySet()) {
            for (Map.Entry<String, BigDecimal> n : ev.getValue().entrySet()) {
                filasNotas.add(new String[]{String.valueOf(ev.getKey()), n.getKey(), n.getValue().toPlainString()});
            }
        }
        CsvUtil.escribir(carpeta.resolve("notas.csv"), ENC_NOTAS, filasNotas);
    }
}
