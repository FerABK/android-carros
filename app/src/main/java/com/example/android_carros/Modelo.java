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
    public Long getId(){return id;}
    public void setId(Long id){this.id=id;}
    public String getNome(){return nome;}
    public void setNome(String nome){this.nome = nome;}
    public String getTipo(){return tipo;}
    public void setTipo(String tipo){this.tipo = tipo;}
    public Long getIdMarca(){return idMarca;}
    public void setIdMarca(Long idMarca){this.idMarca=idMarca;}
    public Marca getMarca(){return marca;}
    public void setMarca(Marca marca){this.marca=marca;}

}
