package com.epet30.mochila;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.epet30.mochila.modelo.Materia;
import com.epet30.mochila.modelo.Nota;
import com.epet30.mochila.modelo.Tarea;

import java.util.ArrayList;
import java.util.List;

/**
 * Detalle de una materia: sus tareas y sus notas, separadas en dos pestañas.
 * Desde el menú de arriba se puede editar o eliminar la materia.
 */
public class MateriaDetalleActivity extends AppCompatActivity {

    public static final String EXTRA_MATERIA_ID = "materia_id";

    private DatabaseHelper db;
    private Materia materia;

    private RadioGroup rgVista;
    private RadioButton rbTareas;
    private RadioButton rbNotas;
    private RecyclerView lista;
    private TextView txtVacio;
    private Button btnAgregar;

    private TareaAdapter tareaAdapter;
    private NotaAdapter notaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_materia_detalle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);
        materia = db.obtenerMateria(getIntent().getIntExtra(EXTRA_MATERIA_ID, -1));
        if (materia == null) {
            Toast.makeText(this, "No se encontró la materia", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        aplicarColorDeLaMateria();

        rgVista = findViewById(R.id.rgVista);
        rbTareas = findViewById(R.id.rbTareas);
        rbNotas = findViewById(R.id.rbNotas);
        lista = findViewById(R.id.listaDetalle);
        txtVacio = findViewById(R.id.txtVacioDetalle);
        btnAgregar = findViewById(R.id.btnAgregarDetalle);

        lista.setLayoutManager(new LinearLayoutManager(this));

        tareaAdapter = new TareaAdapter(new ArrayList<>(), false, new TareaAdapter.Listener() {
            @Override
            public void onMarcarHecha(Tarea tarea, boolean hecha) {
                AccionesTarea.cambiarEstado(MateriaDetalleActivity.this, db, tarea, hecha,
                        btnAgregar, MateriaDetalleActivity.this::mostrar);
            }

            @Override
            public void onTocar(Tarea tarea) {
                AccionesTarea.abrirEditor(MateriaDetalleActivity.this, tarea);
            }

            @Override
            public void onMantenerPresionado(Tarea tarea) {
                AccionesTarea.confirmarEliminar(MateriaDetalleActivity.this, db, tarea,
                        MateriaDetalleActivity.this::mostrar);
            }
        });

        notaAdapter = new NotaAdapter(new ArrayList<>(), nota -> {
            Intent intent = new Intent(this, NotaActivity.class);
            intent.putExtra(NotaActivity.EXTRA_MATERIA_ID, materia.getId());
            intent.putExtra(NotaActivity.EXTRA_NOTA_ID, nota.getId());
            startActivity(intent);
        });

        rgVista.setOnCheckedChangeListener((grupo, idElegido) -> mostrar());

        btnAgregar.setOnClickListener(v -> {
            Intent intent;
            if (viendoTareas()) {
                intent = new Intent(this, NuevaTareaActivity.class);
                intent.putExtra(NuevaTareaActivity.EXTRA_MATERIA_ID, materia.getId());
            } else {
                intent = new Intent(this, NotaActivity.class);
                intent.putExtra(NotaActivity.EXTRA_MATERIA_ID, materia.getId());
            }
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (materia != null) {
            mostrar();
        }
    }

    /** La barra superior toma el nombre y el color de la materia. */
    private void aplicarColorDeLaMateria() {
        setTitle(materia.getNombre());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(materia.getColorInt()));
        }
        getWindow().setStatusBarColor(materia.getColorInt());
    }

    private boolean viendoTareas() {
        return rgVista.getCheckedRadioButtonId() == R.id.rbTareas;
    }

    /** Carga la pestaña elegida y actualiza los contadores. */
    private void mostrar() {
        List<Tarea> tareas = db.obtenerTareasDeMateria(materia.getId());
        List<Nota> notas = db.obtenerNotas(materia.getId());
        rbTareas.setText("Tareas (" + tareas.size() + ")");
        rbNotas.setText("Notas (" + notas.size() + ")");

        if (viendoTareas()) {
            tareaAdapter.actualizar(tareas);
            lista.setAdapter(tareaAdapter);
            btnAgregar.setText("Agregar tarea, TP o examen");
            txtVacio.setText("Todavía no hay tareas de " + materia.getNombre() + ".");
            txtVacio.setVisibility(tareas.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            notaAdapter.actualizar(notas);
            lista.setAdapter(notaAdapter);
            btnAgregar.setText("Agregar nota");
            txtVacio.setText("Todavía no hay notas.\nGuardá acá tus apuntes y resúmenes.");
            txtVacio.setVisibility(notas.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.materia_detalle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.accion_editar_materia) {
            MateriaDialogos.mostrarEditor(this, db, materia, () -> {
                aplicarColorDeLaMateria();
                mostrar();
            });
            return true;
        } else if (item.getItemId() == R.id.accion_eliminar_materia) {
            MateriaDialogos.confirmarEliminar(this, db, materia, this::finish);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
