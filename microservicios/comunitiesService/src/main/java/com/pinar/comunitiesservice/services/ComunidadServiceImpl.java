package com.pinar.comunitiesservice.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.pinar.comunitiesservice.entities.Comunidad;
import com.pinar.comunitiesservice.entities.FeedItemDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
public class ComunidadServiceImpl implements ComunidadService {
    private static final String COLECCION_COMUNIDADES = "comunidades";
    private static final String COLECCION_PINES = "cloud_anchor_pins";
    private static final String COLECCION_USUARIOS = "usuarios";

    private final FeedService feedService;

    public ComunidadServiceImpl(FeedService feedService) {
        this.feedService = feedService;
    }

    @Override
    public Comunidad createComunidad(Comunidad comunidad, String uidCreador, String urlImagen)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLECCION_COMUNIDADES).document();

        Comunidad nueva = new Comunidad(
                docRef.getId(),
                comunidad.getName(),
                comunidad.getDescription(),
                uidCreador,
                comunidad.isPublic(),
                new Date(),
                urlImagen,
                1,
                List.of(uidCreador));

        docRef.set(toFirestoreMap(nueva)).get();
        addCommunityToUser(db, uidCreador, nueva);
        return nueva;
    }

    @Override
    public Comunidad updateComunidad(String id, Comunidad comunidad, String uidUsuario)
            throws ExecutionException, InterruptedException, IllegalAccessException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLECCION_COMUNIDADES).document(id);
        DocumentSnapshot docSnap = docRef.get().get();

        if (!docSnap.exists()) {
            throw new IllegalArgumentException("La comunidad con id " + id + " no existe.");
        }
        if (!uidUsuario.equals(docSnap.getString("createdBy"))) {
            throw new IllegalAccessException("Solo el creador de la comunidad puede actualizarla.");
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", comunidad.getName());
        updates.put("description", comunidad.getDescription());
        updates.put("isPublic", comunidad.isPublic());
        updates.put("imageUrl", comunidad.getImageUrl());
        docRef.update(updates).get();

        return toComunidad(docRef.get().get());
    }

    @Override
    public void compartirPin(String comunidadId, String pinId, String uidUsuario)
            throws ExecutionException, InterruptedException, IllegalAccessException {
        Firestore db = FirestoreClient.getFirestore();
        Comunidad comunidad = getExistingComunidad(db, comunidadId);

        if (!comunidad.getMembers().contains(uidUsuario)) {
            throw new IllegalAccessException("Solo miembros de la comunidad pueden compartir pines en ella.");
        }

        DocumentReference pinRef = db.collection(COLECCION_PINES).document(pinId);
        DocumentSnapshot pinSnap = pinRef.get().get();

        if (!pinSnap.exists() || !uidUsuario.equals(pinSnap.getString("createdBy"))) {
            throw new IllegalAccessException(
                    "El pin con id " + pinId + " no existe o el usuario no tiene permiso para compartirlo.");
        }

        pinRef.update("comunidades", FieldValue.arrayUnion(comunidadId)).get();
        feedService.publishPinToCommunityFeed(comunidadId, toFeedItem(pinSnap, comunidad));
    }

    @Override
    public void descompartirPin(String comunidadId, String pinId, String uidUsuario)
            throws ExecutionException, InterruptedException, IllegalAccessException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference pinRef = db.collection(COLECCION_PINES).document(pinId);
        DocumentSnapshot pinSnap = pinRef.get().get();

        if (!pinSnap.exists() || !uidUsuario.equals(pinSnap.getString("createdBy"))) {
            throw new IllegalAccessException(
                    "El pin con id " + pinId + " no existe o el usuario no tiene permiso para descompartirlo.");
        }

        pinRef.update("comunidades", FieldValue.arrayRemove(comunidadId)).get();
    }

    @Override
    public Comunidad getComunidad(String id, String uid)
            throws ExecutionException, InterruptedException, IllegalAccessException {
        Firestore db = FirestoreClient.getFirestore();
        Comunidad comunidad = getExistingComunidad(db, id);

        if (!comunidad.isPublic() && !comunidad.getMembers().contains(uid)) {
            throw new IllegalAccessException("La comunidad con id " + id + " es privada y el usuario no es miembro.");
        }

        return comunidad;
    }

    @Override
    public void unirseAComunidad(String comunidadId, String uidUsuario)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLECCION_COMUNIDADES).document(comunidadId);
        Comunidad comunidad = getExistingComunidad(db, comunidadId);

        if (!comunidad.isPublic()) {
            throw new IllegalArgumentException(
                    "La comunidad con id " + comunidadId + " es privada, no se puede unir sin invitacion.");
        }

        if (comunidad.getMembers().contains(uidUsuario)) {
            throw new IllegalArgumentException("El usuario ya es miembro de la comunidad con id " + comunidadId + ".");
        }

        docRef.update("members", FieldValue.arrayUnion(uidUsuario), "memberCount", FieldValue.increment(1)).get();
        comunidad.setMemberCount(comunidad.getMemberCount() + 1);
        comunidad.getMembers().add(uidUsuario);
        addCommunityToUser(db, uidUsuario, comunidad);
    }

    @Override
    public void salirDeComunidad(String comunidadId, String uidUsuario)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLECCION_COMUNIDADES).document(comunidadId);
        Comunidad comunidad = getExistingComunidad(db, comunidadId);

        if (!comunidad.getMembers().contains(uidUsuario)) {
            throw new IllegalArgumentException("El usuario no es miembro de la comunidad con id " + comunidadId + ".");
        }

        docRef.update("members", FieldValue.arrayRemove(uidUsuario), "memberCount", FieldValue.increment(-1)).get();
        removeCommunityFromUser(db, uidUsuario, comunidadId);
    }

    @Override
    public List<Comunidad> getRecomendados() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection(COLECCION_COMUNIDADES)
                .whereEqualTo("isPublic", true)
                .orderBy("memberCount", Query.Direction.DESCENDING)
                .limit(10)
                .get();

        List<Comunidad> comunidades = new ArrayList<>();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        for (QueryDocumentSnapshot doc : documents) {
            comunidades.add(toComunidad(doc));
        }
        return comunidades;
    }

    @Override
    public List<FeedItemDTO> getFeed(String uidUsuario) {
        return feedService.getFeed(uidUsuario);
    }

    private Comunidad getExistingComunidad(Firestore db, String comunidadId)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot docSnap = db.collection(COLECCION_COMUNIDADES).document(comunidadId).get().get();
        if (!docSnap.exists()) {
            throw new IllegalArgumentException("La comunidad con id " + comunidadId + " no existe.");
        }
        return toComunidad(docSnap);
    }

    private Comunidad toComunidad(DocumentSnapshot docSnap) {
        Comunidad comunidad = docSnap.toObject(Comunidad.class);
        if (comunidad == null) {
            throw new IllegalArgumentException("No se pudo leer la comunidad con id " + docSnap.getId() + ".");
        }

        if (comunidad.getId() == null || comunidad.getId().isBlank()) {
            comunidad.setId(docSnap.getId());
        }

        Boolean publicValue = docSnap.getBoolean("isPublic");
        if (publicValue != null) {
            comunidad.setPublic(publicValue);
        }

        List<String> members = getStringList(docSnap, "members");
        if (members.isEmpty()) {
            members = getStringList(docSnap, "memberIds");
        }
        comunidad.setMembers(new ArrayList<>(members));

        Long memberCount = docSnap.getLong("memberCount");
        if (memberCount == null) {
            memberCount = docSnap.getLong("numMembers");
        }
        comunidad.setMemberCount(memberCount == null ? comunidad.getMembers().size() : memberCount.intValue());

        return comunidad;
    }

    private Map<String, Object> toFirestoreMap(Comunidad comunidad) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", comunidad.getId());
        data.put("name", comunidad.getName());
        data.put("description", comunidad.getDescription());
        data.put("createdBy", comunidad.getCreatedBy());
        data.put("isPublic", comunidad.isPublic());
        data.put("createdAt", comunidad.getCreatedAt());
        data.put("imageUrl", comunidad.getImageUrl());
        data.put("memberCount", comunidad.getMemberCount());
        data.put("members", comunidad.getMembers());
        return data;
    }

    private void addCommunityToUser(Firestore db, String uidUsuario, Comunidad comunidad)
            throws ExecutionException, InterruptedException {
        DocumentReference userRef = db.collection(COLECCION_USUARIOS).document(uidUsuario);
        userRef.set(Map.of("memberOf", FieldValue.arrayUnion(toBasicInfoMap(comunidad))), SetOptions.merge()).get();
    }

    private void removeCommunityFromUser(Firestore db, String uidUsuario, String comunidadId)
            throws ExecutionException, InterruptedException {
        DocumentReference userRef = db.collection(COLECCION_USUARIOS).document(uidUsuario);
        DocumentSnapshot userSnap = userRef.get().get();

        List<Map<String, Object>> updatedMemberOf = new ArrayList<>();
        Object rawMemberOf = userSnap.get("memberOf");
        if (rawMemberOf instanceof List<?>) {
            for (Object item : (List<?>) rawMemberOf) {
                if (item instanceof Map<?, ?> rawMap && !Objects.equals(rawMap.get("id"), comunidadId)) {
                    updatedMemberOf.add(toStringObjectMap(rawMap));
                }
            }
        }

        userRef.set(Map.of("memberOf", updatedMemberOf), SetOptions.merge()).get();
    }

    private Map<String, Object> toBasicInfoMap(Comunidad comunidad) {
        Map<String, Object> datosBasicos = new LinkedHashMap<>();
        datosBasicos.put("id", comunidad.getId());
        datosBasicos.put("name", comunidad.getName());
        datosBasicos.put("imgUrl", comunidad.getImageUrl());
        datosBasicos.put("description", comunidad.getDescription());
        return datosBasicos;
    }

    private List<String> getStringList(DocumentSnapshot docSnap, String fieldName) {
        Object rawValue = docSnap.get(fieldName);
        if (!(rawValue instanceof List<?> rawList)) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof String value && !value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    private Map<String, Object> toStringObjectMap(Map<?, ?> rawMap) {
        Map<String, Object> converted = new HashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() instanceof String key) {
                converted.put(key, entry.getValue());
            }
        }
        return converted;
    }

    private FeedItemDTO toFeedItem(DocumentSnapshot pinSnap, Comunidad comunidad) {
        return new FeedItemDTO(
                pinSnap.getId(),
                nullToEmpty(pinSnap.getString("title")),
                nullToEmpty(pinSnap.getString("description")),
                comunidad.getId(),
                comunidad.getName(),
                nullToEmpty(pinSnap.getString("createdBy")),
                System.currentTimeMillis(),
                nullToEmpty(pinSnap.getString("imageUrl")));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
