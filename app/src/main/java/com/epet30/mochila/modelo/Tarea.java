package com.epet30.mochila.modelo;

import java.util.Calendar;

/**
 * Una tarea, un trabajo práctico o un examen de una materia.
 * Se relaciona con la clase Materia: cada tarea tiene un objeto Materia.
 */
public class Tarea {

    public static final String TIPO_TAREA = "Tarea";
    public static final String TIPO_TP = "TP";
    public static final String TIPO_EXAMEN = "Examen";

    private static final long UN_DIA_MS = 24L * 60 * 60 * 1000;

    private int id;
    private String titulo;
    private String tipo;
    private Materia materia;
    private long fecha;          // fecha y hora de entrega o del examen, en milisegundos
    private String descripcion;
    private boolean hecha;
    private boolean recordatorio;
    private long fechaHecha;     // cuándo se marcó como hecha (0 = todavía no)

    public Tarea(String titulo, String tipo, Materia materia, long fecha,
                 String descripcion, boolean recordatorio) {
        this(0, titulo, tipo, materia, fecha, descripcion, false, recordatorio, 0);
    }

    public Tarea(int id, String titulo, String tipo, Materia materia, long fecha,
                 String descripcion, boolean hecha, boolean recordatorio, long fechaHecha) {
        this.id = id;
        this.titulo = titulo;
        this.tipo = tipo;
        this.materia = materia;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.hecha = hecha;
        this.recordatorio = recordatorio;
        this.fechaHecha = fechaHecha;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isHecha() {
        return hecha;
    }

    public void setHecha(boolean hecha) {
        this.hecha = hecha;
    }

    public boolean isRecordatorio() {
        return recordatorio;
    }

    public void setRecordatorio(boolean recordatorio) {
        this.recordatorio = recordatorio;
    }

    public long getFechaHecha() {
        return fechaHecha;
    }

    public void setFechaHecha(long fechaHecha) {
        this.fechaHecha = fechaHecha;
    }

    public boolean esExamen() {
        return TIPO_EXAMEN.equals(tipo);
    }

    /** Días que faltan desde hoy hasta la fecha (0 = hoy, negativo = ya pasó). */
    public int getDiasRestantes() {
        return diasEntre(System.currentTimeMillis(), fecha);
    }

    /** Texto corto para mostrar en la lista: "Hoy", "Mañana", "En 3 días"... */
    public String getTextoDiasRestantes() {
        if (hecha) {
            return "Hecha";
        }
        int dias = getDiasRestantes();
        if (dias < 0) {
            return "Vencida";
        } else if (dias == 0) {
            return "Hoy";
        } else if (dias == 1) {
            return "Mañana";
        }
        return "En " + dias + " días";
    }

    /** Una tarea es urgente si no está hecha y vence hoy, mañana o ya venció. */
    public boolean esUrgente() {
        return !hecha && getDiasRestantes() <= 1;
    }

    public static int diasEntre(long desde, long hasta) {
        long inicio = inicioDelDia(desde);
        long fin = inicioDelDia(hasta);
        return (int) Math.round((fin - inicio) / (double) UN_DIA_MS);
    }

    public static long inicioDelDia(long millis) {
        Calendar calendario = Calendar.getInstance();
        calendario.setTimeInMillis(millis);
        calendario.set(Calendar.HOUR_OF_DAY, 0);
        calendario.set(Calendar.MINUTE, 0);
        calendario.set(Calendar.SECOND, 0);
        calendario.set(Calendar.MILLISECOND, 0);
        return calendario.getTimeInMillis();
    }
}
