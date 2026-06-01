package com.pinar.comunitiesservice.entities;

public class FeedItemDTO {
    private String pinId;
    private String pinTitle;
    private String pinDescription;
    private String communityId;
    private String communityName;
    private String createdByUid;
    private long createdAt;
    private String imageUrl;

    public FeedItemDTO() {
    }

    public FeedItemDTO(String pinId, String pinTitle, String pinDescription, String communityId, String communityName,
            String createdByUid, long createdAt, String imageUrl) {
        this.pinId = pinId;
        this.pinTitle = pinTitle;
        this.pinDescription = pinDescription;
        this.communityId = communityId;
        this.communityName = communityName;
        this.createdByUid = createdByUid;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
    }

    public String getPinId() {
        return pinId;
    }

    public void setPinId(String pinId) {
        this.pinId = pinId;
    }

    public String getPinTitle() {
        return pinTitle;
    }

    public void setPinTitle(String pinTitle) {
        this.pinTitle = pinTitle;
    }

    public String getPinDescription() {
        return pinDescription;
    }

    public void setPinDescription(String pinDescription) {
        this.pinDescription = pinDescription;
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public String getCreatedByUid() {
        return createdByUid;
    }

    public void setCreatedByUid(String createdByUid) {
        this.createdByUid = createdByUid;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
