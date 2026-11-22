package tutoralerta;

/** Entidad Estudiante: cédula (10 dígitos), nombres, apellidos y correo. */
public final class Estudiante {
    private final String cedula;
    private final String nombres;
    private final String apellidos;
    private final String correo;

    public Estudiante(String cedula, String nombres, String apellidos, String correo) {
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
    }

    public String getCedula() { return cedula; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getCorreo() { return correo; }
}
