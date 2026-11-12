package com.epet30.mochila;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.epet30.mochila.modelo.Nota;

/**
 * Crea una nota nueva o edita una existente dentro de una materia.
 */
public class NotaActivity extends AppCompatActivity {

    public static final String EXTRA_MATERIA_ID = "materia_id";
    public static final String EXTRA_NOTA_ID = "nota_id";

    private DatabaseHelper db;
    private EditText edtTitulo;
    private EditText edtContenido;
    private int materiaId;
    private Nota nota; // null si es una nota nueva

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nota);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);
        edtTitulo = findViewById(R.id.edtTituloNota);
        edtContenido = findViewById(R.id.edtContenidoNota);
        View btnEliminar = findViewById(R.id.btnEliminarNota);

        materiaId = getIntent().getIntExtra(EXTRA_MATERIA_ID, -1);
        int notaId = getIntent().getIntExtra(EXTRA_NOTA_ID, -1);
        if (notaId != -1) {
            nota = db.obtenerNota(notaId);
        }

        if (nota != null) {
            setTitle("Editar nota");
            edtTitulo.setText(nota.getTitulo());
            edtContenido.setText(nota.getContenido());
            btnEliminar.setVisibility(View.VISIBLE);
            btnEliminar.setOnClickListener(v -> confirmarEliminar());
        } else {
            setTitle("Nueva nota");
        }

        findViewById(R.id.btnGuardarNota).setOnClickListener(v -> guardar());
    }

    private void guardar() {
        String titulo = edtTitulo.getText().toString().trim();
        String contenido = edtContenido.getText().toString().trim();
        if (titulo.isEmpty()) {
            edtTitulo.setError("Escribí un título");
            edtTitulo.requestFocus();
            return;
        }

        long ahora = System.currentTimeMillis();
        if (nota == null) {
            db.agregarNota(new Nota(materiaId, titulo, contenido, ahora));
        } else {
            nota.setTitulo(titulo);
            nota.setContenido(contenido);
            nota.setFechaEdicion(ahora);
            db.actualizarNota(nota);
        }
        Toast.makeText(this, "Nota guardada", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar nota")
                .setMessage("¿Querés eliminar esta nota?")
                .setPositiveButton("Eliminar", (dialogo, boton) -> {
                    db.eliminarNota(nota.getId());
                    Toast.makeText(this, "Nota eliminada", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
