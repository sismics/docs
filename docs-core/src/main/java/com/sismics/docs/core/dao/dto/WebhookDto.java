package com.sismics.docs.core.dao.dto;

/**
 * Webhook DTO.
 * 
 * @author bgamard
 */
public class WebhookDto {
    /**
     * Webhook ID.
     */
    private String id;
    
    /**
     * Event.
     */
    private String event;

    /**
     * URL.
     */
    private String url;

    /**
     * Creation date.
     */
    private Long createTimestamp;

    public String getId() {
        return id;
    }

    public WebhookDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getEvent() {
        return event;
    }

    public WebhookDto setEvent(String event) {
        this.event = event;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public WebhookDto setUrl(String url) {
        this.url = url;
        return this;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public WebhookDto setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return id.equals(((WebhookDto) obj).getId());
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
