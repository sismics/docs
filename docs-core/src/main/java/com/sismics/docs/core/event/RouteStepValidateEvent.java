package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.dao.dto.UserDto;
import com.sismics.docs.core.model.jpa.Document;

/**
 * Event fired on route step validation event.
 *
 * @author bgamard
 */
public class RouteStepValidateEvent {
    /**
     * User.
     */
    private UserDto user;

    /**
     * Document linked to the route.
     */
    private Document document;

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public Document getDocument() {
        return document;
    }

    public RouteStepValidateEvent setDocument(Document document) {
        this.document = document;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("user", user)
                .add("document", document)
                .toString();
    }
}
