package com.epet30.mochila;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Grilla de días del Calendario. Cada día muestra puntos con el color de las materias
 * que tienen algo ese día. Los lugares vacíos antes del día 1 tienen el valor 0.
 */
public class DiaAdapter extends RecyclerView.Adapter<DiaAdapter.ViewHolder> {

    public interface Listener {
        void onDiaElegido(long dia);
    }

    private final List<Long> dias = new ArrayList<>();
    private final Map<Long, List<Integer>> coloresPorDia = new HashMap<>();
    private final Listener listener;
    private long hoy;
    private long elegido;

    public DiaAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Carga un mes nuevo. */
    public void actualizar(List<Long> nuevosDias, Map<Long, List<Integer>> colores, long hoy, long elegido) {
        dias.clear();
        dias.addAll(nuevosDias);
        coloresPorDia.clear();
        coloresPorDia.putAll(colores);
        this.hoy = hoy;
        this.elegido = elegido;
        notifyDataSetChanged();
    }

    public void elegir(long dia) {
        elegido = dia;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dia, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        long dia = dias.get(position);
        if (dia == 0) {
            holder.numero.setText("");
            holder.numero.setBackground(null);
            for (View punto : holder.puntos) {
                punto.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
            return;
        }

        Calendar calendario = Calendar.getInstance();
        calendario.setTimeInMillis(dia);
        holder.numero.setText(String.valueOf(calendario.get(Calendar.DAY_OF_MONTH)));

        int colorTexto;
        if (dia == elegido) {
            holder.numero.setBackgroundResource(R.drawable.bg_dia_elegido);
            colorTexto = ContextCompat.getColor(holder.itemView.getContext(), R.color.blanco);
        } else if (dia == hoy) {
            holder.numero.setBackgroundResource(R.drawable.bg_dia_hoy);
            colorTexto = ContextCompat.getColor(holder.itemView.getContext(), R.color.tinta);
        } else {
            holder.numero.setBackground(null);
            colorTexto = ContextCompat.getColor(holder.itemView.getContext(), R.color.tinta);
        }
        holder.numero.setTextColor(colorTexto);
        holder.numero.setTypeface(null, dia == hoy || dia == elegido ? Typeface.BOLD : Typeface.NORMAL);

        List<Integer> colores = coloresPorDia.get(dia);
        for (int i = 0; i < holder.puntos.length; i++) {
            if (colores != null && i < colores.size()) {
                GradientDrawable circulo = new GradientDrawable();
                circulo.setShape(GradientDrawable.OVAL);
                circulo.setColor(colores.get(i));
                holder.puntos[i].setBackground(circulo);
                holder.puntos[i].setVisibility(View.VISIBLE);
            } else {
                holder.puntos[i].setVisibility(View.GONE);
            }
        }

        int cantidad = colores == null ? 0 : colores.size();
        holder.itemView.setContentDescription(Formato.fechaLarga(dia)
                + (cantidad == 0 ? "" : ", " + cantidad + (cantidad == 1 ? " entrega" : " entregas")));
        holder.itemView.setOnClickListener(v -> listener.onDiaElegido(dia));
    }

    @Override
    public int getItemCount() {
        return dias.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView numero;
        final View[] puntos;

        ViewHolder(View vista) {
            super(vista);
            numero = vista.findViewById(R.id.txtNumeroDia);
            puntos = new View[]{
                    vista.findViewById(R.id.punto1),
                    vista.findViewById(R.id.punto2),
                    vista.findViewById(R.id.punto3)
            };
        }
    }
}
