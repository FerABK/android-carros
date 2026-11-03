package com.example.android_carros.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.app.AlertDialog;


import androidx.appcompat.app.AppCompatActivity;

import com.example.android_carros.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (!temInternet()) {
            new AlertDialog.Builder(this)
                    .setTitle("Sem conexão com a internet")
                    .setMessage("Este aplicativo precisa de internet para funcionar. Conecte-se e tente novamente.")
                    .setPositiveButton("OK", null)
                    .show();
        }

        TextView menuMarcas = findViewById(R.id.menuMarcas);
        TextView menuModelos = findViewById(R.id.menuModelos);
        TextView menuCidades = findViewById(R.id.menuCidades);
        TextView menuAnuncios = findViewById(R.id.menuAnuncios);

        menuMarcas.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, MarcasActivity.class);
            startActivity(intent);
        });

        menuModelos.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ModelosActivity.class);
            startActivity(intent);
        });

        menuCidades.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CidadesActivity.class);
            startActivity(intent);
        });

        menuAnuncios.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AnunciosActivity.class);
            startActivity(intent);
        });
    }

    private boolean temInternet() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        Network network = connectivityManager.getActiveNetwork();

        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities =
                connectivityManager.getNetworkCapabilities(network);

        return capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
}
