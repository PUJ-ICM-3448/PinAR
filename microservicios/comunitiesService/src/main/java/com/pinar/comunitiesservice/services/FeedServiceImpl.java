package com.pinar.comunitiesservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.pinar.comunitiesservice.entities.FeedItemDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
public class FeedServiceImpl implements FeedService {
    private static final String COLECCION_COMUNIDADES = "comunidades";
    private static final int FEED_LIMIT = 50;
    private static final int PAGE_SIZE = 20;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public FeedServiceImpl(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishPinToCommunityFeed(String comunidadId, FeedItemDTO item)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference comunidadRef = db.collection(COLECCION_COMUNIDADES).document(comunidadId);
        DocumentSnapshot comunidadSnap = comunidadRef.get().get();

        if (!comunidadSnap.exists()) {
            throw new IllegalArgumentException("La comunidad con id " + comunidadId + " no existe.");
        }

        List<String> members = getMembers(comunidadSnap);
        String payload = toJson(item);

        for (String uid : members) {
            String key = feedKey(uid);
            //Sorted set de redis con llave feed:usuario,
            // valor el nuevo pin, y el score el timestamp
            // para que se ordene automatico por fecha
            redisTemplate.opsForZSet().add(key, payload, item.getCreatedAt());
            trimFeed(key);
        }
    }

    @Override
    public List<FeedItemDTO> getFeed(String uidUsuario) {
        Set<String> rawItems = redisTemplate.opsForZSet().reverseRange(
                feedKey(uidUsuario),
                0,
                PAGE_SIZE - 1
        );

        if (rawItems == null || rawItems.isEmpty()) {
            return List.of();
        }

        List<FeedItemDTO> items = new ArrayList<>();
        for (String rawItem : rawItems) {
            items.add(fromJson(rawItem));
        }
        return items;
    }

    private void trimFeed(String key) {
        //obterner cuantos elementos ya tiene ese feed
        Long size = redisTemplate.opsForZSet().zCard(key);
        if (size != null && size > FEED_LIMIT) {
            //eliminar mas antiguos si se pasa del limite
            redisTemplate.opsForZSet().removeRange(key, 0, size - FEED_LIMIT - 1);
        }
    }

    private List<String> getMembers(DocumentSnapshot comunidadSnap) {
        Object rawMembers = comunidadSnap.get("members");
        if (!(rawMembers instanceof List<?> rawList)) {
            return List.of();
        }

        List<String> members = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof String uid && !uid.isBlank()) {
                members.add(uid);
            }
        }
        return members;
    }

    private String feedKey(String uidUsuario) {
        return "feed:" + uidUsuario;
    }

    private String toJson(FeedItemDTO item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("No se pudo serializar el item del feed.", e);
        }
    }

    private FeedItemDTO fromJson(String rawItem) {
        try {
            return objectMapper.readValue(rawItem, FeedItemDTO.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("No se pudo leer un item del feed desde Redis.", e);
        }
    }
}
