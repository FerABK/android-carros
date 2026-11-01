package com.example.android_carros.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_carros.R;
import com.example.android_carros.adapters.MarcaAdapter;
import com.example.android_carros.api.ApiService;
import com.example.android_carros.api.RetrofitClient;
import com.example.android_carros.models.Marca;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarcasActivity extends AppCompatActivity {

    private ListView listMarcas;
    private TextView textoVazio;
    private EditText pesquisaMarca;

    private List<Marca> todasAsMarcas;
    private MarcaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marcas);

        listMarcas = findViewById(R.id.listMarcas);
        textoVazio = findViewById(R.id.textoVazio);
        pesquisaMarca = findViewById(R.id.pesquisaMarca);

        todasAsMarcas = new ArrayList<>();

        adapter = new MarcaAdapter(this, new ArrayList<>());
        listMarcas.setAdapter(adapter);
        listMarcas.setEmptyView(textoVazio);

        listMarcas.setOnItemClickListener((parent, view, position, id) -> {
            Marca marca = adapter.getItem(position);

            if (marca != null) {
                mostrarOpcoesMarca(marca);
            }
        });

        TextView adicionarMarca = findViewById(R.id.adicionarMarca);

        adicionarMarca.setOnClickListener(view -> adicionarMarca());

        pesquisaMarca.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarMarcas(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        listarMarcas();
    }

    private void listarMarcas() {
        ApiService apiService = RetrofitClient.getApiService();

        Call<List<Marca>> chamada = apiService.listarMarcas();

        chamada.enqueue(new Callback<List<Marca>>() {
            @Override
            public void onResponse(Call<List<Marca>> call, Response<List<Marca>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todasAsMarcas.clear();
                    todasAsMarcas.addAll(response.body());

                    filtrarMarcas(pesquisaMarca.getText().toString());
                } else {
                    Toast.makeText(MarcasActivity.this,
                            "Erro ao buscar marcas: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Marca>> call, Throwable t) {
                Toast.makeText(MarcasActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filtrarMarcas(String texto) {
        List<Marca> marcasFiltradas = new ArrayList<>();

        for (Marca marca : todasAsMarcas) {
            if (marca.getNome().toLowerCase().contains(texto.toLowerCase())) {
                marcasFiltradas.add(marca);
            }
        }

        adapter.clear();
        adapter.addAll(marcasFiltradas);
        adapter.notifyDataSetChanged();
    }
    private void mostrarOpcoesMarca(Marca marca) {
        String[] opcoes = {"Editar", "Excluir"};

        new android.app.AlertDialog.Builder(this)
                .setTitle(marca.getNome())
                .setItems(opcoes, (dialog, escolha) -> {
                    if (escolha == 0) {
                        editarMarca(marca);
                    } else {
                        confirmarExclusao(marca);
                    }
                })
                .show();
    }
    private void editarMarca(Marca marca) {
        EditText campoNome = new EditText(this);
        campoNome.setText(marca.getNome());

        new android.app.AlertDialog.Builder(this)
                .setTitle("Editar marca")
                .setView(campoNome)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {

                    String novoNome = campoNome.getText().toString().trim();

                    if (novoNome.isEmpty()) {
                        Toast.makeText(this, "Informe o nome da marca",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    marca.setNome(novoNome);

                    ApiService apiService = RetrofitClient.getApiService();

                    apiService.atualizarMarca(marca.getId(), marca)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        listarMarcas();

                                        Toast.makeText(MarcasActivity.this,
                                                "Marca atualizada!",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MarcasActivity.this,
                                                "Erro ao atualizar: " + response.code(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(MarcasActivity.this,
                                            "Erro de conexão: " + t.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .show();
    }
    private void confirmarExclusao(Marca marca) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Excluir marca")
                .setMessage("Deseja realmente excluir " + marca.getNome() + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    excluirMarca(marca);
                })
                .show();
    }
    private void excluirMarca(Marca marca) {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.excluirMarca(marca.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            listarMarcas();

                            Toast.makeText(MarcasActivity.this,
                                    "Marca excluída!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MarcasActivity.this,
                                    "Não foi possível excluir. Código: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(MarcasActivity.this,
                                "Erro de conexão: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void adicionarMarca() {
        EditText campoNome = new EditText(this);
        campoNome.setHint("Nome da marca");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Adicionar marca")
                .setView(campoNome)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {

                    String nome = campoNome.getText().toString().trim();

                    if (nome.isEmpty()) {
                        Toast.makeText(this, "Informe o nome da marca",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Marca novaMarca = new Marca(null, nome);

                    ApiService apiService = RetrofitClient.getApiService();

                    apiService.cadastrarMarca(novaMarca)
                            .enqueue(new Callback<Marca>() {
                                @Override
                                public void onResponse(Call<Marca> call,
                                                       Response<Marca> response) {
                                    if (response.isSuccessful()) {
                                        listarMarcas();

                                        Toast.makeText(MarcasActivity.this,
                                                "Marca cadastrada!",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MarcasActivity.this,
                                                "Erro ao cadastrar: " + response.code(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Marca> call, Throwable t) {
                                    Toast.makeText(MarcasActivity.this,
                                            "Erro de conexão: " + t.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .show();
    }
}

