
package com.example.android_carros.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_carros.R;
import com.example.android_carros.adapters.ModeloAdapter;
import com.example.android_carros.api.ApiService;
import com.example.android_carros.api.RetrofitClient;
import com.example.android_carros.models.Marca;
import com.example.android_carros.models.Modelo;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModelosActivity extends AppCompatActivity {

    private ListView listModelos;
    private EditText pesquisaModelo;

    private List<Marca> marcasDisponiveis = new ArrayList<>();
    private List<Modelo> todosOsModelos = new ArrayList<>();

    private ModeloAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modelos);

        listModelos = findViewById(R.id.listModelos);
        pesquisaModelo = findViewById(R.id.pesquisaModelo);

        adapter = new ModeloAdapter(this, new ArrayList<>());
        listModelos.setAdapter(adapter);

        TextView textoVazio = findViewById(R.id.textoVazio);
        listModelos.setEmptyView(textoVazio);

        // Ao clicar em um modelo, permite editar ou excluir.
        listModelos.setOnItemClickListener((parent, view, position, id) -> {
            Modelo modelo = adapter.getItem(position);
            mostrarOpcoesModelo(modelo);
        });

        // Abre o cadastro.
        TextView adicionarModelo = findViewById(R.id.adicionarModelo);
        adicionarModelo.setOnClickListener(view -> adicionarModelo());

        // Filtra os modelos enquanto o usuário digita.
        pesquisaModelo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarModelos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listarModelos();
    }

    // Busca os modelos no servidor.
    private void listarModelos() {
        ApiService api = RetrofitClient.getApiService();

        api.listarModelos(null).enqueue(new Callback<List<Modelo>>() {
            @Override
            public void onResponse(Call<List<Modelo>> call, Response<List<Modelo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todosOsModelos.clear();
                    todosOsModelos.addAll(response.body());

                    filtrarModelos(pesquisaModelo.getText().toString());
                } else {
                    Toast.makeText(ModelosActivity.this,
                            "Erro ao buscar modelos: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Modelo>> call, Throwable t) {
                Toast.makeText(ModelosActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Mostra somente os modelos que correspondem à pesquisa.
    private void filtrarModelos(String texto) {
        List<Modelo> filtrados = new ArrayList<>();

        for (Modelo modelo : todosOsModelos) {
            if (modelo.getNome() != null &&
                    modelo.getNome().toLowerCase().contains(texto.toLowerCase())) {
                filtrados.add(modelo);
            }
        }

        adapter.clear();
        adapter.addAll(filtrados);
    }

    // Mostra as opções de edição e exclusão.
    private void mostrarOpcoesModelo(Modelo modelo) {
        String[] opcoes = {"Editar", "Excluir"};

        new AlertDialog.Builder(this)
                .setTitle(modelo.getNome())
                .setItems(opcoes, (dialog, escolha) -> {
                    if (escolha == 0) {
                        editarModelo(modelo);
                    } else {
                        confirmarExclusao(modelo);
                    }
                })
                .show();
    }

    // Abre o formulário de edição.
    private void editarModelo(Modelo modelo) {
        listarMarcas(() -> {

            EditText campoNome = new EditText(this);
            campoNome.setHint("Nome do modelo");
            campoNome.setText(modelo.getNome());

            Spinner spinnerTipo = criarSpinnerTipo();

            // Seleciona o tipo atual.
            for (int i = 0; i < spinnerTipo.getCount(); i++) {
                if (spinnerTipo.getItemAtPosition(i).toString().equals(modelo.getTipo())) {
                    spinnerTipo.setSelection(i);
                    break;
                }
            }

            Spinner spinnerMarca = criarSpinnerMarca();

            // Descobre o ID da marca atual.
            Long idMarca = modelo.getIdMarca();

            if (idMarca == null && modelo.getMarca() != null) {
                idMarca = modelo.getMarca().getId();
            }

            // Seleciona a marca atual.
            for (int i = 0; i < marcasDisponiveis.size(); i++) {
                if (marcasDisponiveis.get(i).getId().equals(idMarca)) {
                    spinnerMarca.setSelection(i);
                    break;
                }
            }

            LinearLayout campos = new LinearLayout(this);
            campos.setOrientation(LinearLayout.VERTICAL);
            campos.setPadding(50, 10, 50, 0);

            campos.addView(campoNome);
            campos.addView(spinnerTipo);
            campos.addView(spinnerMarca);

            new AlertDialog.Builder(this)
                    .setTitle("Editar modelo")
                    .setView(campos)
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Salvar", (dialog, which) -> {

                        String nome = campoNome.getText().toString().trim();
                        String tipo = spinnerTipo.getSelectedItem().toString();

                        if (nome.isEmpty()) {
                            Toast.makeText(this, "Preencha o nome do modelo",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (marcasDisponiveis.isEmpty()) {
                            Toast.makeText(this, "Cadastre uma marca primeiro",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Marca marca = marcasDisponiveis.get(
                                spinnerMarca.getSelectedItemPosition()
                        );

                        Modelo atualizado = new Modelo(
                                modelo.getId(),
                                nome,
                                tipo,
                                marca.getId(),
                                null
                        );

                        ApiService api = RetrofitClient.getApiService();

                        api.atualizarModelo(modelo.getId(), atualizado)
                                .enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        if (response.isSuccessful()) {
                                            listarModelos();
                                            Toast.makeText(ModelosActivity.this,
                                                    "Modelo atualizado!",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ModelosActivity.this,
                                                    "Erro ao atualizar: " + response.code(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        Toast.makeText(ModelosActivity.this,
                                                "Erro de conexão: " + t.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .show();
        });
    }

    // Pergunta antes de excluir.
    private void confirmarExclusao(Modelo modelo) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir modelo")
                .setMessage("Deseja realmente excluir " + modelo.getNome() + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> excluirModelo(modelo))
                .show();
    }

    // Exclui o modelo no servidor.
    private void excluirModelo(Modelo modelo) {
        ApiService api = RetrofitClient.getApiService();

        api.excluirModelo(modelo.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    listarModelos();
                    Toast.makeText(ModelosActivity.this,
                            "Modelo excluído!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ModelosActivity.this,
                            "Não foi possível excluir: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ModelosActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Abre o formulário de cadastro.
    private void adicionarModelo() {
        listarMarcas(() -> {

            EditText campoNome = new EditText(this);
            campoNome.setHint("Nome do modelo");

            Spinner spinnerTipo = criarSpinnerTipo();
            Spinner spinnerMarca = criarSpinnerMarca();

            LinearLayout campos = new LinearLayout(this);
            campos.setOrientation(LinearLayout.VERTICAL);
            campos.setPadding(50, 10, 50, 0);

            campos.addView(campoNome);
            campos.addView(spinnerTipo);
            campos.addView(spinnerMarca);

            new AlertDialog.Builder(this)
                    .setTitle("Adicionar modelo")
                    .setView(campos)
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Salvar", (dialog, which) -> {

                        String nome = campoNome.getText().toString().trim();
                        String tipo = spinnerTipo.getSelectedItem().toString();

                        if (nome.isEmpty()) {
                            Toast.makeText(this, "Preencha o nome do modelo",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (marcasDisponiveis.isEmpty()) {
                            Toast.makeText(this, "Cadastre uma marca primeiro",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Marca marca = marcasDisponiveis.get(
                                spinnerMarca.getSelectedItemPosition()
                        );

                        Modelo novoModelo = new Modelo(
                                null,
                                nome,
                                tipo,
                                marca.getId(),
                                null
                        );

                        ApiService api = RetrofitClient.getApiService();

                        api.cadastrarModelo(novoModelo).enqueue(new Callback<Modelo>() {
                            @Override
                            public void onResponse(Call<Modelo> call, Response<Modelo> response) {
                                if (response.isSuccessful()) {
                                    listarModelos();
                                    Toast.makeText(ModelosActivity.this,
                                            "Modelo cadastrado!",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ModelosActivity.this,
                                            "Erro ao cadastrar: " + response.code(),
                                            Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Modelo> call, Throwable t) {
                                Toast.makeText(ModelosActivity.this,
                                        "Erro de conexão: " + t.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .show();
        });
    }

    // Busca as marcas antes de abrir o formulário.
    private void listarMarcas(Runnable aoCarregar) {
        ApiService api = RetrofitClient.getApiService();

        api.listarMarcas().enqueue(new Callback<List<Marca>>() {
            @Override
            public void onResponse(Call<List<Marca>> call, Response<List<Marca>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    marcasDisponiveis.clear();
                    marcasDisponiveis.addAll(response.body());
                    aoCarregar.run();
                } else {
                    Toast.makeText(ModelosActivity.this,
                            "Erro ao buscar marcas: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Marca>> call, Throwable t) {
                Toast.makeText(ModelosActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Cria o Spinner com as marcas cadastradas.
    private Spinner criarSpinnerMarca() {
        Spinner spinner = new Spinner(this);
        List<String> nomes = new ArrayList<>();

        for (Marca marca : marcasDisponiveis) {
            nomes.add(marca.getNome());
        }

        ArrayAdapter<String> adapterMarcas = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nomes
        );

        adapterMarcas.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapterMarcas);

        return spinner;
    }

    // Cria o Spinner com os tipos permitidos.
    private Spinner criarSpinnerTipo() {
        Spinner spinner = new Spinner(this);

        String[] tipos = {"SEDAN", "HATCH", "SUV", "CAMIONETE"};

        ArrayAdapter<String> adapterTipos = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tipos
        );

        adapterTipos.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapterTipos);

        return spinner;
    }
}
