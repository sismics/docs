package com.sismics.docs.core.model.jpa;

import java.util.Date;

/**
 * An entity which can be logged.
 * 
 * @author bgamard
 */
public interface Loggable {
    /**
     * Get a string representation of this entity for logging purpose.
     * Avoid returning sensitive data like passwords.
     * 
     * @return Entity message
     */
    public String toMessage();
    
    /**
     * Loggable are soft deletable.
     * 
     * @return deleteDate
     */
    public Date getDeleteDate();
}
