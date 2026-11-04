package com.example.android_carros.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
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
    private Spinner filtroModelo;
    private Spinner filtroTipo;
    private EditText anoInicial;
    private EditText anoFinal;
    private EditText valorMinimo;
    private EditText valorMaximo;

    private List<Modelo> modelosFiltro = new ArrayList<>();
    private List<Modelo> modelosDisponiveis = new ArrayList<>();
    private List<Cidade> cidadesDisponiveis = new ArrayList<>();
    private List<Anuncio> todosOsAnuncios = new ArrayList<>();

    private AnuncioAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);

        listAnuncios = findViewById(R.id.listAnuncios);
        filtroModelo = findViewById(R.id.filtroModelo);
        filtroTipo = findViewById(R.id.filtroTipo);
        anoInicial = findViewById(R.id.anoInicial);
        anoFinal = findViewById(R.id.anoFinal);
        valorMinimo = findViewById(R.id.valorMinimo);
        valorMaximo = findViewById(R.id.valorMaximo);

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

        TextView aplicarFiltros = findViewById(R.id.aplicarFiltros);
        aplicarFiltros.setOnClickListener(view -> aplicarFiltros());

        configurarFiltroTipo();
        carregarModelosFiltro();
    }

    private void configurarFiltroTipo() {
        String[] tipos = {
                "Todos os tipos",
                "SEDAN",
                "HATCH",
                "SUV",
                "CAMIONETE"
        };

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tipos
        );

        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        filtroTipo.setAdapter(spinnerAdapter);
    }

    private void carregarModelosFiltro() {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.listarModelos(null).enqueue(new Callback<List<Modelo>>() {
            @Override
            public void onResponse(Call<List<Modelo>> call,
                                   Response<List<Modelo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    modelosFiltro.clear();
                    modelosFiltro.addAll(response.body());

                    List<String> nomes = new ArrayList<>();
                    nomes.add("Todos os modelos");

                    for (Modelo modelo : modelosFiltro) {
                        nomes.add(modelo.getNome());
                    }

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                            AnunciosActivity.this,
                            android.R.layout.simple_spinner_item,
                            nomes
                    );

                    spinnerAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                    );

                    filtroModelo.setAdapter(spinnerAdapter);
                    listarAnuncios(null, null, null, null, null, null);
                } else {
                    Toast.makeText(AnunciosActivity.this,
                            "Erro ao buscar modelos: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Modelo>> call, Throwable t) {
                Toast.makeText(AnunciosActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void aplicarFiltros() {
        Long idModelo = null;
        String tipo = null;
        Integer anoDe = null;
        Integer anoAte = null;
        Double valorDe = null;
        Double valorAte = null;

        int posicaoModelo = filtroModelo.getSelectedItemPosition();

        if (posicaoModelo > 0) {
            idModelo = modelosFiltro.get(posicaoModelo - 1).getId();
        }

        int posicaoTipo = filtroTipo.getSelectedItemPosition();

        if (posicaoTipo > 0) {
            tipo = filtroTipo.getSelectedItem().toString();
        }

        String textoAnoInicial = anoInicial.getText().toString().trim();
        String textoAnoFinal = anoFinal.getText().toString().trim();
        String textoValorMinimo = valorMinimo.getText().toString().trim();
        String textoValorMaximo = valorMaximo.getText().toString().trim();

        try {
            if (!textoAnoInicial.isEmpty()) {
                anoDe = Integer.parseInt(textoAnoInicial);
            }

            if (!textoAnoFinal.isEmpty()) {
                anoAte = Integer.parseInt(textoAnoFinal);
            }

            if (!textoValorMinimo.isEmpty()) {
                valorDe = Double.parseDouble(textoValorMinimo);
            }

            if (!textoValorMaximo.isEmpty()) {
                valorAte = Double.parseDouble(textoValorMaximo);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Informe valores válidos nos filtros.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (anoDe != null && anoAte != null && anoDe > anoAte) {
            Toast.makeText(this,
                    "O ano inicial não pode ser maior que o ano final.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (valorDe != null && valorAte != null && valorDe > valorAte) {
            Toast.makeText(this,
                    "O valor mínimo não pode ser maior que o máximo.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        listarAnuncios(idModelo, tipo, anoDe, anoAte, valorDe, valorAte);
    }

    private void listarAnuncios(Long idModelo, String tipo,
                                Integer anoDe, Integer anoAte,
                                Double valorDe, Double valorAte) {

        ApiService apiService = RetrofitClient.getApiService();

        apiService.listarAnuncios(null, null, null, null, null, null)
                .enqueue(new Callback<List<Anuncio>>() {
                    @Override
                    public void onResponse(Call<List<Anuncio>> call,
                                           Response<List<Anuncio>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            todosOsAnuncios.clear();
                            todosOsAnuncios.addAll(response.body());

                            List<Anuncio> anunciosFiltrados = new ArrayList<>();

                            for (Anuncio anuncio : todosOsAnuncios) {
                                Long modeloId = anuncio.getIdModelo();

                                if (modeloId == null && anuncio.getModelo() != null) {
                                    modeloId = anuncio.getModelo().getId();
                                }

                                if (idModelo != null && !idModelo.equals(modeloId)) {
                                    continue;
                                }

                                if (tipo != null &&
                                        (anuncio.getModelo() == null ||
                                                !tipo.equals(anuncio.getModelo().getTipo()))) {
                                    continue;
                                }

                                if (anoDe != null && anuncio.getAno() < anoDe) {
                                    continue;
                                }

                                if (anoAte != null && anuncio.getAno() > anoAte) {
                                    continue;
                                }

                                if (valorDe != null && anuncio.getValor() < valorDe) {
                                    continue;
                                }

                                if (valorAte != null && anuncio.getValor() > valorAte) {
                                    continue;
                                }

                                anunciosFiltrados.add(anuncio);
                            }

                            adapter.clear();
                            adapter.addAll(anunciosFiltrados);
                            adapter.notifyDataSetChanged();

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

    private void atualizarListaAnuncios() {
        aplicarFiltros();
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
            public void onResponse(Call<List<Modelo>> call,
                                   Response<List<Modelo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    modelosDisponiveis.clear();
                    modelosDisponiveis.addAll(response.body());

                    apiService.listarCidades(null).enqueue(new Callback<List<Cidade>>() {
                        @Override
                        public void onResponse(Call<List<Cidade>> call,
                                               Response<List<Cidade>> response) {
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

        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

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

        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

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
            campoValor.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_FLAG_DECIMAL);
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
            campoValor.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_FLAG_DECIMAL);

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
                    atualizarListaAnuncios();

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
                    atualizarListaAnuncios();

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
                    atualizarListaAnuncios();

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
}
