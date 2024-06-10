package com.slackdatafetching;

public class ChannelData {
    private String name;
    private String id;
    private String topic;
    private String description;
    private String creator;
    private String createDate;
    private String privacy;
    private String status;

    // Getters
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getDescription() {
        return description;
    }

    public String getCreator() {
        return creator;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getPrivacy() {
        return privacy;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public void updateData(String name, String topic, String description, String creator, String createDate, String privacy, String status) {
        if (name != null) this.name = name;
        if (topic != null) this.topic = topic;
        if (description != null) this.description = description;
        if (creator != null) this.creator = creator;
        if (createDate != null) this.createDate = createDate;
        if (privacy != null) this.privacy = privacy;
        if (status != null) this.status = status;
    }
}
