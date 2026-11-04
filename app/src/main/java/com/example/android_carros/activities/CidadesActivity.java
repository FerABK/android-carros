package com.example.android_carros.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_carros.R;
import com.example.android_carros.adapters.CidadeAdapter;
import com.example.android_carros.api.ApiService;
import com.example.android_carros.api.RetrofitClient;
import com.example.android_carros.models.Cidade;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CidadesActivity extends AppCompatActivity {

    private ListView listCidades;
    private EditText pesquisaCidade;
    private CidadeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cidades);

        listCidades = findViewById(R.id.listCidades);
        pesquisaCidade = findViewById(R.id.pesquisaCidade);

        adapter = new CidadeAdapter(this, new ArrayList<>());
        listCidades.setAdapter(adapter);

        TextView textoVazio = findViewById(R.id.textoVazio);
        listCidades.setEmptyView(textoVazio);

        listCidades.setOnItemClickListener((parent, view, position, id) -> {
            Cidade cidade = adapter.getItem(position);

            if (cidade != null) {
                mostrarOpcoesCidade(cidade);
            }
        });

        TextView adicionarCidade = findViewById(R.id.adicionarCidade);
        adicionarCidade.setOnClickListener(view -> adicionarCidade());

        pesquisaCidade.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listarCidades(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listarCidades(null);
    }

    private void listarCidades(String nome) {
        ApiService apiService = RetrofitClient.getApiService();

        if (nome == null || nome.trim().isEmpty()) {
            nome = null;
        }

        apiService.listarCidades(nome).enqueue(new Callback<List<Cidade>>() {
            @Override
            public void onResponse(Call<List<Cidade>> call, Response<List<Cidade>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.clear();
                    adapter.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CidadesActivity.this,
                            "Erro ao buscar cidades: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Cidade>> call, Throwable t) {
                Toast.makeText(CidadesActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void atualizarListaCidades() {
        listarCidades(pesquisaCidade.getText().toString().trim());
    }

    private void mostrarOpcoesCidade(Cidade cidade) {
        String[] opcoes = {"Editar", "Excluir"};

        new AlertDialog.Builder(this)
                .setTitle(cidade.getNome())
                .setItems(opcoes, (dialog, escolha) -> {
                    if (escolha == 0) {
                        editarCidade(cidade);
                    } else {
                        confirmarExclusao(cidade);
                    }
                })
                .show();
    }

    private void editarCidade(Cidade cidade) {
        EditText campoNome = new EditText(this);
        campoNome.setHint("Nome da cidade");
        campoNome.setText(cidade.getNome());

        EditText campoDdd = new EditText(this);
        campoDdd.setHint("DDD");
        campoDdd.setInputType(InputType.TYPE_CLASS_NUMBER);
        campoDdd.setText(cidade.getDdd());

        LinearLayout campos = new LinearLayout(this);
        campos.setOrientation(LinearLayout.VERTICAL);
        campos.setPadding(50, 10, 50, 0);
        campos.addView(campoNome);
        campos.addView(campoDdd);

        new AlertDialog.Builder(this)
                .setTitle("Editar cidade")
                .setView(campos)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {

                    String novoNome = campoNome.getText().toString().trim();
                    String novoDdd = campoDdd.getText().toString().trim();

                    if (novoNome.isEmpty() || novoDdd.isEmpty()) {
                        Toast.makeText(this, "Preencha o nome e o DDD",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Cidade cidadeAtualizada = new Cidade(
                            cidade.getId(),
                            novoNome,
                            novoDdd
                    );

                    ApiService apiService = RetrofitClient.getApiService();

                    apiService.atualizarCidade(cidade.getId(), cidadeAtualizada)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        atualizarListaCidades();

                                        Toast.makeText(CidadesActivity.this,
                                                "Cidade atualizada!",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(CidadesActivity.this,
                                                "Erro ao atualizar: " + response.code(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(CidadesActivity.this,
                                            "Erro de conexão: " + t.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .show();
    }

    private void confirmarExclusao(Cidade cidade) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir cidade")
                .setMessage("Deseja realmente excluir " + cidade.getNome() + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> excluirCidade(cidade))
                .show();
    }

    private void excluirCidade(Cidade cidade) {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.excluirCidade(cidade.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    atualizarListaCidades();

                    Toast.makeText(CidadesActivity.this,
                            "Cidade excluída!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CidadesActivity.this,
                            "Não foi possível excluir. Código: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CidadesActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void adicionarCidade() {
        EditText campoNome = new EditText(this);
        campoNome.setHint("Nome da cidade");

        EditText campoDdd = new EditText(this);
        campoDdd.setHint("DDD");
        campoDdd.setInputType(InputType.TYPE_CLASS_NUMBER);

        LinearLayout campos = new LinearLayout(this);
        campos.setOrientation(LinearLayout.VERTICAL);
        campos.setPadding(50, 10, 50, 0);
        campos.addView(campoNome);
        campos.addView(campoDdd);

        new AlertDialog.Builder(this)
                .setTitle("Adicionar cidade")
                .setView(campos)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {

                    String nome = campoNome.getText().toString().trim();
                    String ddd = campoDdd.getText().toString().trim();

                    if (nome.isEmpty() || ddd.isEmpty()) {
                        Toast.makeText(this, "Preencha o nome e o DDD",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Cidade novaCidade = new Cidade(null, nome, ddd);

                    ApiService apiService = RetrofitClient.getApiService();

                    apiService.cadastrarCidade(novaCidade).enqueue(new Callback<Cidade>() {
                        @Override
                        public void onResponse(Call<Cidade> call, Response<Cidade> response) {
                            if (response.isSuccessful()) {
                                atualizarListaCidades();

                                Toast.makeText(CidadesActivity.this,
                                        "Cidade cadastrada!",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CidadesActivity.this,
                                        "Erro ao cadastrar: " + response.code(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Cidade> call, Throwable t) {
                            Toast.makeText(CidadesActivity.this,
                                    "Erro de conexão: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .show();
    }
}