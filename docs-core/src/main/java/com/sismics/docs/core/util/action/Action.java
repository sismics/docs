package com.sismics.docs.core.util.action;

import com.sismics.docs.core.dao.jpa.dto.DocumentDto;

import javax.json.JsonObject;

/**
 * Base action interface.
 *
 * @author bgamard
 */
public interface Action {
    /**
     * Execute the action.
     *
     * @param documentDto Document DTO
     * @param action Action data
     */
    void execute(DocumentDto documentDto, JsonObject action);
}
