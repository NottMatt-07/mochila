package com.epet30.mochila;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.epet30.mochila.modelo.Tarea;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla principal (Inicio): muestra las próximas tareas, TP y exámenes,
 * y el archivo con las tareas que ya se hicieron.
 */
public class MainActivity extends AppCompatActivity {

    /** Si viene en true, Inicio se abre en la pestaña Hechas (archivo). */
    public static final String EXTRA_VER_HECHAS = "ver_hechas";

    private static final int PEDIDO_NOTIFICACIONES = 1;

    private DatabaseHelper db;
    private TareaAdapter adapter;
    private MaterialButtonToggleGroup grupoVista;
    private FloatingActionButton fabAgregar;
    private TextView txtTitulo;
    private TextView txtResumen;
    private View grupoVacio;
    private ImageView imgVacio;
    private TextView txtVacioTitulo;
    private TextView txtVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        RecordatorioHelper.crearCanal(this);
        pedirPermisoDeNotificaciones();
        Navegacion.configurar(this, R.id.nav_inicio);

        TextView txtHoy = findViewById(R.id.txtHoy);
        txtHoy.setText(Formato.fechaLarga(System.currentTimeMillis()));
        txtTitulo = findViewById(R.id.txtTituloInicio);
        txtResumen = findViewById(R.id.txtResumen);
        grupoVacio = findViewById(R.id.grupoVacio);
        imgVacio = findViewById(R.id.imgVacio);
        txtVacioTitulo = findViewById(R.id.txtVacioTitulo);
        txtVacio = findViewById(R.id.txtVacio);
        fabAgregar = findViewById(R.id.fabAgregar);

        RecyclerView lista = findViewById(R.id.listaTareas);
        lista.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TareaAdapter(new ArrayList<>(), true, new TareaAdapter.Listener() {
            @Override
            public void onMarcarHecha(Tarea tarea, boolean hecha) {
                AccionesTarea.cambiarEstado(MainActivity.this, db, tarea, hecha,
                        fabAgregar, MainActivity.this::cargarTareas);
            }

            @Override
            public void onTocar(Tarea tarea) {
                AccionesTarea.abrirEditor(MainActivity.this, tarea);
            }

            @Override
            public void onMantenerPresionado(Tarea tarea) {
                AccionesTarea.confirmarEliminar(MainActivity.this, db, tarea,
                        MainActivity.this::cargarTareas);
            }
        });
        lista.setAdapter(adapter);

        grupoVista = findViewById(R.id.grupoVista);
        grupoVista.addOnButtonCheckedListener((grupo, botonId, marcado) -> {
            if (marcado) {
                cargarTareas();
            }
        });

        fabAgregar.setOnClickListener(v ->
                startActivity(new Intent(this, NuevaTareaActivity.class)));

        abrirPestaniaPedida(getIntent());
    }

    /** Se llama cuando se vuelve a Inicio desde otra pantalla (por ejemplo, desde Progreso). */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        abrirPestaniaPedida(intent);
    }

    private void abrirPestaniaPedida(Intent intent) {
        if (intent != null && intent.getBooleanExtra(EXTRA_VER_HECHAS, false)) {
            grupoVista.check(R.id.btnHechas);
            intent.removeExtra(EXTRA_VER_HECHAS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarTareas(); // se recarga al volver de otra pantalla
    }

    private boolean viendoHechas() {
        return grupoVista.getCheckedButtonId() == R.id.btnHechas;
    }

    private void cargarTareas() {
        if (viendoHechas()) {
            mostrarHechas();
        } else {
            mostrarPendientes();
        }
    }

    private void mostrarPendientes() {
        List<Tarea> pendientes = db.obtenerTareasPendientes();
        adapter.actualizar(pendientes);
        txtTitulo.setText("Lo que viene");
        fabAgregar.show();

        int paraHoy = 0;
        int vencidas = 0;
        for (Tarea tarea : pendientes) {
            int dias = tarea.getDiasRestantes();
            if (dias < 0) {
                vencidas++;
            } else if (dias == 0) {
                paraHoy++;
            }
        }
        if (pendientes.isEmpty()) {
            txtResumen.setText("Estás al día");
        } else {
            StringBuilder resumen = new StringBuilder();
            resumen.append(pendientes.size()).append(pendientes.size() == 1 ? " pendiente" : " pendientes");
            if (paraHoy > 0) {
                resumen.append(" · ").append(paraHoy).append(" para hoy");
            }
            if (vencidas > 0) {
                resumen.append(" · ").append(vencidas).append(vencidas == 1 ? " vencida" : " vencidas");
            }
            txtResumen.setText(resumen);
        }

        imgVacio.setImageResource(R.drawable.ic_logo);
        imgVacio.clearColorFilter();
        txtVacioTitulo.setText("¡Estás al día!");
        txtVacio.setText("No tenés nada pendiente.\nTocá + para agregar una tarea, un TP o un examen.");
        grupoVacio.setVisibility(pendientes.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarHechas() {
        List<Tarea> hechas = db.obtenerTareasHechas();
        adapter.actualizar(hechas);
        txtTitulo.setText("Archivo");
        fabAgregar.hide();

        long inicioSemana = Tarea.inicioDelDia(System.currentTimeMillis()) - 6L * 24 * 60 * 60 * 1000;
        int estaSemana = db.contarHechasDesde(inicioSemana);
        txtResumen.setText(hechas.size() + (hechas.size() == 1 ? " hecha en total" : " hechas en total")
                + " · " + estaSemana + " en los últimos 7 días");

        imgVacio.setImageResource(R.drawable.ic_archivo);
        imgVacio.setColorFilter(ContextCompat.getColor(this, R.color.gris));
        txtVacioTitulo.setText("Todavía no hay nada acá");
        txtVacio.setText("Cuando marques una tarea como hecha, queda guardada en este archivo.");
        grupoVacio.setVisibility(hechas.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void pedirPermisoDeNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, PEDIDO_NOTIFICACIONES);
        }
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
