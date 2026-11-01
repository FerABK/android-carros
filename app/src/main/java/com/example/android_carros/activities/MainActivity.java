package com.example.android_carros.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_carros.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        TextView menuMarcas = findViewById(R.id.menuMarcas);

        menuMarcas.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, MarcasActivity.class);
            startActivity(intent);
        });
    }
}
