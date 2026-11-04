package com.example.android_carros.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
    private Spinner filtroMarca;

    private List<Marca> marcasDisponiveis = new ArrayList<>();
    private ModeloAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modelos);

        listModelos = findViewById(R.id.listModelos);
        filtroMarca = findViewById(R.id.filtroMarca);

        adapter = new ModeloAdapter(this, new ArrayList<>());
        listModelos.setAdapter(adapter);

        TextView textoVazio = findViewById(R.id.textoVazio);
        listModelos.setEmptyView(textoVazio);

        listModelos.setOnItemClickListener((parent, view, position, id) -> {
            Modelo modelo = adapter.getItem(position);

            if (modelo != null) {
                mostrarOpcoesModelo(modelo);
            }
        });

        TextView adicionarModelo = findViewById(R.id.adicionarModelo);
        adicionarModelo.setOnClickListener(view -> adicionarModelo());

        carregarMarcasFiltro();
    }

    private void carregarMarcasFiltro() {
        ApiService api = RetrofitClient.getApiService();

        api.listarMarcas().enqueue(new Callback<List<Marca>>() {
            @Override
            public void onResponse(Call<List<Marca>> call, Response<List<Marca>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    marcasDisponiveis.clear();
                    marcasDisponiveis.addAll(response.body());

                    List<String> nomes = new ArrayList<>();
                    nomes.add("Todas as marcas");

                    for (Marca marca : marcasDisponiveis) {
                        nomes.add(marca.getNome());
                    }

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                            ModelosActivity.this,
                            android.R.layout.simple_spinner_item,
                            nomes
                    );

                    spinnerAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                    );

                    filtroMarca.setAdapter(spinnerAdapter);

                    filtroMarca.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent,
                                                           View view,
                                                           int position,
                                                           long id) {
                                    if (position == 0) {
                                        listarModelos(null);
                                    } else {
                                        Long idMarca = marcasDisponiveis
                                                .get(position - 1).getId();

                                        listarModelos(idMarca);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            }
                    );
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

    private void listarModelos(Long idMarca) {
        ApiService api = RetrofitClient.getApiService();

        api.listarModelos(idMarca).enqueue(new Callback<List<Modelo>>() {
            @Override
            public void onResponse(Call<List<Modelo>> call, Response<List<Modelo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.clear();
                    adapter.addAll(response.body());
                    adapter.notifyDataSetChanged();
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

    private void atualizarListaModelos() {
        int posicao = filtroMarca.getSelectedItemPosition();

        if (posicao <= 0) {
            listarModelos(null);
        } else {
            listarModelos(marcasDisponiveis.get(posicao - 1).getId());
        }
    }

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

    private void editarModelo(Modelo modelo) {
        listarMarcas(() -> {

            EditText campoNome = new EditText(this);
            campoNome.setHint("Nome do modelo");
            campoNome.setText(modelo.getNome());

            Spinner spinnerTipo = criarSpinnerTipo();

            for (int i = 0; i < spinnerTipo.getCount(); i++) {
                if (spinnerTipo.getItemAtPosition(i).toString().equals(modelo.getTipo())) {
                    spinnerTipo.setSelection(i);
                    break;
                }
            }

            Spinner spinnerMarca = criarSpinnerMarca();

            Long idMarca = modelo.getIdMarca();

            if (idMarca == null && modelo.getMarca() != null) {
                idMarca = modelo.getMarca().getId();
            }

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
                                marca
                        );
                        ApiService api = RetrofitClient.getApiService();

                        api.atualizarModelo(modelo.getId(), atualizado)
                                .enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        if (response.isSuccessful()) {
                                            atualizarListaModelos();

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

    private void confirmarExclusao(Modelo modelo) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir modelo")
                .setMessage("Deseja realmente excluir " + modelo.getNome() + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> excluirModelo(modelo))
                .show();
    }

    private void excluirModelo(Modelo modelo) {
        ApiService api = RetrofitClient.getApiService();

        api.excluirModelo(modelo.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    atualizarListaModelos();

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
                                    atualizarListaModelos();

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