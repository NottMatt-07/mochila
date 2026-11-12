package com.epet30.mochila;

import android.os.CountDownTimer;

import java.util.Locale;

/**
 * Temporizador del método Pomodoro que se puede iniciar, pausar y reiniciar.
 * Avisa a la pantalla en cada segundo y cuando termina el bloque.
 */
public class PomodoroTimer {

    /** La pantalla que usa el temporizador implementa esta interfaz. */
    public interface Listener {
        void onTick(long restanteMillis);

        void onFinish();
    }

    private final Listener listener;
    private long duracion;
    private long restante;
    private boolean corriendo;
    private CountDownTimer contador;

    public PomodoroTimer(long duracion, Listener listener) {
        this.duracion = duracion;
        this.restante = duracion;
        this.listener = listener;
    }

    public void iniciar() {
        if (corriendo || restante <= 0) {
            return;
        }
        corriendo = true;
        contador = new CountDownTimer(restante, 250) {
            @Override
            public void onTick(long millisHastaTerminar) {
                restante = millisHastaTerminar;
                listener.onTick(restante);
            }

            @Override
            public void onFinish() {
                restante = 0;
                corriendo = false;
                listener.onTick(0);
                listener.onFinish();
            }
        }.start();
    }

    public void pausar() {
        if (contador != null) {
            contador.cancel();
        }
        corriendo = false;
    }

    public void reiniciar(long nuevaDuracion) {
        pausar();
        duracion = nuevaDuracion;
        restante = nuevaDuracion;
        listener.onTick(restante);
    }

    public long getDuracion() {
        return duracion;
    }

    public long getRestante() {
        return restante;
    }

    public boolean isCorriendo() {
        return corriendo;
    }

    /** Progreso restante entre 0 y max, para la barra circular. */
    public int getProgreso(int max) {
        if (duracion <= 0) {
            return 0;
        }
        return (int) (restante * max / duracion);
    }

    /** Convierte milisegundos al formato mm:ss. */
    public static String formatear(long millis) {
        long segundos = (millis + 999) / 1000;
        return String.format(Locale.US, "%02d:%02d", segundos / 60, segundos % 60);
    }
}
