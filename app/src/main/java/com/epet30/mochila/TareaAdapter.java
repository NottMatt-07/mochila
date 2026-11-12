package com.epet30.mochila;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.epet30.mochila.modelo.Tarea;

import java.util.List;
import java.util.Locale;

/**
 * Muestra una lista de tareas en tarjetas (RecyclerView + CardView).
 */
public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.ViewHolder> {

    /** Acciones que la pantalla quiere escuchar. */
    public interface Listener {
        void onMarcarHecha(Tarea tarea, boolean hecha);

        void onTocar(Tarea tarea);

        void onMantenerPresionado(Tarea tarea);
    }

    private final List<Tarea> tareas;
    private final boolean mostrarMateria;
    private final Listener listener;

    public TareaAdapter(List<Tarea> tareas, boolean mostrarMateria, Listener listener) {
        this.tareas = tareas;
        this.mostrarMateria = mostrarMateria;
        this.listener = listener;
    }

    public void actualizar(List<Tarea> nuevas) {
        tareas.clear();
        tareas.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarea, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tarea tarea = tareas.get(position);
        int colorMateria = tarea.getMateria().getColorInt();

        holder.franja.setBackgroundColor(colorMateria);

        String etiqueta = tarea.getTipo().toUpperCase(Locale.ROOT);
        if (mostrarMateria) {
            etiqueta += " · " + tarea.getMateria().getNombre().toUpperCase(Locale.ROOT);
        }
        holder.etiqueta.setText(etiqueta);
        holder.etiqueta.setTextColor(colorMateria);
        holder.titulo.setText(tarea.getTitulo());
        holder.fecha.setText(Formato.fechaYHora(tarea.getFecha()));

        // Cartel de la derecha: días que faltan, o cuándo se hizo
        if (tarea.isHecha()) {
            String texto = tarea.getFechaHecha() > 0
                    ? "Hecha · " + Formato.diaYMes(tarea.getFechaHecha()) : "Hecha";
            holder.dias.setText(texto);
            holder.dias.setBackgroundResource(R.drawable.bg_pill_hecha);
            holder.dias.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.azul_texto));
        } else if (tarea.esUrgente()) {
            holder.dias.setText(tarea.getTextoDiasRestantes());
            holder.dias.setBackgroundResource(R.drawable.bg_pill_urgente);
            holder.dias.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.sobre_ambar));
        } else {
            holder.dias.setText(tarea.getTextoDiasRestantes());
            holder.dias.setBackgroundResource(R.drawable.bg_pill);
            holder.dias.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.tinta));
        }

        // Las tareas hechas se muestran tachadas
        if (tarea.isHecha()) {
            holder.titulo.setPaintFlags(holder.titulo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.titulo.setAlpha(0.6f);
        } else {
            holder.titulo.setPaintFlags(holder.titulo.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.titulo.setAlpha(1f);
        }

        // Se quita el listener antes de cambiar el estado para no disparar eventos falsos
        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(tarea.isHecha());
        holder.check.setContentDescription("Marcar " + tarea.getTitulo() + " como hecha");
        holder.check.setOnCheckedChangeListener((boton, marcado) -> listener.onMarcarHecha(tarea, marcado));

        holder.itemView.setOnClickListener(v -> listener.onTocar(tarea));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onMantenerPresionado(tarea);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return tareas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View franja;
        final CheckBox check;
        final TextView etiqueta;
        final TextView titulo;
        final TextView fecha;
        final TextView dias;

        ViewHolder(View vista) {
            super(vista);
            franja = vista.findViewById(R.id.franjaMateria);
            check = vista.findViewById(R.id.checkHecha);
            etiqueta = vista.findViewById(R.id.txtEtiqueta);
            titulo = vista.findViewById(R.id.txtTitulo);
            fecha = vista.findViewById(R.id.txtFecha);
            dias = vista.findViewById(R.id.txtDias);
        }
    }
}
