package com.sismics.docs.core.util.action;

import com.sismics.docs.core.dao.dto.DocumentDto;

import jakarta.json.JsonObject;

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

    /**
     * Validate the action.
     *
     * @param action Action data
     * @throws Exception Validation error
     */
    void validate(JsonObject action) throws Exception;
}
