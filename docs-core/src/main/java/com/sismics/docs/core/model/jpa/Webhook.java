package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.constant.WebhookEvent;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Webhook entity.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_WEBHOOK")
public class Webhook implements Loggable {
    /**
     * Webhook ID.
     */
    @Id
    @Column(name = "WHK_ID_C", nullable = false, length = 36)
    private String id;
    
    /**
     * Event.
     */
    @Column(name = "WHK_EVENT_C", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private WebhookEvent event;
    
    /**
     * URL.
     */
    @Column(name = "WHK_URL_C", nullable = false, length = 1024)
    private String url;

    /**
     * Creation date.
     */
    @Column(name = "WHK_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * Deletion date.
     */
    @Column(name = "WHK_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public Webhook setId(String id) {
        this.id = id;
        return this;
    }

    public WebhookEvent getEvent() {
        return event;
    }

    public Webhook setEvent(WebhookEvent event) {
        this.event = event;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Webhook setUrl(String url) {
        this.url = url;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Webhook setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    @Override
    public Date getDeleteDate() {
        return deleteDate;
    }

    public Webhook setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("event", event)
                .add("url", url)
                .toString();
    }

    @Override
    public String toMessage() {
        return url;
    }
}
