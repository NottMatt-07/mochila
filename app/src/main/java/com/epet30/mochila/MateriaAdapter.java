package com.epet30.mochila;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.epet30.mochila.modelo.Materia;

import java.util.List;

/**
 * Muestra la lista de materias con su color, pendientes y cantidad de notas.
 */
public class MateriaAdapter extends RecyclerView.Adapter<MateriaAdapter.ViewHolder> {

    public interface Listener {
        void onMateriaClick(Materia materia);

        void onMateriaMantenida(Materia materia);
    }

    private final List<Materia> materias;
    private final Listener listener;

    public MateriaAdapter(List<Materia> materias, Listener listener) {
        this.materias = materias;
        this.listener = listener;
    }

    public void actualizar(List<Materia> nuevas) {
        materias.clear();
        materias.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_materia, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Materia materia = materias.get(position);

        GradientDrawable circulo = new GradientDrawable();
        circulo.setShape(GradientDrawable.OVAL);
        circulo.setColor(materia.getColorInt());
        holder.inicial.setBackground(circulo);
        holder.inicial.setText(materia.getInicial());

        holder.nombre.setText(materia.getNombre());

        int pendientes = materia.getCantidadPendientes();
        int notas = materia.getCantidadNotas();
        String detalle = pendientes + (pendientes == 1 ? " pendiente" : " pendientes")
                + " · " + notas + (notas == 1 ? " nota" : " notas");
        holder.detalle.setText(detalle);

        holder.itemView.setOnClickListener(v -> listener.onMateriaClick(materia));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onMateriaMantenida(materia);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return materias.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView inicial;
        final TextView nombre;
        final TextView detalle;

        ViewHolder(View vista) {
            super(vista);
            inicial = vista.findViewById(R.id.txtInicial);
            nombre = vista.findViewById(R.id.txtNombreMateria);
            detalle = vista.findViewById(R.id.txtDetalleMateria);
        }
    }
}
