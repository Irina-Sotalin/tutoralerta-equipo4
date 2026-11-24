# TutorAlerta – Equipo 4 (Escenario B)

Aplicación de escritorio en Java (Swing) para que un tutor registre la asistencia y las notas de su curso y pueda detectar a tiempo a quién citar a tutoría.
Proyecto IB – ISID432 Desarrollo y Mantenimiento de Software – Escuela Politécnica Nacional.

## Equipo

- **Nombre del equipo:** Equipo 4
- **Escenario:** B – TutorAlerta, alerta temprana de riesgo académico (Equipo 4)

| Integrante | Rol |
| --- | --- |
| Irina Sotalin | Desarrolladora / QA Lead |
| Milene Robalino | Scrum Master + Desarrolladora |
| Chenoa Rosales | Product Owner + Desarrolladora |
| Diego Pacheco | Desarrollador |


## Tablero de Trello

https://trello.com/b/6b0515ac6fbbdbe271057522

## Historias del Sprint 1

| Historia | Descripción |
| --- | --- |
| HU-B1 | Registrar estudiantes (lista ordenada por apellido, editar sin cambiar la cédula) |
| HU-B2 | Tomar asistencia de cada clase (una sesión por fecha) |
| HU-B3 | Registrar las notas de una evaluación para todo el curso |

## Cómo ejecutar la aplicación

Requisito: tener instalado Java 11 o superior (`java -version`).

1. Descargar o clonar el repositorio.
2. Entrar a la carpeta `dist`.
3. Hacer doble clic en `TutorAlerta.jar`, o desde una terminal: `java -jar TutorAlerta.jar` (en Windows también sirve `ejecutar.bat`).

La aplicación lee y guarda sus datos en la carpeta `dist/datos` (archivos CSV), que debe estar junto al `.jar`. Ya incluye datos de prueba ficticios: 12 estudiantes, 4 sesiones de asistencia y 2 evaluaciones.

## Cómo compilar (opcional)

Requisito: JDK 11 o superior (`javac -version`).

- Windows: doble clic en `compilar.bat`.
- Linux / macOS: `bash compilar.sh`

Esto genera `dist/TutorAlerta.jar`. También se puede abrir la carpeta en un IDE (NetBeans, IntelliJ, Eclipse) y ejecutar la clase `tutoralerta.Main` desde la raíz del proyecto.

Para correr las pruebas automáticas de las reglas de negocio: `pruebas.bat` o `./pruebas.sh`.

## Archivos de datos (CSV, UTF-8, separados por coma)

| Archivo | Columnas |
| --- | --- |
| `estudiantes.csv` | cedula, nombres, apellidos, correo |
| `asistencia.csv` | fecha (aaaa-MM-dd), cedula, presente (1 = presente, 0 = ausente) |
| `evaluaciones.csv` | id, nombre, fecha (aaaa-MM-dd) |
| `notas.csv` | id_evaluacion, cedula, calificacion |

## Reglas del negocio implementadas

- Un solo curso y una sola materia.
- La cédula es única, tiene exactamente 10 dígitos y no se puede cambiar.
- Solo puede haber una sesión de asistencia por fecha.
- Las notas van de 0 a 20 con hasta dos decimales (ver `Validador.NOTA_MAX`).
- Toda pantalla con fecha permite cambiarla; por defecto aparece la de hoy (formato dd/MM/aaaa).

## Estructura del proyecto

```
src/tutoralerta/   código fuente (Main, pantallas, Repositorio, Validador, CsvUtil)
pruebas/           pruebas automáticas de la lógica
dist/              TutorAlerta.jar + datos/ (CSV de prueba)
```

## Convención de commits

Cada mensaje empieza con el código de la historia, por ejemplo: `HU-B1: validar cédula repetida`.
