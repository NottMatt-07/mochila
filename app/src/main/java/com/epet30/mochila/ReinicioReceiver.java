package com.epet30.mochila;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.epet30.mochila.modelo.Tarea;

/**
 * Android borra las alarmas programadas cuando el celular se apaga o se reinicia.
 * Este receptor se ejecuta al encender el celular (o al actualizar la app)
 * y vuelve a programar los recordatorios de todas las tareas pendientes.
 */
public class ReinicioReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String accion = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(accion)
                && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(accion)) {
            return;
        }
        DatabaseHelper db = new DatabaseHelper(context);
        try {
            for (Tarea tarea : db.obtenerTareasConRecordatorio()) {
                RecordatorioHelper.programar(context, tarea);
            }
        } finally {
            db.close();
        }
    }
}
