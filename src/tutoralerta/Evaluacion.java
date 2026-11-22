package tutoralerta;

import java.time.LocalDate;

/** Entidad Evaluación: nombre (por ejemplo "Parcial 1") y fecha. */
public final class Evaluacion {
    private final int id;
    private final String nombre;
    private final LocalDate fecha;

    public Evaluacion(int id, String nombre, LocalDate fecha) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public LocalDate getFecha() { return fecha; }

    @Override
    public String toString() {
        return nombre + " (" + Validador.formatear(fecha) + ")";
    }
}
