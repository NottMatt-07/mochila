package com.epet30.mochila;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Maneja la barra de navegación inferior que comparten las 5 pantallas principales:
 * Inicio, Materias, Calendario, Pomodoro y Progreso.
 */
public final class Navegacion {

    private Navegacion() {
        // Clase de utilidades: no se crean objetos
    }

    /** Marca la pestaña actual y hace que las demás abran su pantalla. */
    public static BottomNavigationView configurar(Activity actividad, int itemActual) {
        BottomNavigationView barra = actividad.findViewById(R.id.barraNavegacion);
        barra.setSelectedItemId(itemActual);
        barra.setOnItemSelectedListener(item -> {
            if (item.getItemId() != itemActual) {
                ir(actividad, item.getItemId());
            }
            // false: la pestaña marcada no cambia en esta pantalla (cambia en la nueva)
            return false;
        });
        return barra;
    }

    /** Abre la pantalla de esa pestaña sin animación. */
    public static void ir(Activity actividad, int itemId) {
        Class<?> destino = pantallaDe(itemId);
        if (destino == null || destino == actividad.getClass()) {
            return;
        }
        Intent intent = new Intent(actividad, destino);
        if (destino == MainActivity.class) {
            // Inicio ya está abierto debajo: se vuelve a él en lugar de abrir otro
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        actividad.startActivity(intent);
        // Inicio queda siempre abierto debajo; las otras pestañas se cierran al cambiar,
        // así el botón Atrás siempre vuelve a Inicio
        if (!(actividad instanceof MainActivity)) {
            actividad.finish();
        }
        actividad.overridePendingTransition(0, 0);
    }

    private static Class<?> pantallaDe(int itemId) {
        if (itemId == R.id.nav_inicio) {
            return MainActivity.class;
        } else if (itemId == R.id.nav_materias) {
            return MateriasActivity.class;
        } else if (itemId == R.id.nav_calendario) {
            return CalendarioActivity.class;
        } else if (itemId == R.id.nav_pomodoro) {
            return PomodoroActivity.class;
        } else if (itemId == R.id.nav_progreso) {
            return ProgresoActivity.class;
        }
        return null;
    }
}
