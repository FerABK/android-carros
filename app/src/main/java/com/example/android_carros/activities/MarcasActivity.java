package com.example.android_carros.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
    private EditText pesquisaMarca;

    private List<Marca> todasAsMarcas = new ArrayList<>();
    private MarcaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marcas);

        listMarcas = findViewById(R.id.listMarcas);
        pesquisaMarca = findViewById(R.id.pesquisaMarca);

        adapter = new MarcaAdapter(this, new ArrayList<>());
        listMarcas.setAdapter(adapter);

        TextView textoVazio = findViewById(R.id.textoVazio);
        listMarcas.setEmptyView(textoVazio);

        // Ao clicar em uma marca, mostra as opções de editar ou excluir.
        listMarcas.setOnItemClickListener((parent, view, position, id) -> {
            Marca marca = adapter.getItem(position);

            if (marca != null) {
                mostrarOpcoesMarca(marca);
            }
        });

        // Abre a janela para cadastrar uma marca.
        TextView adicionarMarca = findViewById(R.id.adicionarMarca);
        adicionarMarca.setOnClickListener(view -> adicionarMarca());

        // Atualiza a lista conforme o usuário digita.
        pesquisaMarca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarMarcas(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listarMarcas();
    }

    // Busca todas as marcas no servidor.
    private void listarMarcas() {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.listarMarcas().enqueue(new Callback<List<Marca>>() {
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

    // Mostra somente as marcas que correspondem à pesquisa.
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

    // Mostra as opções disponíveis para uma marca.
    private void mostrarOpcoesMarca(Marca marca) {
        String[] opcoes = {"Editar", "Excluir"};

        new AlertDialog.Builder(this)
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

    // Abre a janela para editar uma marca.
    private void editarMarca(Marca marca) {
        EditText campoNome = new EditText(this);
        campoNome.setText(marca.getNome());

        new AlertDialog.Builder(this)
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

                    Marca marcaAtualizada = new Marca(marca.getId(), novoNome);

                    ApiService apiService = RetrofitClient.getApiService();

                    apiService.atualizarMarca(marca.getId(), marcaAtualizada)
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

    // Pede confirmação antes de excluir.
    private void confirmarExclusao(Marca marca) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir marca")
                .setMessage("Deseja realmente excluir " + marca.getNome() + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> excluirMarca(marca))
                .show();
    }

    // Envia a exclusão da marca para o servidor.
    private void excluirMarca(Marca marca) {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.excluirMarca(marca.getId()).enqueue(new Callback<Void>() {
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

    // Abre a janela para cadastrar uma marca.
    private void adicionarMarca() {
        EditText campoNome = new EditText(this);
        campoNome.setHint("Nome da marca");

        new AlertDialog.Builder(this)
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

                    apiService.cadastrarMarca(novaMarca).enqueue(new Callback<Marca>() {
                        @Override
                        public void onResponse(Call<Marca> call, Response<Marca> response) {

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