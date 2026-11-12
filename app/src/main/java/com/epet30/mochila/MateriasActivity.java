package com.epet30.mochila;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.epet30.mochila.modelo.Materia;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista de materias. Desde acá se agregan materias nuevas, se editan o eliminan
 * (manteniendo presionada una) y se entra a cada una.
 */
public class MateriasActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private MateriaAdapter adapter;
    private TextView txtSubtitulo;
    private TextView txtVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_materias);

        db = new DatabaseHelper(this);
        Navegacion.configurar(this, R.id.nav_materias);
        txtSubtitulo = findViewById(R.id.txtSubtituloMaterias);
        txtVacio = findViewById(R.id.txtVacioMaterias);

        RecyclerView lista = findViewById(R.id.listaMaterias);
        lista.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MateriaAdapter(new ArrayList<>(), new MateriaAdapter.Listener() {
            @Override
            public void onMateriaClick(Materia materia) {
                Intent intent = new Intent(MateriasActivity.this, MateriaDetalleActivity.class);
                intent.putExtra(MateriaDetalleActivity.EXTRA_MATERIA_ID, materia.getId());
                startActivity(intent);
            }

            @Override
            public void onMateriaMantenida(Materia materia) {
                mostrarOpciones(materia);
            }
        });
        lista.setAdapter(adapter);

        findViewById(R.id.fabAgregarMateria).setOnClickListener(v ->
                MateriaDialogos.mostrarEditor(this, db, null, this::cargarMaterias));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMaterias();
    }

    private void cargarMaterias() {
        List<Materia> materias = db.obtenerMaterias();
        int pendientes = 0;
        for (Materia materia : materias) {
            materia.setCantidadPendientes(db.contarPendientes(materia.getId()));
            materia.setCantidadNotas(db.contarNotas(materia.getId()));
            pendientes += materia.getCantidadPendientes();
        }
        adapter.actualizar(materias);
        txtSubtitulo.setText(materias.size() + (materias.size() == 1 ? " materia" : " materias")
                + " · " + pendientes + (pendientes == 1 ? " pendiente" : " pendientes"));
        txtVacio.setVisibility(materias.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Al mantener presionada una materia: editar o eliminar. */
    private void mostrarOpciones(Materia materia) {
        new AlertDialog.Builder(this)
                .setTitle(materia.getNombre())
                .setItems(new String[]{"Editar", "Eliminar"}, (dialogo, opcion) -> {
                    if (opcion == 0) {
                        MateriaDialogos.mostrarEditor(this, db, materia, this::cargarMaterias);
                    } else {
                        MateriaDialogos.confirmarEliminar(this, db, materia, this::cargarMaterias);
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
