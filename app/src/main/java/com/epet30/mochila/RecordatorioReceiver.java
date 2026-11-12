package com.epet30.mochila;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Se ejecuta cuando llega la hora de un recordatorio programado con AlarmManager
 * y muestra la notificación, aunque la app esté cerrada.
 */
public class RecordatorioReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(RecordatorioHelper.EXTRA_ID, 0);
        String titulo = intent.getStringExtra(RecordatorioHelper.EXTRA_TITULO);
        String texto = intent.getStringExtra(RecordatorioHelper.EXTRA_TEXTO);
        if (titulo == null) {
            titulo = "Mochila";
        }
        if (texto == null) {
            texto = "Tenés una entrega próxima";
        }
        RecordatorioHelper.mostrar(context, id, titulo, texto);
    }
}
