package com.pinar.comunitiesservice.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateComunidadRequest {
    private String name;
    private String description;
    private boolean isPublic = true;
    private String imageUrl = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("isPublic")
    public boolean isPublic() {
        return isPublic;
    }

    @JsonProperty("isPublic")
    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl != null ? imageUrl : "";
    }
}
