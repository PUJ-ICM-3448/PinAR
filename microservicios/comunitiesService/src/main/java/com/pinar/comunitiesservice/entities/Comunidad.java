package com.pinar.comunitiesservice.entities;

import com.google.cloud.firestore.annotation.PropertyName;

import java.util.Date;
import java.util.List;

public class Comunidad {
    private String id;
    private String name;
    private String description;
    private String createdBy;
    private boolean isPublic;
    private Date createdAt;
    private String imageUrl;
    private int memberCount;
    private List<String> members;

    public Comunidad() {
    }

    public Comunidad(String id, String name, String description, String createdBy, boolean isPublic, Date createdAt,
            String imageUrl, int memberCount, List<String> members) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
        this.memberCount = memberCount;
        this.members = members;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @PropertyName("isPublic")
    public boolean isPublic() {
        return isPublic;
    }

    @PropertyName("isPublic")
    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}
