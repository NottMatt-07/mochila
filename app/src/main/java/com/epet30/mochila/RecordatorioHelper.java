package com.epet30.mochila;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.epet30.mochila.modelo.Tarea;

/**
 * Programa y muestra las notificaciones de la app:
 * recordatorios de tareas y exámenes, y avisos del Pomodoro.
 */
public final class RecordatorioHelper {

    public static final String CANAL_ID = "recordatorios";
    public static final String EXTRA_ID = "id_notificacion";
    public static final String EXTRA_TITULO = "titulo";
    public static final String EXTRA_TEXTO = "texto";

    private static final long UNA_HORA_MS = 60L * 60 * 1000;
    private static final long UN_DIA_MS = 24 * UNA_HORA_MS;

    private RecordatorioHelper() {
        // Clase de utilidades: no se crean objetos
    }

    /** Crea el canal de notificaciones (obligatorio desde Android 8). */
    public static void crearCanal(Context context) {
        NotificationChannel canal = new NotificationChannel(
                CANAL_ID, "Recordatorios", NotificationManager.IMPORTANCE_HIGH);
        canal.setDescription("Avisos de entregas, exámenes y del Pomodoro");
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(canal);
        }
    }

    /** Programa dos avisos para la tarea: un día antes y una hora antes. */
    public static void programar(Context context, Tarea tarea) {
        if (!tarea.isRecordatorio()) {
            return;
        }
        String materia = tarea.getMateria().getNombre();
        programarAviso(context, tarea.getId() * 10 + 1, tarea.getFecha() - UN_DIA_MS,
                "Mañana: " + tarea.getTipo() + " de " + materia,
                tarea.getTitulo() + " · " + Formato.fechaYHora(tarea.getFecha()));
        programarAviso(context, tarea.getId() * 10 + 2, tarea.getFecha() - UNA_HORA_MS,
                "En una hora: " + tarea.getTipo() + " de " + materia,
                tarea.getTitulo());
    }

    /** Cancela los avisos pendientes de una tarea (por ejemplo, si ya se hizo). */
    public static void cancelar(Context context, int tareaId) {
        AlarmManager alarmas = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmas == null) {
            return;
        }
        for (int numero = 1; numero <= 2; numero++) {
            Intent intent = new Intent(context, RecordatorioReceiver.class);
            PendingIntent pendiente = PendingIntent.getBroadcast(context, tareaId * 10 + numero,
                    intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (pendiente != null) {
                alarmas.cancel(pendiente);
                pendiente.cancel();
            }
        }
    }

    private static void programarAviso(Context context, int codigo, long cuando,
                                       String titulo, String texto) {
        if (cuando <= System.currentTimeMillis()) {
            return; // ese momento ya pasó
        }
        AlarmManager alarmas = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmas == null) {
            return;
        }
        Intent intent = new Intent(context, RecordatorioReceiver.class);
        intent.putExtra(EXTRA_ID, codigo);
        intent.putExtra(EXTRA_TITULO, titulo);
        intent.putExtra(EXTRA_TEXTO, texto);
        PendingIntent pendiente = PendingIntent.getBroadcast(context, codigo, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // Alarma no exacta: no necesita permisos especiales y funciona con el celular en reposo
        alarmas.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cuando, pendiente);
    }

    /** Muestra una notificación ahora mismo. */
    public static void mostrar(Context context, int id, String titulo, String texto) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return; // el usuario no dio permiso para notificaciones
        }
        crearCanal(context);

        Intent abrirApp = new Intent(context, MainActivity.class);
        abrirApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent alTocar = PendingIntent.getActivity(context, 0, abrirApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setColor(ContextCompat.getColor(context, R.color.azul))
                .setContentTitle(titulo)
                .setContentText(texto)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(alTocar);

        NotificationManagerCompat.from(context).notify(id, builder.build());
    }
}
