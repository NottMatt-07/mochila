package com.epet30.mochila;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.epet30.mochila.modelo.Materia;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

/**
 * Temporizador Pomodoro: 25 minutos de estudio y 5 de descanso.
 * Al terminar cada bloque suena un aviso, llega una notificación
 * y el bloque de estudio se guarda en la base de datos.
 */
public class PomodoroActivity extends AppCompatActivity {

    private static final long ESTUDIO_MS = 25L * 60 * 1000;
    private static final long DESCANSO_MS = 5L * 60 * 1000;
    private static final long DEMO_ESTUDIO_MS = 10L * 1000;
    private static final long DEMO_DESCANSO_MS = 5L * 1000;
    private static final int NOTIFICACION_POMODORO = 9999;
    private static final int PROGRESO_MAXIMO = 1000;

    private DatabaseHelper db;
    private PomodoroTimer timer;
    private boolean enEstudio = true;

    private TextView txtModo;
    private TextView txtTiempo;
    private TextView txtResumenHoy;
    private CircularProgressIndicator progreso;
    private Button btnIniciar;
    private Spinner spMateria;
    private SwitchCompat swDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        db = new DatabaseHelper(this);
        configurarNavegacion();
        txtModo = findViewById(R.id.txtModo);
        txtTiempo = findViewById(R.id.txtTiempo);
        txtResumenHoy = findViewById(R.id.txtResumenHoy);
        progreso = findViewById(R.id.progresoPomodoro);
        btnIniciar = findViewById(R.id.btnIniciar);
        spMateria = findViewById(R.id.spMateriaPomodoro);
        swDemo = findViewById(R.id.swDemo);

        List<Materia> materias = db.obtenerMaterias();
        ArrayAdapter<Materia> adaptador = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, materias);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMateria.setAdapter(adaptador);

        timer = new PomodoroTimer(duracionActual(), new PomodoroTimer.Listener() {
            @Override
            public void onTick(long restanteMillis) {
                actualizarReloj();
            }

            @Override
            public void onFinish() {
                terminarBloque();
            }
        });

        btnIniciar.setOnClickListener(v -> {
            if (timer.isCorriendo()) {
                timer.pausar();
            } else {
                timer.iniciar();
            }
            actualizarBoton();
        });

        findViewById(R.id.btnReiniciar).setOnClickListener(v -> {
            timer.reiniciar(duracionActual());
            actualizarBoton();
        });

        swDemo.setOnCheckedChangeListener((boton, activado) -> {
            timer.reiniciar(duracionActual());
            actualizarBoton();
        });

        // La pantalla no se apaga mientras se usa el temporizador
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        actualizarReloj();
        actualizarBoton();
        actualizarResumenDeHoy();
    }

    private long duracionActual() {
        if (swDemo != null && swDemo.isChecked()) {
            return enEstudio ? DEMO_ESTUDIO_MS : DEMO_DESCANSO_MS;
        }
        return enEstudio ? ESTUDIO_MS : DESCANSO_MS;
    }

    private void actualizarReloj() {
        txtTiempo.setText(PomodoroTimer.formatear(timer.getRestante()));
        progreso.setProgress(timer.getProgreso(PROGRESO_MAXIMO));
        txtModo.setText(enEstudio ? "Estudio" : "Descanso");
        int color = ContextCompat.getColor(this, enEstudio ? R.color.azul : R.color.ambar);
        progreso.setIndicatorColor(color);
    }

    private void actualizarBoton() {
        btnIniciar.setText(timer.isCorriendo() ? "Pausar" : "Iniciar");
    }

    private void actualizarResumenDeHoy() {
        int[] resumen = db.resumenDeHoy();
        String bloques = resumen[0] == 1 ? " bloque" : " bloques";
        txtResumenHoy.setText("Hoy: " + resumen[0] + bloques + " · " + resumen[1] + " minutos de estudio");
    }

    /** Se llama cuando el temporizador llega a cero. */
    private void terminarBloque() {
        reproducirSonido();
        if (enEstudio) {
            Materia materia = (Materia) spMateria.getSelectedItem();
            Integer materiaId = materia != null ? materia.getId() : null;
            int minutos = (int) (timer.getDuracion() / 60000);
            db.agregarSesion(materiaId, minutos);
            RecordatorioHelper.mostrar(this, NOTIFICACION_POMODORO,
                    "¡Bloque terminado!", "Muy bien. Tomate un descanso.");
            enEstudio = false;
        } else {
            RecordatorioHelper.mostrar(this, NOTIFICACION_POMODORO,
                    "Terminó el descanso", "Cuando quieras, empezá otro bloque de estudio.");
            enEstudio = true;
        }
        timer.reiniciar(duracionActual());
        actualizarBoton();
        actualizarResumenDeHoy();
    }

    private void reproducirSonido() {
        Uri sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), sonido);
        if (ringtone != null) {
            ringtone.play();
        }
    }

    /**
     * Barra inferior: si el temporizador está corriendo, avisa antes de cambiar de pantalla,
     * porque el Pomodoro solo funciona con esta pantalla abierta.
     */
    private void configurarNavegacion() {
        BottomNavigationView barra = Navegacion.configurar(this, R.id.nav_pomodoro);
        barra.setOnItemSelectedListener(item -> {
            int destino = item.getItemId();
            if (destino == R.id.nav_pomodoro) {
                return false;
            }
            if (timer != null && timer.isCorriendo()) {
                new AlertDialog.Builder(this)
                        .setTitle("¿Salir del Pomodoro?")
                        .setMessage("El temporizador se va a detener.")
                        .setPositiveButton("Salir", (d, b) -> Navegacion.ir(this, destino))
                        .setNegativeButton("Seguir estudiando", null)
                        .show();
            } else {
                Navegacion.ir(this, destino);
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.pausar();
        }
        db.close();
        super.onDestroy();
    }
}
