package com.epet30.mochila.modelo;

import android.graphics.Color;

/**
 * Representa una materia de la escuela (por ejemplo, Física o Programación).
 * Cada tarea y cada nota pertenecen a una materia.
 */
public class Materia {

    private int id;
    private String nombre;
    private String color;

    // Datos calculados para mostrar en la lista (no se guardan en la tabla materias)
    private int cantidadPendientes;
    private int cantidadNotas;
    private int minutosEstudiados;

    public Materia(String nombre, String color) {
        this(0, nombre, color);
    }

    public Materia(int id, String nombre, String color) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getCantidadPendientes() {
        return cantidadPendientes;
    }

    public void setCantidadPendientes(int cantidadPendientes) {
        this.cantidadPendientes = cantidadPendientes;
    }

    public int getCantidadNotas() {
        return cantidadNotas;
    }

    public void setCantidadNotas(int cantidadNotas) {
        this.cantidadNotas = cantidadNotas;
    }

    public int getMinutosEstudiados() {
        return minutosEstudiados;
    }

    public void setMinutosEstudiados(int minutosEstudiados) {
        this.minutosEstudiados = minutosEstudiados;
    }

    /** Devuelve el color de la materia listo para usar en la interfaz. */
    public int getColorInt() {
        try {
            return Color.parseColor(color);
        } catch (IllegalArgumentException | NullPointerException e) {
            return Color.parseColor("#2F5BEA");
        }
    }

    /** Primera letra del nombre, para el círculo de la lista de materias. */
    public String getInicial() {
        if (nombre == null || nombre.isEmpty()) {
            return "?";
        }
        return nombre.substring(0, 1).toUpperCase();
    }

    /** El Spinner usa este texto para mostrar la materia. */
    @Override
    public String toString() {
        return nombre;
    }
}
