package com.sismics.docs.core.util;

import com.sismics.docs.core.constant.ActionType;
import com.sismics.docs.core.dao.dto.DocumentDto;
import com.sismics.docs.core.util.action.Action;
import com.sismics.docs.core.util.action.AddTagAction;
import com.sismics.docs.core.util.action.ProcessFilesAction;
import com.sismics.docs.core.util.action.RemoveTagAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonObject;

/**
 * Action utilities.
 *
 * @author bgamard
 */
public class ActionUtil {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ActionUtil.class);

    /**
     * Find the action associated to an action type.
     *
     * @param actionType Action type
     * @return Action
     */
    private static Action findAction(ActionType actionType) {
        Action action = null;
        switch (actionType) {
            case ADD_TAG:
                action = new AddTagAction();
                break;
            case REMOVE_TAG:
                action = new RemoveTagAction();
                break;
            case PROCESS_FILES:
                action = new ProcessFilesAction();
                break;
            default:
                log.error("Action type not handled: " + actionType);
                break;
        }

        return action;
    }

    /**
     * Validate an action.
     *
     * @param actionType Action type
     * @param actionData Action data
     * @throws Exception Validation error
     */
    public static void validateAction(ActionType actionType, JsonObject actionData) throws Exception {
        Action action = findAction(actionType);
        action.validate(actionData);
    }

    /**
     * Execute an action.
     *
     * @param actionType Action type
     * @param actionData Action data
     * @param documentDto Document DTO
     */
    public static void executeAction(ActionType actionType, JsonObject actionData, DocumentDto documentDto) {
        Action action = findAction(actionType);
        action.execute(documentDto, actionData);
    }
}
