package com.epet30.mochila;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Pantalla de presentación con el logo. Se muestra solo la primera vez:
 * después se recuerda con SharedPreferences y la app abre directo en Inicio.
 */
public class BienvenidaActivity extends AppCompatActivity {

    public static final String PREFERENCIAS = "mochila_preferencias";
    private static final String CLAVE_BIENVENIDA_VISTA = "bienvenida_vista";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferencias = getSharedPreferences(PREFERENCIAS, MODE_PRIVATE);
        if (preferencias.getBoolean(CLAVE_BIENVENIDA_VISTA, false)) {
            abrirInicio();
            return;
        }

        setContentView(R.layout.activity_bienvenida);

        // Animaciones de entrada: el logo rebota y los textos suben
        View marcoLogo = findViewById(R.id.marcoLogo);
        marcoLogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.logo_entrada));
        View contenido = findViewById(R.id.contenidoBienvenida);
        contenido.startAnimation(AnimationUtils.loadAnimation(this, R.anim.aparecer));

        Button btnEmpezar = findViewById(R.id.btnEmpezar);
        btnEmpezar.setOnClickListener(v -> {
            preferencias.edit().putBoolean(CLAVE_BIENVENIDA_VISTA, true).apply();
            abrirInicio();
        });
    }

    private void abrirInicio() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
