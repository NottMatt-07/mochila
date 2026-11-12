package com.epet30.mochila;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Ayuda a mostrar fechas y horas en español.
 */
public final class Formato {

    private static final Locale ESPANOL = Locale.forLanguageTag("es-AR");

    private Formato() {
        // Clase de utilidades: no se crean objetos
    }

    /** Ejemplo: "Vie 6 de nov · 08:00" */
    public static String fechaYHora(long millis) {
        return capitalizar(new SimpleDateFormat("EEE d 'de' MMM · HH:mm", ESPANOL).format(new Date(millis)));
    }

    /** Ejemplo: "Jueves 5 de noviembre" */
    public static String fechaLarga(long millis) {
        return capitalizar(new SimpleDateFormat("EEEE d 'de' MMMM", ESPANOL).format(new Date(millis)));
    }

    /** Ejemplo: "06/11/2026" */
    public static String fechaCorta(long millis) {
        return new SimpleDateFormat("dd/MM/yyyy", ESPANOL).format(new Date(millis));
    }

    /** Ejemplo: "08:00" */
    public static String hora(long millis) {
        return new SimpleDateFormat("HH:mm", ESPANOL).format(new Date(millis));
    }

    /** Ejemplo: "5 nov" */
    public static String diaYMes(long millis) {
        return new SimpleDateFormat("d MMM", ESPANOL).format(new Date(millis)).replace(".", "");
    }

    /** Ejemplo: "Noviembre 2026" */
    public static String mesYAnio(long millis) {
        return capitalizar(new SimpleDateFormat("LLLL yyyy", ESPANOL).format(new Date(millis)));
    }

    /** Ejemplo: "Jue" (para los días del gráfico) */
    public static String diaCorto(long millis) {
        return capitalizar(new SimpleDateFormat("EEE", ESPANOL).format(new Date(millis)).replace(".", ""));
    }

    /** Ejemplo: 95 minutos → "1 h 35 min" */
    public static String minutos(int minutos) {
        if (minutos < 60) {
            return minutos + " min";
        }
        int horas = minutos / 60;
        int resto = minutos % 60;
        return resto == 0 ? horas + " h" : horas + " h " + resto + " min";
    }

    public static String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase(ESPANOL) + texto.substring(1);
    }
}
