package com.pinar.comunitiesservice.services;

import com.pinar.comunitiesservice.entities.Comunidad;
import com.pinar.comunitiesservice.entities.FeedItemDTO;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ComunidadService {
    Comunidad createComunidad(Comunidad comunidad, String uidCreador, String urlImagen) throws ExecutionException, InterruptedException;
    Comunidad updateComunidad(String id, Comunidad comunidad, String uidUsuario) throws ExecutionException, InterruptedException, IllegalAccessException;
    void compartirPin(String comunidadId, String pinId, String uidUsuario) throws ExecutionException, InterruptedException, IllegalAccessException;
    void descompartirPin(String comunidadId, String pinId, String uidUsuario) throws ExecutionException, InterruptedException, IllegalAccessException;
    Comunidad getComunidad(String id, String uid) throws ExecutionException, InterruptedException, IllegalAccessException;
    void unirseAComunidad(String comunidadId, String uidUsuario) throws ExecutionException, InterruptedException;
    void salirDeComunidad(String comunidadId, String uidUsuario) throws ExecutionException, InterruptedException;
    List<Comunidad> getRecomendados() throws ExecutionException, InterruptedException;
    List<FeedItemDTO> getFeed(String uidUsuario) throws ExecutionException, InterruptedException;

}
