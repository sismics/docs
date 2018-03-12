package com.sismics.docs.core.util;

import com.sismics.docs.core.constant.ActionType;
import com.sismics.docs.core.dao.jpa.dto.DocumentDto;
import com.sismics.docs.core.util.action.Action;
import com.sismics.docs.core.util.action.AddTagAction;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;

/**
 * Action utilities.
 *
 * @author bgamard
 */
public class ActionUtil {
    /**
     * Logger.
     */
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(LuceneUtil.class);

    /**
     * Execute an action.
     *
     * @param actionType Action type
     * @param actionData Action data
     * @param documentDto Document DTO
     */
    public static void executeAction(ActionType actionType, JsonObject actionData, DocumentDto documentDto) {
        Action action;
        switch (actionType) {
            case ADD_TAG:
                action = new AddTagAction();
                break;
            default:
                log.error("Action type not handled: " + actionType);
                return;
        }

        action.execute(documentDto, actionData);
    }
}
