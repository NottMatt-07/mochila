package com.epet30.mochila;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.epet30.mochila.modelo.Materia;
import com.epet30.mochila.modelo.Tarea;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Formulario para cargar una tarea, un TP o un examen, o para editar una que ya existe.
 */
public class NuevaTareaActivity extends AppCompatActivity {

    public static final String EXTRA_MATERIA_ID = "materia_id";
    public static final String EXTRA_TAREA_ID = "tarea_id";
    /** Día elegido en el Calendario (en milisegundos), para cargar la tarea en esa fecha. */
    public static final String EXTRA_FECHA = "fecha";

    private DatabaseHelper db;
    private EditText edtTitulo;
    private EditText edtDescripcion;
    private Spinner spMateria;
    private RadioGroup rgTipo;
    private Button btnFecha;
    private Button btnHora;
    private SwitchCompat swRecordatorio;

    private final Calendar fechaElegida = Calendar.getInstance();
    private List<Materia> materias;
    private Tarea tareaEditada; // null si es una tarea nueva

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_tarea);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);
        edtTitulo = findViewById(R.id.edtTitulo);
        edtDescripcion = findViewById(R.id.edtDescripcion);
        spMateria = findViewById(R.id.spMateria);
        rgTipo = findViewById(R.id.rgTipo);
        btnFecha = findViewById(R.id.btnFecha);
        btnHora = findViewById(R.id.btnHora);
        swRecordatorio = findViewById(R.id.swRecordatorio);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        if (!cargarMaterias()) {
            return;
        }

        int tareaId = getIntent().getIntExtra(EXTRA_TAREA_ID, -1);
        if (tareaId != -1) {
            tareaEditada = db.obtenerTarea(tareaId);
        }

        if (tareaEditada != null) {
            String tipo = tareaEditada.getTipo();
            setTitle("Editar " + (Tarea.TIPO_TP.equals(tipo) ? tipo : tipo.toLowerCase(Locale.ROOT)));
            btnGuardar.setText("Guardar cambios");
            cargarDatosDeLaTarea();
        } else {
            setTitle("Nueva tarea");
            // Por defecto: mañana (o el día elegido en el Calendario) a las 8:00
            long fechaDelCalendario = getIntent().getLongExtra(EXTRA_FECHA, -1);
            if (fechaDelCalendario > 0) {
                fechaElegida.setTimeInMillis(fechaDelCalendario);
            } else {
                fechaElegida.add(Calendar.DAY_OF_MONTH, 1);
            }
            fechaElegida.set(Calendar.HOUR_OF_DAY, 8);
            fechaElegida.set(Calendar.MINUTE, 0);
        }
        fechaElegida.set(Calendar.SECOND, 0);
        fechaElegida.set(Calendar.MILLISECOND, 0);
        actualizarBotonesDeFecha();

        btnFecha.setOnClickListener(v -> elegirFecha());
        btnHora.setOnClickListener(v -> elegirHora());
        btnGuardar.setOnClickListener(v -> guardar());
    }

    /** Llena el Spinner con las materias guardadas. Devuelve false si no hay ninguna. */
    private boolean cargarMaterias() {
        materias = db.obtenerMaterias();
        if (materias.isEmpty()) {
            Toast.makeText(this, "Primero agregá una materia", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MateriasActivity.class));
            finish();
            return false;
        }

        ArrayAdapter<Materia> adaptador = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, materias);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMateria.setAdapter(adaptador);

        // Si se abrió desde una materia, queda elegida esa materia
        elegirMateria(getIntent().getIntExtra(EXTRA_MATERIA_ID, -1));
        return true;
    }

    private void elegirMateria(int materiaId) {
        for (int i = 0; i < materias.size(); i++) {
            if (materias.get(i).getId() == materiaId) {
                spMateria.setSelection(i);
                return;
            }
        }
    }

    /** Modo edición: completa el formulario con los datos guardados. */
    private void cargarDatosDeLaTarea() {
        edtTitulo.setText(tareaEditada.getTitulo());
        edtDescripcion.setText(tareaEditada.getDescripcion());
        elegirMateria(tareaEditada.getMateria().getId());
        if (Tarea.TIPO_TP.equals(tareaEditada.getTipo())) {
            rgTipo.check(R.id.rbTp);
        } else if (Tarea.TIPO_EXAMEN.equals(tareaEditada.getTipo())) {
            rgTipo.check(R.id.rbExamen);
        } else {
            rgTipo.check(R.id.rbTarea);
        }
        swRecordatorio.setChecked(tareaEditada.isRecordatorio());
        fechaElegida.setTimeInMillis(tareaEditada.getFecha());
    }

    private void elegirFecha() {
        new DatePickerDialog(this, (vista, anio, mes, dia) -> {
            fechaElegida.set(Calendar.YEAR, anio);
            fechaElegida.set(Calendar.MONTH, mes);
            fechaElegida.set(Calendar.DAY_OF_MONTH, dia);
            actualizarBotonesDeFecha();
        }, fechaElegida.get(Calendar.YEAR), fechaElegida.get(Calendar.MONTH),
                fechaElegida.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void elegirHora() {
        new TimePickerDialog(this, (vista, hora, minuto) -> {
            fechaElegida.set(Calendar.HOUR_OF_DAY, hora);
            fechaElegida.set(Calendar.MINUTE, minuto);
            actualizarBotonesDeFecha();
        }, fechaElegida.get(Calendar.HOUR_OF_DAY), fechaElegida.get(Calendar.MINUTE), true).show();
    }

    private void actualizarBotonesDeFecha() {
        long millis = fechaElegida.getTimeInMillis();
        btnFecha.setText(Formato.fechaCorta(millis));
        btnHora.setText(Formato.hora(millis));
    }

    private String tipoElegido() {
        int elegido = rgTipo.getCheckedRadioButtonId();
        if (elegido == R.id.rbTp) {
            return Tarea.TIPO_TP;
        } else if (elegido == R.id.rbExamen) {
            return Tarea.TIPO_EXAMEN;
        }
        return Tarea.TIPO_TAREA;
    }

    private void guardar() {
        String titulo = edtTitulo.getText().toString().trim();
        if (titulo.isEmpty()) {
            edtTitulo.setError("Escribí un título");
            edtTitulo.requestFocus();
            return;
        }

        Materia materia = (Materia) spMateria.getSelectedItem();
        String descripcion = edtDescripcion.getText().toString().trim();
        long fecha = fechaElegida.getTimeInMillis();

        Tarea tarea;
        if (tareaEditada != null) {
            tarea = tareaEditada;
            tarea.setTitulo(titulo);
            tarea.setTipo(tipoElegido());
            tarea.setMateria(materia);
            tarea.setFecha(fecha);
            tarea.setDescripcion(descripcion);
            tarea.setRecordatorio(swRecordatorio.isChecked());
            db.actualizarTarea(tarea);
            // Los avisos viejos se borran y se programan con la fecha nueva
            RecordatorioHelper.cancelar(this, tarea.getId());
            if (!tarea.isHecha()) {
                RecordatorioHelper.programar(this, tarea);
            }
        } else {
            tarea = new Tarea(titulo, tipoElegido(), materia, fecha, descripcion,
                    swRecordatorio.isChecked());
            db.agregarTarea(tarea);
            RecordatorioHelper.programar(this, tarea);
        }

        if (fecha < System.currentTimeMillis() && !tarea.isHecha()) {
            Toast.makeText(this, "Guardada. Ojo: esa fecha ya pasó", Toast.LENGTH_LONG).show();
        } else if (tareaEditada != null) {
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show();
        } else {
            String mensaje = Tarea.TIPO_TAREA.equals(tarea.getTipo())
                    ? "Tarea guardada" : tarea.getTipo() + " guardado";
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    // Menú de la barra superior: solo aparece "Eliminar" cuando se edita una tarea
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (tareaEditada != null) {
            getMenuInflater().inflate(R.menu.editar_tarea, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.accion_eliminar_tarea && tareaEditada != null) {
            AccionesTarea.confirmarEliminar(this, db, tareaEditada, this::finish);
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
