package com.epet30.mochila;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.epet30.mochila.modelo.Materia;
import com.epet30.mochila.modelo.Nota;
import com.epet30.mochila.modelo.Tarea;

import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y recupera toda la información de la app en una base de datos SQLite
 * dentro del celular: materias, tareas, notas y sesiones de Pomodoro.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "mochila.db";
    // Versión 2: las tareas hechas guardan la fecha en que se completaron (archivo)
    private static final int VERSION_BD = 2;

    private static final long UN_DIA_MS = 24L * 60 * 60 * 1000;

    private static final String CONSULTA_TAREAS =
            "SELECT t.id, t.titulo, t.tipo, t.fecha, t.descripcion, t.hecha, t.recordatorio, "
                    + "m.id, m.nombre, m.color, t.fecha_hecha "
                    + "FROM tareas t JOIN materias m ON t.materia_id = m.id ";

    public DatabaseHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE materias ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nombre TEXT NOT NULL, "
                + "color TEXT NOT NULL)");

        db.execSQL("CREATE TABLE tareas ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "titulo TEXT NOT NULL, "
                + "tipo TEXT NOT NULL, "
                + "materia_id INTEGER NOT NULL, "
                + "fecha INTEGER NOT NULL, "
                + "descripcion TEXT, "
                + "hecha INTEGER NOT NULL DEFAULT 0, "
                + "recordatorio INTEGER NOT NULL DEFAULT 1, "
                + "fecha_hecha INTEGER, "
                + "FOREIGN KEY (materia_id) REFERENCES materias(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE notas ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "materia_id INTEGER NOT NULL, "
                + "titulo TEXT NOT NULL, "
                + "contenido TEXT, "
                + "fecha_edicion INTEGER NOT NULL, "
                + "FOREIGN KEY (materia_id) REFERENCES materias(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE sesiones ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "materia_id INTEGER, "
                + "minutos INTEGER NOT NULL, "
                + "fecha INTEGER NOT NULL, "
                + "FOREIGN KEY (materia_id) REFERENCES materias(id) ON DELETE SET NULL)");

        // Materias de ejemplo para poder usar la app desde el primer momento
        insertarMateriaInicial(db, "Programación", "#087F5B");
        insertarMateriaInicial(db, "Matemática", "#2F5BEA");
        insertarMateriaInicial(db, "Física", "#B4410B");
        insertarMateriaInicial(db, "Inglés", "#6741D9");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionVieja, int versionNueva) {
        // Se actualiza la tabla sin borrar lo que el usuario ya cargó
        if (versionVieja < 2) {
            db.execSQL("ALTER TABLE tareas ADD COLUMN fecha_hecha INTEGER");
            db.execSQL("UPDATE tareas SET fecha_hecha = fecha WHERE hecha = 1");
        }
    }

    private static void insertarMateriaInicial(SQLiteDatabase db, String nombre, String color) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("color", color);
        db.insert("materias", null, valores);
    }

    private static String[] argumentos(Object... valores) {
        String[] texto = new String[valores.length];
        for (int i = 0; i < valores.length; i++) {
            texto[i] = String.valueOf(valores[i]);
        }
        return texto;
    }

    // ---------------------------------------------------------------- Materias

    public long agregarMateria(Materia materia) {
        long id = getWritableDatabase().insert("materias", null, valoresDeMateria(materia));
        materia.setId((int) id);
        return id;
    }

    public void actualizarMateria(Materia materia) {
        getWritableDatabase().update("materias", valoresDeMateria(materia), "id = ?",
                argumentos(materia.getId()));
    }

    /** Borra la materia junto con sus tareas y notas (ON DELETE CASCADE). */
    public void eliminarMateria(int materiaId) {
        getWritableDatabase().delete("materias", "id = ?", argumentos(materiaId));
    }

    private static ContentValues valoresDeMateria(Materia materia) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", materia.getNombre());
        valores.put("color", materia.getColor());
        return valores;
    }

    public List<Materia> obtenerMaterias() {
        List<Materia> lista = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id, nombre, color FROM materias ORDER BY nombre COLLATE NOCASE", null);
        try {
            while (cursor.moveToNext()) {
                lista.add(new Materia(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
            }
        } finally {
            cursor.close();
        }
        return lista;
    }

    public Materia obtenerMateria(int id) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id, nombre, color FROM materias WHERE id = ?", argumentos(id));
        try {
            if (cursor.moveToFirst()) {
                return new Materia(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public int contarMaterias() {
        return contar("SELECT COUNT(*) FROM materias", null);
    }

    public int contarPendientes(int materiaId) {
        return contar("SELECT COUNT(*) FROM tareas WHERE materia_id = ? AND hecha = 0",
                argumentos(materiaId));
    }

    public int contarNotas(int materiaId) {
        return contar("SELECT COUNT(*) FROM notas WHERE materia_id = ?", argumentos(materiaId));
    }

    private int contar(String sql, String[] argumentos) {
        Cursor cursor = getReadableDatabase().rawQuery(sql, argumentos);
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    // ------------------------------------------------------------------ Tareas

    public long agregarTarea(Tarea tarea) {
        long id = getWritableDatabase().insert("tareas", null, valoresDeTarea(tarea));
        tarea.setId((int) id);
        return id;
    }

    public void actualizarTarea(Tarea tarea) {
        getWritableDatabase().update("tareas", valoresDeTarea(tarea), "id = ?",
                argumentos(tarea.getId()));
    }

    private static ContentValues valoresDeTarea(Tarea tarea) {
        ContentValues valores = new ContentValues();
        valores.put("titulo", tarea.getTitulo());
        valores.put("tipo", tarea.getTipo());
        valores.put("materia_id", tarea.getMateria().getId());
        valores.put("fecha", tarea.getFecha());
        valores.put("descripcion", tarea.getDescripcion());
        valores.put("hecha", tarea.isHecha() ? 1 : 0);
        valores.put("recordatorio", tarea.isRecordatorio() ? 1 : 0);
        if (tarea.isHecha() && tarea.getFechaHecha() > 0) {
            valores.put("fecha_hecha", tarea.getFechaHecha());
        } else {
            valores.putNull("fecha_hecha");
        }
        return valores;
    }

    public Tarea obtenerTarea(int tareaId) {
        List<Tarea> lista = leerTareas(CONSULTA_TAREAS + "WHERE t.id = ?", argumentos(tareaId));
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** Tareas sin terminar de todas las materias, de la más próxima a la más lejana. */
    public List<Tarea> obtenerTareasPendientes() {
        return leerTareas(CONSULTA_TAREAS + "WHERE t.hecha = 0 ORDER BY t.fecha", null);
    }

    /** Archivo: tareas ya hechas, de la última completada a la primera. */
    public List<Tarea> obtenerTareasHechas() {
        return leerTareas(CONSULTA_TAREAS
                + "WHERE t.hecha = 1 ORDER BY COALESCE(t.fecha_hecha, t.fecha) DESC", null);
    }

    /** Tareas de una materia: primero las pendientes y después las hechas. */
    public List<Tarea> obtenerTareasDeMateria(int materiaId) {
        return leerTareas(CONSULTA_TAREAS + "WHERE t.materia_id = ? ORDER BY t.hecha, t.fecha",
                argumentos(materiaId));
    }

    /** Todas las tareas (hechas o no) con fecha entre desde (incluida) y hasta (excluida). */
    public List<Tarea> obtenerTareasEntre(long desde, long hasta) {
        return leerTareas(CONSULTA_TAREAS + "WHERE t.fecha >= ? AND t.fecha < ? ORDER BY t.fecha",
                argumentos(desde, hasta));
    }

    /** Tareas pendientes con recordatorio activado y fecha futura (para reprogramar avisos). */
    public List<Tarea> obtenerTareasConRecordatorio() {
        return leerTareas(CONSULTA_TAREAS
                        + "WHERE t.hecha = 0 AND t.recordatorio = 1 AND t.fecha > ? ORDER BY t.fecha",
                argumentos(System.currentTimeMillis()));
    }

    /** Marca la tarea como hecha (y guarda cuándo) o la devuelve a pendientes. */
    public void marcarHecha(int tareaId, boolean hecha) {
        ContentValues valores = new ContentValues();
        valores.put("hecha", hecha ? 1 : 0);
        if (hecha) {
            valores.put("fecha_hecha", System.currentTimeMillis());
        } else {
            valores.putNull("fecha_hecha");
        }
        getWritableDatabase().update("tareas", valores, "id = ?", argumentos(tareaId));
    }

    public void eliminarTarea(int tareaId) {
        getWritableDatabase().delete("tareas", "id = ?", argumentos(tareaId));
    }

    private List<Tarea> leerTareas(String sql, String[] argumentos) {
        List<Tarea> lista = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery(sql, argumentos);
        try {
            while (cursor.moveToNext()) {
                Materia materia = new Materia(cursor.getInt(7), cursor.getString(8), cursor.getString(9));
                lista.add(new Tarea(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        materia,
                        cursor.getLong(3),
                        cursor.getString(4),
                        cursor.getInt(5) == 1,
                        cursor.getInt(6) == 1,
                        cursor.isNull(10) ? 0 : cursor.getLong(10)));
            }
        } finally {
            cursor.close();
        }
        return lista;
    }

    // ------------------------------------------------------------------- Notas

    public long agregarNota(Nota nota) {
        ContentValues valores = valoresDeNota(nota);
        long id = getWritableDatabase().insert("notas", null, valores);
        nota.setId((int) id);
        return id;
    }

    public void actualizarNota(Nota nota) {
        getWritableDatabase().update("notas", valoresDeNota(nota), "id = ?",
                argumentos(nota.getId()));
    }

    public void eliminarNota(int notaId) {
        getWritableDatabase().delete("notas", "id = ?", argumentos(notaId));
    }

    public List<Nota> obtenerNotas(int materiaId) {
        List<Nota> lista = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id, materia_id, titulo, contenido, fecha_edicion FROM notas "
                        + "WHERE materia_id = ? ORDER BY fecha_edicion DESC",
                argumentos(materiaId));
        try {
            while (cursor.moveToNext()) {
                lista.add(leerNota(cursor));
            }
        } finally {
            cursor.close();
        }
        return lista;
    }

    public Nota obtenerNota(int notaId) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id, materia_id, titulo, contenido, fecha_edicion FROM notas WHERE id = ?",
                argumentos(notaId));
        try {
            return cursor.moveToFirst() ? leerNota(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    private static ContentValues valoresDeNota(Nota nota) {
        ContentValues valores = new ContentValues();
        valores.put("materia_id", nota.getMateriaId());
        valores.put("titulo", nota.getTitulo());
        valores.put("contenido", nota.getContenido());
        valores.put("fecha_edicion", nota.getFechaEdicion());
        return valores;
    }

    private static Nota leerNota(Cursor cursor) {
        return new Nota(cursor.getInt(0), cursor.getInt(1), cursor.getString(2),
                cursor.getString(3), cursor.getLong(4));
    }

    // ---------------------------------------------------------------- Pomodoro

    /** Guarda un bloque de estudio terminado. materiaId puede ser null. */
    public void agregarSesion(Integer materiaId, int minutos) {
        ContentValues valores = new ContentValues();
        if (materiaId == null) {
            valores.putNull("materia_id");
        } else {
            valores.put("materia_id", materiaId);
        }
        valores.put("minutos", minutos);
        valores.put("fecha", System.currentTimeMillis());
        getWritableDatabase().insert("sesiones", null, valores);
    }

    /** Devuelve {bloques terminados hoy, minutos estudiados hoy}. */
    public int[] resumenDeHoy() {
        return resumenDesde(Tarea.inicioDelDia(System.currentTimeMillis()));
    }

    /** Devuelve {bloques terminados, minutos estudiados} desde ese momento. */
    public int[] resumenDesde(long desde) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*), COALESCE(SUM(minutos), 0) FROM sesiones WHERE fecha >= ?",
                argumentos(desde));
        try {
            if (cursor.moveToFirst()) {
                return new int[]{cursor.getInt(0), cursor.getInt(1)};
            }
            return new int[]{0, 0};
        } finally {
            cursor.close();
        }
    }

    // ------------------------------------------------------------ Estadísticas

    public int contarHechasDesde(long desde) {
        return contar("SELECT COUNT(*) FROM tareas WHERE hecha = 1 AND fecha_hecha >= ?",
                argumentos(desde));
    }

    public int contarVencidas() {
        return contar("SELECT COUNT(*) FROM tareas WHERE hecha = 0 AND fecha < ?",
                argumentos(System.currentTimeMillis()));
    }

    public int contarProximas(int dias) {
        long ahora = System.currentTimeMillis();
        return contar("SELECT COUNT(*) FROM tareas WHERE hecha = 0 AND fecha >= ? AND fecha < ?",
                argumentos(ahora, ahora + dias * UN_DIA_MS));
    }

    /** Minutos estudiados en cada uno de los últimos días (el último lugar es hoy). */
    public int[] minutosPorDia(int dias) {
        int[] minutos = new int[dias];
        long hoy = Tarea.inicioDelDia(System.currentTimeMillis());
        long desde = Tarea.inicioDelDia(hoy - (dias - 1) * UN_DIA_MS);
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT fecha, minutos FROM sesiones WHERE fecha >= ?", argumentos(desde));
        try {
            while (cursor.moveToNext()) {
                int indice = Tarea.diasEntre(desde, cursor.getLong(0));
                if (indice >= 0 && indice < dias) {
                    minutos[indice] += cursor.getInt(1);
                }
            }
        } finally {
            cursor.close();
        }
        return minutos;
    }

    /** Materias con los minutos estudiados desde esa fecha, de la más estudiada a la menos. */
    public List<Materia> minutosPorMateria(long desde) {
        List<Materia> lista = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT m.id, m.nombre, m.color, SUM(s.minutos) AS total "
                        + "FROM sesiones s JOIN materias m ON s.materia_id = m.id "
                        + "WHERE s.fecha >= ? GROUP BY m.id ORDER BY total DESC",
                argumentos(desde));
        try {
            while (cursor.moveToNext()) {
                Materia materia = new Materia(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
                materia.setMinutosEstudiados(cursor.getInt(3));
                lista.add(materia);
            }
        } finally {
            cursor.close();
        }
        return lista;
    }

    /** Racha: cuántos días seguidos (hasta hoy o ayer) se terminó al menos un bloque de estudio. */
    public int rachaDeEstudio() {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT fecha FROM sesiones ORDER BY fecha DESC", null);
        try {
            long hoy = System.currentTimeMillis();
            int racha = 0;
            int diaEsperado = -1; // días atrás desde hoy que tendría que tener estudio
            while (cursor.moveToNext()) {
                int diasAtras = Tarea.diasEntre(cursor.getLong(0), hoy);
                if (diaEsperado == -1) {
                    if (diasAtras > 1) {
                        return 0; // el último estudio fue antes de ayer: la racha se cortó
                    }
                    diaEsperado = diasAtras;
                }
                if (diasAtras == diaEsperado) {
                    racha++;
                    diaEsperado++;
                } else if (diasAtras > diaEsperado) {
                    break; // faltó un día
                }
            }
            return racha;
        } finally {
            cursor.close();
        }
    }
}
