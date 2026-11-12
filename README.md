# Mochila – App para organizar el estudio

Trabajo final de Programación · Alumno: Matías · Curso: 5° 1° · EPET N°30

Mochila es una app Android hecha en **Java con Android Studio**. Sirve para organizar tareas, trabajos prácticos y exámenes por materia, verlos en un calendario, guardar apuntes, estudiar con la técnica Pomodoro y seguir el progreso de la semana, con recordatorios que llegan como notificación.

**Versión 2.0.** Cambios respecto de la primera entrega:

- Barra de navegación inferior con 5 secciones: Inicio, Materias, Calendario, Pomodoro y Progreso.
- **Archivo de tareas hechas:** al completar una tarea no se pierde. Pasa a la pestaña *Hechas* con la fecha en que se hizo, se puede deshacer al momento y se puede devolver a pendientes.
- Editar tareas (tocando la tarjeta) y eliminarlas desde el menú.
- Crear, editar y eliminar materias, eligiendo su color.
- **Calendario mensual** con puntos de colores en los días con entregas o exámenes.
- **Progreso:** estadísticas de los últimos 7 días, con un gráfico de barras dibujado con Canvas.
- Los recordatorios se vuelven a programar si se reinicia el celular.
- **Modo oscuro** automático y diseño más prolijo (franja de color por materia, estados vacíos, títulos grandes).
- La base de datos se actualiza sin borrar lo que ya estaba cargado.

## Cómo abrir y ejecutar el proyecto

1. Descomprimir el .zip.
2. En Android Studio: **File › Open** y elegir la carpeta `Mochila` (la que tiene `settings.gradle`).
3. Si pregunta por la versión de Java de Gradle, elegir **JVM 21**.
4. Esperar a que termine la sincronización de Gradle (la primera vez descarga lo necesario, hace falta internet).
5. Conectar un celular con depuración USB (o usar un emulador) y tocar **Run ▶**.

**Sin Android Studio (APK generado en GitHub):** el proyecto incluye `.github/workflows/apk.yml`. Al subirlo a un repositorio de GitHub, la pestaña **Actions** compila la app sola y deja el archivo `app-debug.apk` para descargar e instalar en el celular.

Datos técnicos: compileSdk 34, minSdk 26 (Android 8.0 o superior), Gradle 8.5, Android Gradle Plugin 8.2.2.

## Pantallas (9 Activity)

| Activity | Qué hace |
| --- | --- |
| `BienvenidaActivity` | Presentación con el logo animado. Se muestra solo la primera vez (se recuerda con SharedPreferences). |
| `MainActivity` | **Inicio.** Dos pestañas: *Pendientes* (ordenadas por fecha, con los días que faltan y cuántas vencen hoy) y *Hechas* (archivo de tareas completadas). Tocar una tarjeta la edita; mantenerla presionada la elimina. |
| `NuevaTareaActivity` | Formulario para cargar o **editar** una tarea, TP o examen: título, materia, tipo, fecha, hora, recordatorio y descripción. |
| `MateriasActivity` | Lista de materias con sus pendientes y notas. Botón + para agregar; mantener presionada una materia para editarla o eliminarla. |
| `MateriaDetalleActivity` | Tareas y notas de una materia, en dos pestañas. La barra superior toma el color de la materia. |
| `NotaActivity` | Crear, editar o eliminar una nota (apunte o resumen). |
| `CalendarioActivity` | Calendario del mes con puntos de colores. Al tocar un día se ven sus tareas y se puede agregar una en esa fecha. |
| `PomodoroActivity` | Temporizador de 25 minutos de estudio y 5 de descanso, eligiendo la materia. Guarda los bloques terminados. |
| `ProgresoActivity` | Estadísticas: tareas hechas, minutos de estudio, racha de días seguidos, gráfico por día, estudio por materia y pendientes. |

**Navegación:** Bienvenida → Inicio. Desde la barra inferior se pasa entre Inicio, Materias, Calendario, Pomodoro y Progreso. Desde Inicio, Materias y Calendario se abre el formulario de tarea; desde Materias se entra al Detalle de materia y de ahí a cada Nota. El botón Atrás siempre vuelve a Inicio.

## Clases Java

| Clase | Función |
| --- | --- |
| `modelo/Materia` | Una materia: nombre y color. |
| `modelo/Tarea` | Tarea, TP o examen. **Se relaciona con `Materia`** (tiene un objeto Materia). Guarda si está hecha y cuándo se hizo. Calcula los días que faltan. |
| `modelo/Nota` | Apunte de una materia. |
| `DatabaseHelper` | Base de datos SQLite: crea y actualiza las tablas, guarda y lee materias, tareas, notas y sesiones, y calcula las estadísticas. |
| `Navegacion` | Maneja la barra de navegación inferior que comparten las 5 pantallas principales. |
| `AccionesTarea` | Acciones comunes sobre una tarea: editar, marcar como hecha (con *Deshacer*) y eliminar. |
| `MateriaDialogos` | Ventanas para crear, editar (nombre y color) y eliminar materias. |
| `GraficoBarras` | Vista propia (hereda de `View`) que dibuja el gráfico de barras con `Canvas`. |
| `PomodoroTimer` | Temporizador que se inicia, pausa y reinicia; avisa a la pantalla con una interfaz `Listener`. |
| `RecordatorioHelper` | Programa avisos con AlarmManager (1 día antes y 1 hora antes) y muestra notificaciones. |
| `RecordatorioReceiver` | Recibe la alarma y muestra la notificación aunque la app esté cerrada. |
| `ReinicioReceiver` | Al encender el celular, vuelve a programar los recordatorios de las tareas pendientes. |
| `TareaAdapter`, `MateriaAdapter`, `NotaAdapter`, `DiaAdapter` | Muestran las listas y la grilla del calendario en RecyclerView. |
| `Formato` | Muestra fechas, horas y minutos en español. |

Las clases de modelo usan atributos privados (encapsulamiento), constructores y métodos get y set. `GraficoBarras` usa herencia (extiende `View`) y las pantallas implementan interfaces para escuchar eventos (`Listener`).

## Componentes de la interfaz

TextView, EditText, Button, ImageView, ImageButton, Spinner (materias), CheckBox (tarea hecha), RadioButton (tipo de tarea y pestañas), Switch (recordatorio y modo demostración), RecyclerView (listas y grilla del calendario), CardView, FloatingActionButton, CircularProgressIndicator, ProgressBar, BottomNavigationView, MaterialButtonToggleGroup, Snackbar, AlertDialog y una vista propia con Canvas.

## Persistencia de datos

- **SQLite** (`mochila.db`, versión 2), con 4 tablas: `materias`, `tareas`, `notas` y `sesiones`. Las tareas hechas guardan la fecha en `fecha_hecha`.
- Al actualizar desde la versión 1, la tabla se modifica con `ALTER TABLE` y **no se pierde nada** de lo cargado.
- **SharedPreferences**: recuerda si ya se vio la pantalla de Bienvenida.

Al abrir la app por primera vez se crean 4 materias de ejemplo: Programación, Matemática, Física e Inglés.

## Desafío adicional

1. **Notificaciones:** recordatorios de entregas y exámenes, y aviso al terminar cada bloque de Pomodoro. En Android 13 o superior la app pide permiso. Se reprograman después de reiniciar el celular.
2. **Animaciones:** el logo y los textos de la pantalla de Bienvenida aparecen con animación.
3. **Sonidos:** suena un aviso al terminar cada bloque de Pomodoro.
4. **Extra:** calendario, estadísticas con gráfico propio y modo oscuro.

## Identidad visual

- Logo: una mochila azul con un tilde en el bolsillo (`res/drawable/ic_logo.xml`), usado en la Bienvenida, en Inicio y como ícono de la app.
- Colores: azul `#2F5BEA`, azul oscuro `#1F3FA8`, ámbar `#F5A623`, tinta `#1B2333` y fondo `#F7F8FC`. En modo oscuro se usan las variantes de `res/values-night/colors.xml`.
- Estilo: tarjetas redondeadas con una franja del color de la materia, títulos grandes, botones grandes y textos claros.

## Para la demostración

1. **Archivo:** marcar una tarea como hecha → aparece el aviso con *Deshacer* → verla en la pestaña *Hechas* con su fecha.
2. **Editar:** tocar una tarjeta, cambiar la fecha y guardar.
3. **Calendario:** ver los puntos de colores y tocar un día.
4. **Pomodoro:** activar **Modo demostración** para que los bloques duren 10 segundos (estudio) y 5 segundos (descanso), así se ve el sonido y la notificación sin esperar 25 minutos.
5. **Progreso:** ver el gráfico y la racha después de terminar algunos bloques (en modo demostración los bloques cuentan como 0 minutos, pero sí suman a la racha).
6. **Modo oscuro:** activarlo en el celular y volver a la app.

Para probar un recordatorio: cargar un examen con fecha de hoy y una hora que sea dentro de poco más de una hora; el aviso "En una hora" llega a la hora programada. Las alarmas usan el modo no exacto de Android, así que pueden llegar unos minutos tarde.

## Limitaciones conocidas

- El temporizador Pomodoro funciona con la pantalla abierta; si se cambia de sección mientras corre, la app avisa que se va a detener.
- Los datos se guardan solo en el celular (no hay cuenta ni copia en la nube).
