package com.epet30.mochila;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.epet30.mochila.modelo.Materia;
import com.epet30.mochila.modelo.Tarea;

import java.util.ArrayList;
import java.util.List;

/**
 * Ventanas para crear, editar y eliminar materias.
 */
public final class MateriaDialogos {

    /** Colores que se pueden elegir para una materia. */
    public static final String[] COLORES = {
            "#2F5BEA", "#6741D9", "#C2255C", "#B4410B", "#E67700", "#087F5B", "#1098AD"
    };

    /** Se ejecuta cuando la materia se guardó o se eliminó. */
    public interface AlTerminar {
        void listo();
    }

    private MateriaDialogos() {
        // Clase de utilidades: no se crean objetos
    }

    /**
     * Muestra el formulario de materia.
     *
     * @param materia la materia a editar, o null para crear una nueva
     */
    public static void mostrarEditor(Activity actividad, DatabaseHelper db, Materia materia,
                                     AlTerminar alTerminar) {
        boolean esNueva = materia == null;
        int margen = dp(actividad, 20);

        LinearLayout contenido = new LinearLayout(actividad);
        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(margen, dp(actividad, 8), margen, 0);

        EditText edtNombre = new EditText(actividad);
        edtNombre.setHint("Nombre de la materia");
        edtNombre.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        edtNombre.setMaxLines(1);
        edtNombre.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        contenido.addView(edtNombre);

        TextView txtColor = new TextView(actividad);
        txtColor.setText("Color");
        txtColor.setTextColor(ContextCompat.getColor(actividad, R.color.tinta));
        txtColor.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        txtColor.setPadding(0, dp(actividad, 16), 0, dp(actividad, 8));
        contenido.addView(txtColor);

        // Fila de círculos de colores; el elegido se marca con un borde
        LinearLayout filaColores = new LinearLayout(actividad);
        filaColores.setOrientation(LinearLayout.HORIZONTAL);
        contenido.addView(filaColores);

        final String[] colorElegido = {
                esNueva ? COLORES[db.contarMaterias() % COLORES.length] : materia.getColor()
        };
        List<View> circulos = new ArrayList<>();
        for (String color : COLORES) {
            View circulo = new View(actividad);
            LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(dp(actividad, 34), dp(actividad, 34));
            parametros.setMarginEnd(dp(actividad, 8));
            circulo.setLayoutParams(parametros);
            circulo.setContentDescription("Color " + color);
            circulo.setTag(color);
            circulo.setOnClickListener(v -> {
                colorElegido[0] = (String) v.getTag();
                pintarCirculos(actividad, circulos, colorElegido[0]);
            });
            circulos.add(circulo);
            filaColores.addView(circulo);
        }
        pintarCirculos(actividad, circulos, colorElegido[0]);

        if (!esNueva) {
            edtNombre.setText(materia.getNombre());
            edtNombre.setSelection(edtNombre.getText().length());
        }

        AlertDialog dialogo = new AlertDialog.Builder(actividad)
                .setTitle(esNueva ? "Nueva materia" : "Editar materia")
                .setView(contenido)
                .setPositiveButton("Guardar", null) // se configura abajo para poder validar
                .setNegativeButton("Cancelar", null)
                .create();

        dialogo.setOnShowListener(d -> dialogo.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String nombre = edtNombre.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        edtNombre.setError("Escribí el nombre de la materia");
                        return;
                    }
                    if (esNueva) {
                        db.agregarMateria(new Materia(nombre, colorElegido[0]));
                        Toast.makeText(actividad, nombre + " agregada", Toast.LENGTH_SHORT).show();
                    } else {
                        materia.setNombre(nombre);
                        materia.setColor(colorElegido[0]);
                        db.actualizarMateria(materia);
                    }
                    dialogo.dismiss();
                    alTerminar.listo();
                }));
        dialogo.show();
    }

    /** Pide confirmación y borra la materia con todas sus tareas y notas. */
    public static void confirmarEliminar(Activity actividad, DatabaseHelper db, Materia materia,
                                         AlTerminar alTerminar) {
        int tareas = db.obtenerTareasDeMateria(materia.getId()).size();
        int notas = db.contarNotas(materia.getId());
        new AlertDialog.Builder(actividad)
                .setTitle("Eliminar " + materia.getNombre())
                .setMessage("También se van a borrar sus " + tareas + " tareas y sus "
                        + notas + " notas. No se puede deshacer.")
                .setPositiveButton("Eliminar", (d, boton) -> {
                    // Primero se cancelan los recordatorios de sus tareas
                    for (Tarea tarea : db.obtenerTareasDeMateria(materia.getId())) {
                        RecordatorioHelper.cancelar(actividad, tarea.getId());
                    }
                    db.eliminarMateria(materia.getId());
                    Toast.makeText(actividad, materia.getNombre() + " eliminada", Toast.LENGTH_SHORT).show();
                    alTerminar.listo();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private static void pintarCirculos(Activity actividad, List<View> circulos, String elegido) {
        for (View circulo : circulos) {
            String color = (String) circulo.getTag();
            GradientDrawable forma = new GradientDrawable();
            forma.setShape(GradientDrawable.OVAL);
            forma.setColor(Color.parseColor(color));
            if (color.equalsIgnoreCase(elegido)) {
                forma.setStroke(dp(actividad, 4), ContextCompat.getColor(actividad, R.color.tinta));
            }
            circulo.setBackground(forma);
        }
    }

    private static int dp(Activity actividad, int valor) {
        return Math.round(valor * actividad.getResources().getDisplayMetrics().density);
    }
}
