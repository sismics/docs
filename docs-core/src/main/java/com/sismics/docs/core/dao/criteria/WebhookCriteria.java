package com.sismics.docs.core.dao.criteria;

import com.sismics.docs.core.constant.WebhookEvent;

/**
 * Webhook criteria.
 *
 * @author bgamard 
 */
public class WebhookCriteria {
    /**
     * Webhook event.
     */
    private WebhookEvent event;

    public WebhookEvent getEvent() {
        return event;
    }

    public WebhookCriteria setEvent(WebhookEvent event) {
        this.event = event;
        return this;
    }
}
