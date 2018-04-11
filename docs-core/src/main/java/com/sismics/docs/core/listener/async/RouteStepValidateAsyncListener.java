package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.dto.UserDto;
import com.sismics.docs.core.event.RouteStepValidateEvent;
import com.sismics.docs.core.util.TransactionUtil;
import com.sismics.util.EmailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener for route step validate.
 *
 * @author bgamard
 */
public class RouteStepValidateAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(RouteStepValidateAsyncListener.class);

    /**
     * Handle events.
     * 
     * @param event Event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void on(final RouteStepValidateEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Route step validate event: " + event.toString());
        }
        
        TransactionUtil.handle(() -> {
            final UserDto user = event.getUser();

            // Send route step validated email
            Map<String, Object> paramRootMap = new HashMap<>();
            paramRootMap.put("user_name", user.getUsername());
            paramRootMap.put("document_id", event.getDocument().getId());
            paramRootMap.put("document_title", event.getDocument().getTitle());

            EmailUtil.sendEmail(Constants.EMAIL_TEMPLATE_ROUTE_STEP_VALIDATE, user, paramRootMap);
        });
    }
}
