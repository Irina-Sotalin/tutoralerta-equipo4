import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import tutoralerta.*;

/** Pruebas automáticas sencillas de las reglas de las 3 historias. Ejecutar con pruebas.sh / pruebas.bat */
public class PruebasLogica {
    static int ok = 0, fallos = 0;

    static void esperarError(String nombre, Runnable r, String contiene) {
        try {
            r.run();
            fallos++;
            System.out.println("FALLA  " + nombre + " (debía dar error)");
        } catch (ValidacionException e) {
            if (contiene == null || e.getMessage().contains(contiene)) { ok++; System.out.println("ok     " + nombre); }
            else { fallos++; System.out.println("FALLA  " + nombre + " -> mensaje: " + e.getMessage()); }
        }
    }
    static void esperar(String nombre, boolean cond) {
        if (cond) { ok++; System.out.println("ok     " + nombre); } else { fallos++; System.out.println("FALLA  " + nombre); }
    }

    public static void main(String[] args) throws Exception {
        Path tmp = Files.createTempDirectory("tutoralerta-test");
        Repositorio r = new Repositorio(tmp);

        // ---- HU-B1
        r.agregarEstudiante("1750000002", "Zoila", "Zambrano", "zoila@epn.edu.ec");
        r.agregarEstudiante("1750000001", "Ana", "Andrade", "ana@epn.edu.ec");
        esperar("B1 lista ordenada por apellido", r.listarEstudiantes().get(0).getApellidos().equals("Andrade"));
        esperarError("B1 cédula repetida", () -> r.agregarEstudiante("1750000001", "X", "Y", "x@y.com"), "Ya existe");
        esperarError("B1 cédula de 9 dígitos", () -> r.agregarEstudiante("175000000", "X", "Y", "x@y.com"), "10 dígitos");
        esperarError("B1 cédula con letras", () -> r.agregarEstudiante("17500000AB", "X", "Y", "x@y.com"), "10 dígitos");
        esperarError("B1 campo vacío", () -> r.agregarEstudiante("1750000003", "", "Y", "x@y.com"), "obligatorios");
        esperarError("B1 correo inválido", () -> r.agregarEstudiante("1750000003", "X", "Y", "sin-arroba"), "correo");
        r.editarEstudiante("1750000001", "Ana María", "Andrade", "ana.m@epn.edu.ec");
        esperar("B1 edición guardada", r.listarEstudiantes().get(0).getNombres().equals("Ana María"));
        esperar("B1 no se duplicó al editar", r.listarEstudiantes().size() == 2);
        Repositorio r2 = new Repositorio(tmp); // simula cerrar y abrir
        esperar("B1 datos persisten al reabrir", r2.listarEstudiantes().size() == 2
                && r2.listarEstudiantes().get(0).getNombres().equals("Ana María"));

        // ---- HU-B2
        LocalDate d1 = LocalDate.of(2026, 11, 18);
        Map<String, Boolean> asis = new LinkedHashMap<>();
        asis.put("1750000001", true);
        asis.put("1750000002", false);
        r.guardarSesion(d1, asis);
        esperarError("B2 sesión repetida en la misma fecha", () -> r.guardarSesion(d1, asis), "Ya existe una sesión");
        esperar("B2 sesión persiste", new Repositorio(tmp).sesiones().get(d1).get("1750000002") == Boolean.FALSE);
        esperarError("B2 lista vacía", () -> r.guardarSesion(LocalDate.of(2026, 11, 19), new HashMap<>()), "No hay estudiantes");

        // ---- HU-B3
        Map<String, String> notas = new HashMap<>();
        notas.put("1750000001", "18,5");
        notas.put("1750000002", "7.25");
        int id = r.guardarEvaluacion(null, "Parcial 1", d1, notas);
        esperar("B3 guarda y recupera notas", new Repositorio(tmp).notasDe(id).get("1750000001").compareTo(new BigDecimal("18.5")) == 0);
        String[] malas = {"21", "-1", "abc", "", "7.255", "1e1", "NaN", "--3"};
        for (String m : malas) {
            Map<String, String> n = new HashMap<>(notas);
            n.put("1750000002", m);
            esperarError("B3 nota inválida \"" + m + "\" no guarda", () -> r.guardarEvaluacion(null, "Otra", d1, n), "no se guardó");
        }
        esperar("B3 nada se guardó con notas malas", r.listarEvaluaciones().size() == 1);
        Map<String, String> limites = new HashMap<>();
        limites.put("1750000001", "0");
        limites.put("1750000002", "20");
        r.guardarEvaluacion(null, "Limites", d1, limites);
        esperar("B3 acepta 0 y 20", r.listarEvaluaciones().size() == 2);
        esperarError("B3 nombre vacío", () -> r.guardarEvaluacion(null, "  ", d1, notas), "obligatorio");
        esperarError("B3 nombre repetido", () -> r.guardarEvaluacion(null, "parcial 1", d1, notas), "Ya existe");
        notas.put("1750000002", "9");
        r.guardarEvaluacion(id, "Parcial 1", d1, notas);
        esperar("B3 corregir evaluación abierta", r.notasDe(id).get("1750000002").compareTo(new BigDecimal("9")) == 0
                && r.listarEvaluaciones().size() == 2);

        // ---- Fechas
        esperarError("Fecha inválida", () -> Validador.parseFecha("31/02/2026"), "no es válida");
        esperarError("Fecha vacía", () -> Validador.parseFecha(""), "obligatoria");
        esperar("Fecha válida", Validador.parseFecha("5/1/2026").equals(LocalDate.of(2026, 1, 5)));

        // ---- Datos de prueba entregados
        Path dist = Paths.get("dist", "datos");
        if (Files.exists(dist)) {
            Repositorio real = new Repositorio(dist);
            esperar("Datos de prueba: 12 estudiantes", real.listarEstudiantes().size() == 12);
            esperar("Datos de prueba: 4 sesiones", real.sesiones().size() == 4);
            esperar("Datos de prueba: 2 evaluaciones con 12 notas", real.listarEvaluaciones().size() == 2 && real.notasDe(2).size() == 12);
        }
        System.out.println("\nResultado: " + ok + " pruebas correctas, " + fallos + " con fallas.");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
