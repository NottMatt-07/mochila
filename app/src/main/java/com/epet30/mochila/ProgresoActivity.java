package com.epet30.mochila;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.epet30.mochila.modelo.Materia;
import com.epet30.mochila.modelo.Tarea;

import java.util.List;

/**
 * Estadísticas de los últimos 7 días: tareas hechas, minutos de estudio con Pomodoro,
 * racha de días seguidos, estudio por materia y lo que queda pendiente.
 */
public class ProgresoActivity extends AppCompatActivity {

    private static final int DIAS = 7;
    private static final long UN_DIA_MS = 24L * 60 * 60 * 1000;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progreso);

        db = new DatabaseHelper(this);
        Navegacion.configurar(this, R.id.nav_progreso);

        findViewById(R.id.btnVerArchivo).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_VER_HECHAS, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mostrarNumeros();
        mostrarGrafico();
        mostrarPorMateria();
        mostrarPendientes();
    }

    private long inicioDeLaSemana() {
        return Tarea.inicioDelDia(System.currentTimeMillis() - (DIAS - 1) * UN_DIA_MS);
    }

    private void mostrarNumeros() {
        TextView txtHechas = findViewById(R.id.txtNumHechas);
        TextView txtMinutos = findViewById(R.id.txtNumMinutos);
        TextView txtRacha = findViewById(R.id.txtNumRacha);
        TextView etiquetaHechas = findViewById(R.id.txtEtiquetaHechas);
        TextView etiquetaRacha = findViewById(R.id.txtEtiquetaRacha);

        int hechas = db.contarHechasDesde(inicioDeLaSemana());
        txtHechas.setText(String.valueOf(hechas));
        etiquetaHechas.setText(hechas == 1 ? "tarea hecha" : "tareas hechas");

        int minutos = db.resumenDesde(inicioDeLaSemana())[1];
        txtMinutos.setText(Formato.minutos(minutos));

        int racha = db.rachaDeEstudio();
        txtRacha.setText(String.valueOf(racha));
        etiquetaRacha.setText(racha == 1 ? "día de racha" : "días de racha");
    }

    private void mostrarGrafico() {
        int[] minutos = db.minutosPorDia(DIAS);
        String[] etiquetas = new String[DIAS];
        long hoy = Tarea.inicioDelDia(System.currentTimeMillis());
        for (int i = 0; i < DIAS; i++) {
            long dia = hoy - (DIAS - 1 - i) * UN_DIA_MS;
            etiquetas[i] = i == DIAS - 1 ? "Hoy" : Formato.diaCorto(dia);
        }
        GraficoBarras grafico = findViewById(R.id.graficoDias);
        grafico.setDatos(minutos, etiquetas, DIAS - 1);
    }

    /** Una fila por materia: nombre, minutos y una barra con su color. */
    private void mostrarPorMateria() {
        LinearLayout contenedor = findViewById(R.id.listaPorMateria);
        contenedor.removeAllViews();
        List<Materia> materias = db.minutosPorMateria(inicioDeLaSemana());

        int maximo = 1;
        for (Materia materia : materias) {
            maximo = Math.max(maximo, materia.getMinutosEstudiados());
        }

        int margen = Math.round(10 * getResources().getDisplayMetrics().density);
        for (Materia materia : materias) {
            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setPadding(0, margen, 0, 0);

            TextView nombre = new TextView(this);
            nombre.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            nombre.setText(materia.getNombre());
            nombre.setTextColor(ContextCompat.getColor(this, R.color.tinta));
            nombre.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

            TextView minutos = new TextView(this);
            minutos.setText(Formato.minutos(materia.getMinutosEstudiados()));
            minutos.setTextColor(ContextCompat.getColor(this, R.color.gris));
            minutos.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            fila.addView(nombre);
            fila.addView(minutos);
            contenedor.addView(fila);

            ProgressBar barra = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            barra.setMax(maximo);
            barra.setProgress(Math.max(materia.getMinutosEstudiados(), maximo / 50));
            barra.setProgressTintList(ColorStateList.valueOf(materia.getColorInt()));
            barra.setProgressBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.gris_claro)));
            barra.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            contenedor.addView(barra);
        }

        findViewById(R.id.txtSinEstudio).setVisibility(materias.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarPendientes() {
        TextView txtProximas = findViewById(R.id.txtProximas);
        TextView txtVencidas = findViewById(R.id.txtVencidas);
        int proximas = db.contarProximas(DIAS);
        int vencidas = db.contarVencidas();
        txtProximas.setText(proximas == 0 ? "Nada para los próximos 7 días"
                : proximas + (proximas == 1 ? " entrega o examen" : " entregas o exámenes")
                + " en los próximos 7 días");
        txtVencidas.setText(vencidas == 0 ? "Ninguna tarea vencida"
                : vencidas + (vencidas == 1 ? " tarea vencida sin marcar" : " tareas vencidas sin marcar"));
        txtVencidas.setTextColor(ContextCompat.getColor(this, vencidas == 0 ? R.color.gris : R.color.rojo));
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
