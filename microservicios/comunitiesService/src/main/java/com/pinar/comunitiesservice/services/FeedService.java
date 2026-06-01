package com.pinar.comunitiesservice.services;

import com.pinar.comunitiesservice.entities.FeedItemDTO;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface FeedService {
    void publishPinToCommunityFeed(String comunidadId, FeedItemDTO item)
            throws ExecutionException, InterruptedException;

    List<FeedItemDTO> getFeed(String uidUsuario);
}
