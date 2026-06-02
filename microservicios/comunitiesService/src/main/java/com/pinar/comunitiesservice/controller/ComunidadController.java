package com.pinar.comunitiesservice.controller;

import com.pinar.comunitiesservice.entities.Comunidad;
import com.pinar.comunitiesservice.entities.CreateComunidadRequest;
import com.pinar.comunitiesservice.services.ComunidadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comunidades")
public class ComunidadController {

    private final ComunidadService comunidadService;

    public ComunidadController(ComunidadService comunidadService) {
        this.comunidadService = comunidadService;
    }

    @PostMapping
    public ResponseEntity<?> createComunidad(@RequestBody CreateComunidadRequest request) {
        try{
            String uid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Comunidad nueva = new Comunidad();
            nueva.setName(request.getName());
            nueva.setDescription(request.getDescription());
            nueva.setPublic(request.isPublic());
            nueva.setImageUrl(request.getImageUrl());
            Comunidad comunidadCreada = comunidadService.createComunidad(nueva, uid, request.getImageUrl());
            return new ResponseEntity<>(comunidadCreada, HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>("No se ha podido crear la nueva comunidad: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateComunidad(@PathVariable String id, @RequestBody Comunidad c){
        try{
            String uid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return ResponseEntity.ok(comunidadService.updateComunidad(id, c, uid));
        }catch (Exception e){
            return new ResponseEntity<>("No se ha podido actualizar la comunidad: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/pines/{pinId}")
    public ResponseEntity<?> sharePin(@PathVariable String id, @PathVariable String pinId){
        try{
            String uid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            comunidadService.compartirPin(id, pinId, uid);
            return ResponseEntity.ok("Pin compartido exitosamente.");
        }catch (Exception e){
            return new ResponseEntity<>("No se ha podido compartir el pin: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}/pines/{pinId}")
    public ResponseEntity<?> unsharePin(@PathVariable String id, @PathVariable String pinId){
        try{
            String uid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            comunidadService.descompartirPin(id, pinId, uid);
            return ResponseEntity.ok("Pin descompartido exitosamente.");
        }catch (Exception e){
            return new ResponseEntity<>("No se ha podido descompartir el pin: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/miembros")
    public ResponseEntity<?> joinCommunity(@PathVariable String id){
        try{
            String uid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            comunidadService.unirseAComunidad(id, uid);
            return ResponseEntity.ok("Usuario agregado a la comunidad exitosamente.");
        }catch (Exception e){
            return new ResponseEntity<>("No se ha podido unir a la comunidad: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}/miembros")
    public ResponseEntity<?> leaveCommunity(@PathVariable String id){
        try{
            String uid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            comunidadService.salirDeComunidad(id, uid);
            return ResponseEntity.ok("Usuario removido de la comunidad exitosamente.");
        }catch (Exception e){
            return new ResponseEntity<>("No se ha podido salir de la comunidad: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/recomendadas")
    public ResponseEntity<?> getRecommendedCommunities(){
        try{
            return ResponseEntity.ok(comunidadService.getRecomendados());
        }catch (Exception e){
            return new ResponseEntity<>("No se han podido obtener las comunidades recomendadas: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(){
        try{
            String uid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return ResponseEntity.ok(comunidadService.getFeed(uid));
        }catch (Exception e){
            return new ResponseEntity<>("No se ha podido obtener el feed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getComunidad(@PathVariable String id){
        try{
            String uid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Comunidad comunidad = comunidadService.getComunidad(id, uid);
            return ResponseEntity.ok(comunidad);
        }catch (Exception e){
            return new ResponseEntity<>("No se ha podido obtener la comunidad: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
