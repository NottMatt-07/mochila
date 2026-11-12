package com.epet30.mochila;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.epet30.mochila.modelo.Tarea;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calendario mensual: marca con puntos de colores los días que tienen tareas,
 * TP o exámenes, y abajo muestra lo que hay en el día elegido.
 */
public class CalendarioActivity extends AppCompatActivity {

    private static final String[] INICIALES = {"L", "M", "M", "J", "V", "S", "D"};

    private DatabaseHelper db;
    private DiaAdapter diaAdapter;
    private TareaAdapter tareaAdapter;
    private TextView txtMes;
    private TextView txtDiaElegido;
    private TextView txtVacioDia;

    /** Primer día del mes que se está viendo. */
    private final Calendar mesVisible = Calendar.getInstance();
    private long diaElegido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario);

        db = new DatabaseHelper(this);
        Navegacion.configurar(this, R.id.nav_calendario);
        txtMes = findViewById(R.id.txtMes);
        txtDiaElegido = findViewById(R.id.txtDiaElegido);
        txtVacioDia = findViewById(R.id.txtVacioDia);
        crearFilaDiasSemana();

        RecyclerView grid = findViewById(R.id.gridDias);
        grid.setLayoutManager(new GridLayoutManager(this, 7));
        diaAdapter = new DiaAdapter(this::elegirDia);
        grid.setAdapter(diaAdapter);

        RecyclerView listaDelDia = findViewById(R.id.listaDelDia);
        listaDelDia.setLayoutManager(new LinearLayoutManager(this));
        tareaAdapter = new TareaAdapter(new ArrayList<>(), true, new TareaAdapter.Listener() {
            @Override
            public void onMarcarHecha(Tarea tarea, boolean hecha) {
                AccionesTarea.cambiarEstado(CalendarioActivity.this, db, tarea, hecha,
                        findViewById(R.id.fabAgregarEnDia), CalendarioActivity.this::cargarMes);
            }

            @Override
            public void onTocar(Tarea tarea) {
                AccionesTarea.abrirEditor(CalendarioActivity.this, tarea);
            }

            @Override
            public void onMantenerPresionado(Tarea tarea) {
                AccionesTarea.confirmarEliminar(CalendarioActivity.this, db, tarea,
                        CalendarioActivity.this::cargarMes);
            }
        });
        listaDelDia.setAdapter(tareaAdapter);

        diaElegido = Tarea.inicioDelDia(System.currentTimeMillis());
        irAlPrimerDiaDelMes(diaElegido);

        findViewById(R.id.btnMesAnterior).setOnClickListener(v -> cambiarMes(-1));
        findViewById(R.id.btnMesSiguiente).setOnClickListener(v -> cambiarMes(1));
        // Tocar el nombre del mes vuelve a hoy
        txtMes.setOnClickListener(v -> {
            diaElegido = Tarea.inicioDelDia(System.currentTimeMillis());
            irAlPrimerDiaDelMes(diaElegido);
            cargarMes();
        });

        findViewById(R.id.fabAgregarEnDia).setOnClickListener(v -> {
            Intent intent = new Intent(this, NuevaTareaActivity.class);
            intent.putExtra(NuevaTareaActivity.EXTRA_FECHA, diaElegido);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMes();
    }

    private void crearFilaDiasSemana() {
        LinearLayout fila = findViewById(R.id.filaDiasSemana);
        for (String inicial : INICIALES) {
            TextView texto = new TextView(this);
            texto.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            texto.setGravity(Gravity.CENTER);
            texto.setText(inicial);
            texto.setTextColor(ContextCompat.getColor(this, R.color.gris));
            texto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            texto.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            fila.addView(texto);
        }
    }

    private void irAlPrimerDiaDelMes(long dia) {
        mesVisible.setTimeInMillis(dia);
        mesVisible.set(Calendar.DAY_OF_MONTH, 1);
    }

    private void cambiarMes(int cantidad) {
        mesVisible.add(Calendar.MONTH, cantidad);
        // Se elige el día 1 del mes nuevo, o hoy si es el mes actual
        long hoy = Tarea.inicioDelDia(System.currentTimeMillis());
        Calendar calendarioHoy = Calendar.getInstance();
        calendarioHoy.setTimeInMillis(hoy);
        boolean esElMesActual = calendarioHoy.get(Calendar.YEAR) == mesVisible.get(Calendar.YEAR)
                && calendarioHoy.get(Calendar.MONTH) == mesVisible.get(Calendar.MONTH);
        diaElegido = esElMesActual ? hoy : Tarea.inicioDelDia(mesVisible.getTimeInMillis());
        cargarMes();
    }

    /** Arma la grilla del mes visible con los puntos de cada día. */
    private void cargarMes() {
        long inicioMes = Tarea.inicioDelDia(mesVisible.getTimeInMillis());
        Calendar cursor = Calendar.getInstance();
        cursor.setTimeInMillis(inicioMes);

        // Lugares vacíos antes del día 1 (la semana empieza el lunes)
        int vacios = (cursor.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7;
        List<Long> dias = new ArrayList<>();
        for (int i = 0; i < vacios; i++) {
            dias.add(0L);
        }
        int diasDelMes = cursor.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < diasDelMes; i++) {
            dias.add(Tarea.inicioDelDia(cursor.getTimeInMillis()));
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
        long finMes = Tarea.inicioDelDia(cursor.getTimeInMillis());

        // Colores de las materias con algo en cada día
        Map<Long, List<Integer>> colores = new HashMap<>();
        for (Tarea tarea : db.obtenerTareasEntre(inicioMes, finMes)) {
            long dia = Tarea.inicioDelDia(tarea.getFecha());
            List<Integer> lista = colores.get(dia);
            if (lista == null) {
                lista = new ArrayList<>();
                colores.put(dia, lista);
            }
            lista.add(tarea.getMateria().getColorInt());
        }

        txtMes.setText(Formato.mesYAnio(inicioMes));
        diaAdapter.actualizar(dias, colores, Tarea.inicioDelDia(System.currentTimeMillis()), diaElegido);
        mostrarDia();
    }

    private void elegirDia(long dia) {
        diaElegido = dia;
        diaAdapter.elegir(dia);
        mostrarDia();
    }

    /** Muestra abajo las tareas del día elegido. */
    private void mostrarDia() {
        Calendar siguiente = Calendar.getInstance();
        siguiente.setTimeInMillis(diaElegido);
        siguiente.add(Calendar.DAY_OF_MONTH, 1);
        List<Tarea> tareas = db.obtenerTareasEntre(diaElegido, siguiente.getTimeInMillis());
        tareaAdapter.actualizar(tareas);

        String titulo = Formato.fechaLarga(diaElegido);
        int diasDesdeHoy = Tarea.diasEntre(System.currentTimeMillis(), diaElegido);
        if (diasDesdeHoy == 0) {
            titulo = "Hoy · " + titulo;
        } else if (diasDesdeHoy == 1) {
            titulo = "Mañana · " + titulo;
        }
        txtDiaElegido.setText(titulo);
        txtVacioDia.setVisibility(tareas.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
