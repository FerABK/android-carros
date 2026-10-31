package com.example.android_carros;

public class Anuncio {
    private Long id;
    private Modelo modelo;
    private Cidade cidade;
    private String descricao;
    private String cor;
    private Double valor;
    private Integer ano;
    private Integer km;
    private Long idCidade;
    private Long idModelo;

    public Anuncio(){}

    public Anuncio(Long id, Modelo modelo, Cidade cidade, String descricao, String cor,
                   Double valor, Integer ano, Integer km, Long idCidade, Long idModelo) {
        this.id = id;
        this.modelo = modelo;
        this.cidade = cidade;
        this.descricao = descricao;
        this.cor = cor;
        this.valor = valor;
        this.ano = ano;
        this.km = km;
        this.idCidade = idCidade;
        this.idModelo = idModelo;
    }
}
