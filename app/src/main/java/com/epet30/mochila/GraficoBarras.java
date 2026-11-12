package com.epet30.mochila;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;

/**
 * Gráfico de barras simple, dibujado a mano con Canvas.
 * Se usa en Progreso para mostrar los minutos de estudio de cada día.
 */
public class GraficoBarras extends View {

    private int[] valores = new int[0];
    private String[] etiquetas = new String[0];
    private int indiceDestacado = -1;

    private final Paint pincelBarra = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelFondo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelValor = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public GraficoBarras(Context context) {
        this(context, null);
    }

    public GraficoBarras(Context context, AttributeSet attrs) {
        super(context, attrs);
        float tamanioTexto = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12,
                getResources().getDisplayMetrics());
        pincelFondo.setColor(ContextCompat.getColor(context, R.color.gris_claro));
        pincelTexto.setColor(ContextCompat.getColor(context, R.color.gris));
        pincelTexto.setTextSize(tamanioTexto);
        pincelTexto.setTextAlign(Paint.Align.CENTER);
        pincelValor.setColor(ContextCompat.getColor(context, R.color.tinta));
        pincelValor.setTextSize(tamanioTexto);
        pincelValor.setTextAlign(Paint.Align.CENTER);
        pincelValor.setFakeBoldText(true);
    }

    /**
     * @param valores    alto de cada barra
     * @param etiquetas  texto debajo de cada barra
     * @param destacado  barra que se pinta de ámbar (por ejemplo, hoy); -1 para ninguna
     */
    public void setDatos(int[] valores, String[] etiquetas, int destacado) {
        this.valores = valores;
        this.etiquetas = etiquetas;
        this.indiceDestacado = destacado;
        StringBuilder descripcion = new StringBuilder("Minutos de estudio por día: ");
        for (int i = 0; i < valores.length; i++) {
            descripcion.append(etiquetas[i]).append(' ').append(valores[i]).append(i < valores.length - 1 ? ", " : "");
        }
        setContentDescription(descripcion);
        invalidate(); // vuelve a dibujar
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (valores.length == 0) {
            return;
        }
        float densidad = getResources().getDisplayMetrics().density;
        float altoTexto = pincelTexto.getTextSize();
        float espacioAbajo = altoTexto + 8 * densidad;  // lugar para las etiquetas
        float espacioArriba = altoTexto + 6 * densidad; // lugar para el número
        float altoUtil = getHeight() - espacioAbajo - espacioArriba;
        float anchoColumna = getWidth() / (float) valores.length;
        float anchoBarra = Math.min(anchoColumna * 0.55f, 36 * densidad);
        float radio = 8 * densidad;

        int maximo = 1;
        for (int valor : valores) {
            maximo = Math.max(maximo, valor);
        }

        for (int i = 0; i < valores.length; i++) {
            float centro = anchoColumna * i + anchoColumna / 2;
            float izquierda = centro - anchoBarra / 2;
            float base = espacioArriba + altoUtil;

            // Fondo gris de toda la columna
            rect.set(izquierda, espacioArriba, izquierda + anchoBarra, base);
            canvas.drawRoundRect(rect, radio, radio, pincelFondo);

            // Barra con el valor (al menos un poquito si es mayor que cero)
            if (valores[i] > 0) {
                float alto = Math.max(altoUtil * valores[i] / maximo, radio * 2);
                rect.set(izquierda, base - alto, izquierda + anchoBarra, base);
                pincelBarra.setColor(ContextCompat.getColor(getContext(),
                        i == indiceDestacado ? R.color.ambar : R.color.azul));
                canvas.drawRoundRect(rect, radio, radio, pincelBarra);
                canvas.drawText(String.valueOf(valores[i]), centro, base - alto - 4 * densidad, pincelValor);
            }

            canvas.drawText(etiquetas[i], centro, getHeight() - 2 * densidad, pincelTexto);
        }
    }
}
