package com.example.android_carros;

public class Modelo {
    private Long id;
    private String nome;
    private String tipo;
    private Long idMarca;
    private Marca marca;

    public Modelo(){}

    public Modelo(Long id, String nome, String tipo, Long idMarca, Marca marca){
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.idMarca = idMarca;
        this.marca = marca;
    }
}
