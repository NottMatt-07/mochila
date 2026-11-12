package com.epet30.mochila.modelo;

/**
 * Un apunte o resumen guardado dentro de una materia.
 */
public class Nota {

    private int id;
    private int materiaId;
    private String titulo;
    private String contenido;
    private long fechaEdicion;

    public Nota(int materiaId, String titulo, String contenido, long fechaEdicion) {
        this(0, materiaId, titulo, contenido, fechaEdicion);
    }

    public Nota(int id, int materiaId, String titulo, String contenido, long fechaEdicion) {
        this.id = id;
        this.materiaId = materiaId;
        this.titulo = titulo;
        this.contenido = contenido;
        this.fechaEdicion = fechaEdicion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMateriaId() {
        return materiaId;
    }

    public void setMateriaId(int materiaId) {
        this.materiaId = materiaId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public long getFechaEdicion() {
        return fechaEdicion;
    }

    public void setFechaEdicion(long fechaEdicion) {
        this.fechaEdicion = fechaEdicion;
    }

    /** Primeras líneas del contenido para la vista previa en la lista. */
    public String getResumen() {
        if (contenido == null || contenido.trim().isEmpty()) {
            return "Sin contenido";
        }
        String texto = contenido.trim().replace('\n', ' ');
        return texto.length() > 90 ? texto.substring(0, 90) + "…" : texto;
    }
}
