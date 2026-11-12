package com.epet30.mochila;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.epet30.mochila.modelo.Nota;

import java.util.List;

/**
 * Muestra las notas (apuntes) de una materia.
 */
public class NotaAdapter extends RecyclerView.Adapter<NotaAdapter.ViewHolder> {

    public interface Listener {
        void onNotaClick(Nota nota);
    }

    private final List<Nota> notas;
    private final Listener listener;

    public NotaAdapter(List<Nota> notas, Listener listener) {
        this.notas = notas;
        this.listener = listener;
    }

    public void actualizar(List<Nota> nuevas) {
        notas.clear();
        notas.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nota, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Nota nota = notas.get(position);
        holder.titulo.setText(nota.getTitulo());
        holder.resumen.setText(nota.getResumen());
        holder.fecha.setText("Editada el " + Formato.fechaCorta(nota.getFechaEdicion()));
        holder.itemView.setOnClickListener(v -> listener.onNotaClick(nota));
    }

    @Override
    public int getItemCount() {
        return notas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView titulo;
        final TextView resumen;
        final TextView fecha;

        ViewHolder(View vista) {
            super(vista);
            titulo = vista.findViewById(R.id.txtTituloNota);
            resumen = vista.findViewById(R.id.txtResumenNota);
            fecha = vista.findViewById(R.id.txtFechaNota);
        }
    }
}
