package com.epet30.mochila;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.epet30.mochila.modelo.Tarea;
import com.google.android.material.snackbar.Snackbar;

/**
 * Acciones sobre una tarea que se usan en varias pantallas
 * (Inicio, Detalle de materia y Calendario).
 */
public final class AccionesTarea {

    private AccionesTarea() {
        // Clase de utilidades: no se crean objetos
    }

    /** Abre el formulario para editar la tarea. */
    public static void abrirEditor(Activity actividad, Tarea tarea) {
        Intent intent = new Intent(actividad, NuevaTareaActivity.class);
        intent.putExtra(NuevaTareaActivity.EXTRA_TAREA_ID, tarea.getId());
        actividad.startActivity(intent);
    }

    /**
     * Marca la tarea como hecha (queda guardada en el archivo de Hechas)
     * o la devuelve a pendientes. Al marcarla, ofrece "Deshacer".
     *
     * @param ancla vista sobre la que aparece el aviso (puede ser null)
     */
    public static void cambiarEstado(Activity actividad, DatabaseHelper db, Tarea tarea,
                                     boolean hecha, View ancla, Runnable recargar) {
        db.marcarHecha(tarea.getId(), hecha);
        if (hecha) {
            RecordatorioHelper.cancelar(actividad, tarea.getId());
            Snackbar aviso = Snackbar.make(actividad.findViewById(android.R.id.content),
                    "¡Bien! \"" + tarea.getTitulo() + "\" pasó a Hechas", Snackbar.LENGTH_LONG);
            aviso.setAction("Deshacer", v -> {
                db.marcarHecha(tarea.getId(), false);
                RecordatorioHelper.programar(actividad, tarea);
                recargar.run();
            });
            if (ancla != null && ancla.getVisibility() == View.VISIBLE) {
                aviso.setAnchorView(ancla);
            }
            aviso.show();
        } else {
            RecordatorioHelper.programar(actividad, tarea);
            Toast.makeText(actividad, "Volvió a pendientes", Toast.LENGTH_SHORT).show();
        }
        recargar.run();
    }

    /** Pide confirmación y borra la tarea para siempre. */
    public static void confirmarEliminar(Activity actividad, DatabaseHelper db, Tarea tarea,
                                         Runnable recargar) {
        new AlertDialog.Builder(actividad)
                .setTitle("Eliminar")
                .setMessage("¿Querés eliminar \"" + tarea.getTitulo() + "\"? No se puede deshacer.")
                .setPositiveButton("Eliminar", (dialogo, boton) -> {
                    RecordatorioHelper.cancelar(actividad, tarea.getId());
                    db.eliminarTarea(tarea.getId());
                    recargar.run();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
