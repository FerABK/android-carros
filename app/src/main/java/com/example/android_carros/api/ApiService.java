package com.example.android_carros.api;

import com.example.android_carros.models.Anuncio;
import com.example.android_carros.models.Marca;
import com.example.android_carros.models.Modelo;
import com.example.android_carros.models.Cidade;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {



    @GET("marcas")
    Call<List<Marca>> listarMarcas();
    @GET("marcas/{id}")
    Call<Marca> buscarMarca(@Path("id") Long id);
    @POST("marcas")
    Call<Marca> cadastrarMarca(@Body Marca marca);
    @PUT("marcas/{id}")
    Call<Void> atualizarMarca(@Path("id") Long id, @Body Marca marca);
    @DELETE("marcas/{id}")
    Call<Void> excluirMarca(@Path("id") Long id);

    @GET("modelos")
    Call<List<Modelo>> listarModelos(@Query("marca") Long idMarca);
    @GET("modelos/{id}")
    Call<Modelo> buscarModelo(@Path("id") Long id);
    @POST("modelos")
    Call<Modelo> cadastrarModelo(@Body Modelo modelo);
    @PUT("modelos/{id}")
    Call<Void> atualizarModelo(@Path("id") Long id, @Body Modelo modelo);
    @DELETE("modelos/{id}")
    Call<Void> excluirModelo(@Path("id") Long id);

    @GET("cidades")
    Call<List<Cidade>> listarCidades(@Query("nome") String nome);
    @GET("cidades/{id}")
    Call<Cidade> buscarCidade(@Path("id") Long id);
    @POST("cidades")
    Call<Cidade> cadastrarCidade(@Body Cidade cidade);
    @PUT("cidades/{id}")
    Call<Void> atualizarCidade(@Path("id") Long id, @Body Cidade cidade);
    @DELETE("cidades/{id}")
    Call<Void> excluirCidade(@Path("id") Long id);

    @GET("anuncios")
    Call<List<Anuncio>> listarAnuncios(
            @Query("modelo") Long idModelo,
            @Query("tipo") String tipo,
            @Query("ano_inicial") Integer anoInicial,
            @Query("ano_final") Integer anoFinal,
            @Query("min") Double valorMinimo,
            @Query("max") Double valorMaximo
    );
    @GET("anuncios/{id}")
    Call<Anuncio> buscarAnuncio(@Path("id") Long id);
    @POST("anuncios")
    Call<Anuncio> cadastrarAnuncio(@Body Anuncio anuncio);
    @PUT("anuncios/{id}")
    Call<Void> atualizarAnuncio(@Path("id") Long id, @Body Anuncio anuncio);
    @DELETE("anuncios/{id}")
    Call<Void> excluirAnuncio(@Path("id") Long id);

}