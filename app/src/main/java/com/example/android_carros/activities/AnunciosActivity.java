package com.example.android_carros.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_carros.R;
import com.example.android_carros.adapters.AnuncioAdapter;
import com.example.android_carros.api.ApiService;
import com.example.android_carros.api.RetrofitClient;
import com.example.android_carros.models.Anuncio;
import com.example.android_carros.models.Cidade;
import com.example.android_carros.models.Modelo;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnunciosActivity extends AppCompatActivity {

    private ListView listAnuncios;
    private EditText pesquisaAnuncio;

    private List<Anuncio> todosOsAnuncios = new ArrayList<>();
    private List<Modelo> modelosDisponiveis = new ArrayList<>();
    private List<Cidade> cidadesDisponiveis = new ArrayList<>();

    private AnuncioAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);

        listAnuncios = findViewById(R.id.listAnuncios);
        pesquisaAnuncio = findViewById(R.id.pesquisaAnuncio);

        adapter = new AnuncioAdapter(this, new ArrayList<>());
        listAnuncios.setAdapter(adapter);

        TextView textoVazio = findViewById(R.id.textoVazio);
        listAnuncios.setEmptyView(textoVazio);

        listAnuncios.setOnItemClickListener((parent, view, position, id) -> {
            Anuncio anuncio = adapter.getItem(position);

            if (anuncio != null) {
                mostrarOpcoesAnuncio(anuncio);
            }
        });

        TextView adicionarAnuncio = findViewById(R.id.adicionarAnuncio);
        adicionarAnuncio.setOnClickListener(view -> adicionarAnuncio());

        pesquisaAnuncio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarAnuncios(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listarAnuncios();
    }

    private void listarAnuncios() {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.listarAnuncios(null, null, null, null, null, null)
                .enqueue(new Callback<List<Anuncio>>() {
                    @Override
                    public void onResponse(Call<List<Anuncio>> call, Response<List<Anuncio>> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            todosOsAnuncios.clear();
                            todosOsAnuncios.addAll(response.body());

                            filtrarAnuncios(pesquisaAnuncio.getText().toString());
                        } else {
                            Toast.makeText(AnunciosActivity.this,
                                    "Erro ao buscar anúncios: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Anuncio>> call, Throwable t) {
                        Toast.makeText(AnunciosActivity.this,
                                "Erro de conexão: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void mostrarOpcoesAnuncio(Anuncio anuncio) {
        String[] opcoes = {"Editar", "Excluir"};

        new AlertDialog.Builder(this)
                .setTitle(anuncio.getDescricao())
                .setItems(opcoes, (dialog, escolha) -> {
                    if (escolha == 0) {
                        editarAnuncio(anuncio);
                    } else {
                        confirmarExclusao(anuncio);
                    }
                })
                .show();
    }

    private void carregarDadosFormulario(Runnable aoCarregar) {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.listarModelos(null).enqueue(new Callback<List<Modelo>>() {
            @Override
            public void onResponse(Call<List<Modelo>> call, Response<List<Modelo>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    modelosDisponiveis.clear();
                    modelosDisponiveis.addAll(response.body());

                    apiService.listarCidades(null).enqueue(new Callback<List<Cidade>>() {
                        @Override
                        public void onResponse(Call<List<Cidade>> call, Response<List<Cidade>> response) {

                            if (response.isSuccessful() && response.body() != null) {
                                cidadesDisponiveis.clear();
                                cidadesDisponiveis.addAll(response.body());

                                aoCarregar.run();
                            } else {
                                Toast.makeText(AnunciosActivity.this,
                                        "Erro ao buscar cidades: " + response.code(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Cidade>> call, Throwable t) {
                            Toast.makeText(AnunciosActivity.this,
                                    "Erro ao buscar cidades: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    Toast.makeText(AnunciosActivity.this,
                            "Erro ao buscar modelos: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Modelo>> call, Throwable t) {
                Toast.makeText(AnunciosActivity.this,
                        "Erro ao buscar modelos: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private Spinner criarSpinnerModelo() {
        List<String> nomes = new ArrayList<>();

        for (Modelo modelo : modelosDisponiveis) {
            nomes.add(modelo.getNome());
        }

        Spinner spinner = new Spinner(this);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nomes
        );

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        return spinner;
    }

    private Spinner criarSpinnerCidade() {
        List<String> nomes = new ArrayList<>();

        for (Cidade cidade : cidadesDisponiveis) {
            nomes.add(cidade.getNome());
        }

        Spinner spinner = new Spinner(this);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nomes
        );

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        return spinner;
    }

    private void selecionarModelo(Spinner spinner, Long idModelo) {
        for (int i = 0; i < modelosDisponiveis.size(); i++) {
            if (modelosDisponiveis.get(i).getId().equals(idModelo)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void selecionarCidade(Spinner spinner, Long idCidade) {
        for (int i = 0; i < cidadesDisponiveis.size(); i++) {
            if (cidadesDisponiveis.get(i).getId().equals(idCidade)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void editarAnuncio(Anuncio anuncio) {
        carregarDadosFormulario(() -> {

            EditText campoDescricao = new EditText(this);
            campoDescricao.setHint("Descrição do anúncio");
            campoDescricao.setText(anuncio.getDescricao());

            Spinner spinnerModelo = criarSpinnerModelo();
            Spinner spinnerCidade = criarSpinnerCidade();

            Long idModelo = anuncio.getIdModelo();
            if (idModelo == null && anuncio.getModelo() != null) {
                idModelo = anuncio.getModelo().getId();
            }

            Long idCidade = anuncio.getIdCidade();
            if (idCidade == null && anuncio.getCidade() != null) {
                idCidade = anuncio.getCidade().getId();
            }

            selecionarModelo(spinnerModelo, idModelo);
            selecionarCidade(spinnerCidade, idCidade);

            EditText campoCor = new EditText(this);
            campoCor.setHint("Cor");
            campoCor.setText(anuncio.getCor());

            EditText campoValor = new EditText(this);
            campoValor.setHint("Valor");
            campoValor.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            campoValor.setText(String.valueOf(anuncio.getValor()));

            EditText campoAno = new EditText(this);
            campoAno.setHint("Ano");
            campoAno.setInputType(InputType.TYPE_CLASS_NUMBER);
            campoAno.setText(String.valueOf(anuncio.getAno()));

            EditText campoKm = new EditText(this);
            campoKm.setHint("Quilometragem");
            campoKm.setInputType(InputType.TYPE_CLASS_NUMBER);
            campoKm.setText(String.valueOf(anuncio.getKm()));

            LinearLayout campos = criarLayoutFormulario();

            campos.addView(spinnerModelo);
            campos.addView(spinnerCidade);
            campos.addView(campoDescricao);
            campos.addView(campoCor);
            campos.addView(campoValor);
            campos.addView(campoAno);
            campos.addView(campoKm);

            new AlertDialog.Builder(this)
                    .setTitle("Editar anúncio")
                    .setView(campos)
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Salvar", (dialog, which) -> {

                        if (!camposPreenchidos(spinnerModelo, spinnerCidade,
                                campoDescricao, campoCor, campoValor, campoAno, campoKm)) {
                            return;
                        }

                        Modelo modelo = modelosDisponiveis.get(
                                spinnerModelo.getSelectedItemPosition());

                        Cidade cidade = cidadesDisponiveis.get(
                                spinnerCidade.getSelectedItemPosition());

                        Anuncio anuncioAtualizado = new Anuncio(
                                anuncio.getId(),
                                modelo,
                                cidade,
                                campoDescricao.getText().toString().trim(),
                                campoCor.getText().toString().trim(),
                                Double.parseDouble(campoValor.getText().toString().trim()),
                                Integer.parseInt(campoAno.getText().toString().trim()),
                                Integer.parseInt(campoKm.getText().toString().trim()),
                                cidade.getId(),
                                modelo.getId()
                        );

                        atualizarAnuncio(anuncioAtualizado);
                    })
                    .show();
        });
    }

    private void adicionarAnuncio() {
        carregarDadosFormulario(() -> {

            EditText campoDescricao = new EditText(this);
            campoDescricao.setHint("Descrição do anúncio");

            Spinner spinnerModelo = criarSpinnerModelo();
            Spinner spinnerCidade = criarSpinnerCidade();

            EditText campoCor = new EditText(this);
            campoCor.setHint("Cor");

            EditText campoValor = new EditText(this);
            campoValor.setHint("Valor");
            campoValor.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

            EditText campoAno = new EditText(this);
            campoAno.setHint("Ano");
            campoAno.setInputType(InputType.TYPE_CLASS_NUMBER);

            EditText campoKm = new EditText(this);
            campoKm.setHint("Quilometragem");
            campoKm.setInputType(InputType.TYPE_CLASS_NUMBER);

            LinearLayout campos = criarLayoutFormulario();

            campos.addView(spinnerModelo);
            campos.addView(spinnerCidade);
            campos.addView(campoDescricao);
            campos.addView(campoCor);
            campos.addView(campoValor);
            campos.addView(campoAno);
            campos.addView(campoKm);

            new AlertDialog.Builder(this)
                    .setTitle("Adicionar anúncio")
                    .setView(campos)
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Cadastrar", (dialog, which) -> {

                        if (!camposPreenchidos(spinnerModelo, spinnerCidade,
                                campoDescricao, campoCor, campoValor, campoAno, campoKm)) {
                            return;
                        }

                        Modelo modelo = modelosDisponiveis.get(
                                spinnerModelo.getSelectedItemPosition());

                        Cidade cidade = cidadesDisponiveis.get(
                                spinnerCidade.getSelectedItemPosition());

                        Anuncio novoAnuncio = new Anuncio(
                                null,
                                modelo,
                                cidade,
                                campoDescricao.getText().toString().trim(),
                                campoCor.getText().toString().trim(),
                                Double.parseDouble(campoValor.getText().toString().trim()),
                                Integer.parseInt(campoAno.getText().toString().trim()),
                                Integer.parseInt(campoKm.getText().toString().trim()),
                                cidade.getId(),
                                modelo.getId()
                        );

                        cadastrarAnuncio(novoAnuncio);
                    })
                    .show();
        });
    }

    private LinearLayout criarLayoutFormulario() {
        LinearLayout campos = new LinearLayout(this);
        campos.setOrientation(LinearLayout.VERTICAL);
        campos.setPadding(50, 10, 50, 0);

        return campos;
    }

    private boolean camposPreenchidos(Spinner modelo, Spinner cidade,
                                      EditText descricao, EditText cor,
                                      EditText valor, EditText ano, EditText km) {

        if (modelosDisponiveis.isEmpty() || cidadesDisponiveis.isEmpty()) {
            Toast.makeText(this, "Cadastre pelo menos um modelo e uma cidade.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (descricao.getText().toString().trim().isEmpty() ||
                cor.getText().toString().trim().isEmpty() ||
                valor.getText().toString().trim().isEmpty() ||
                ano.getText().toString().trim().isEmpty() ||
                km.getText().toString().trim().isEmpty()) {

            Toast.makeText(this, "Preencha todos os campos.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            double valorNumerico = Double.parseDouble(
                    valor.getText().toString().trim());

            if (valorNumerico <= 0) {
                Toast.makeText(this, "Informe um valor maior que zero.",
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            Integer.parseInt(ano.getText().toString().trim());
            Integer.parseInt(km.getText().toString().trim());

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Informe valores numéricos válidos.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void cadastrarAnuncio(Anuncio anuncio) {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.cadastrarAnuncio(anuncio).enqueue(new Callback<Anuncio>() {
            @Override
            public void onResponse(Call<Anuncio> call, Response<Anuncio> response) {

                if (response.isSuccessful()) {
                    listarAnuncios();

                    Toast.makeText(AnunciosActivity.this,
                            "Anúncio cadastrado!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AnunciosActivity.this,
                            "Erro ao cadastrar: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Anuncio> call, Throwable t) {
                Toast.makeText(AnunciosActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void atualizarAnuncio(Anuncio anuncio) {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.atualizarAnuncio(anuncio.getId(), anuncio).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                if (response.isSuccessful()) {
                    listarAnuncios();

                    Toast.makeText(AnunciosActivity.this,
                            "Anúncio atualizado!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AnunciosActivity.this,
                            "Erro ao atualizar: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AnunciosActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmarExclusao(Anuncio anuncio) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir anúncio")
                .setMessage("Deseja realmente excluir " + anuncio.getDescricao() + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> excluirAnuncio(anuncio))
                .show();
    }

    private void excluirAnuncio(Anuncio anuncio) {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.excluirAnuncio(anuncio.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                if (response.isSuccessful()) {
                    listarAnuncios();

                    Toast.makeText(AnunciosActivity.this,
                            "Anúncio excluído!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AnunciosActivity.this,
                            "Não foi possível excluir. Código: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AnunciosActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    private void filtrarAnuncios(String texto) {
        List<Anuncio> anunciosFiltrados = new ArrayList<>();

        for (Anuncio anuncio : todosOsAnuncios) {
            if (anuncio.getDescricao() != null &&
                    anuncio.getDescricao().toLowerCase().contains(texto.toLowerCase())) {

                anunciosFiltrados.add(anuncio);
            }
        }

        adapter.clear();
        adapter.addAll(anunciosFiltrados);
    }
}