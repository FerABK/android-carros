package com.example.android_carros.models;

public class Cidade {
    private Long id;
    private String nome;
    private String ddd;

    public Cidade(){}

    public Cidade (Long id, String nome, String ddd){
        this.id = id;
        this.nome = nome;
        this.ddd = ddd;
    }
    public Long getId(){return id;}
    public void setId(Long id){this.id = id;}
    public String getNome(){return nome;}
    public void setNome(String nome){this.nome = nome;}
    public String getDdd(){return ddd;}
    public void setDdd(String ddd){this.ddd = ddd;}
}
