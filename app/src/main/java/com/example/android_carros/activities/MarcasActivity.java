package com.example.android_carros.activities;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_carros.R;
import com.example.android_carros.adapters.MarcaAdapter;
import com.example.android_carros.api.ApiService;
import com.example.android_carros.api.RetrofitClient;
import com.example.android_carros.models.Marca;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarcasActivity extends AppCompatActivity {

    private ListView listMarcas;
    private TextView textoVazio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_marcas);

        listMarcas = findViewById(R.id.listMarcas);
        textoVazio = findViewById(R.id.textoVazio);

        listarMarcas();
    }

    private void listarMarcas() {

        ApiService apiService = RetrofitClient.getApiService();

        Call<List<Marca>> chamada = apiService.listarMarcas();

        chamada.enqueue(new Callback<List<Marca>>() {

            @Override
            public void onResponse(
                    Call<List<Marca>> call,
                    Response<List<Marca>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    List<Marca> marcas = response.body();

                    MarcaAdapter adapter = new MarcaAdapter(
                            MarcasActivity.this,
                            marcas
                    );

                    listMarcas.setAdapter(adapter);

                    listMarcas.setEmptyView(textoVazio);

                } else {

                    Toast.makeText(
                            MarcasActivity.this,
                            "Erro ao buscar marcas: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<Marca>> call, Throwable t) {

                Toast.makeText(
                        MarcasActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
