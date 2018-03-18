package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.jpa.dto.UserDto;
import com.sismics.docs.core.event.PasswordLostEvent;
import com.sismics.docs.core.model.jpa.PasswordRecovery;
import com.sismics.docs.core.util.TransactionUtil;
import com.sismics.util.EmailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener for password recovery requests.
 *
 * @author jtremeaux 
 */
public class PasswordLostAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PasswordLostAsyncListener.class);

    /**
     * Handle events.
     * 
     * @param passwordLostEvent Event
     */
    @Subscribe
    public void onPasswordLost(final PasswordLostEvent passwordLostEvent) {
        if (log.isInfoEnabled()) {
            log.info("Password lost event: " + passwordLostEvent.toString());
        }
        
        TransactionUtil.handle(() -> {
            final UserDto user = passwordLostEvent.getUser();
            final PasswordRecovery passwordRecovery = passwordLostEvent.getPasswordRecovery();

            // Send the password recovery email
            Map<String, Object> paramRootMap = new HashMap<>();
            paramRootMap.put("user_name", user.getUsername());
            paramRootMap.put("password_recovery_key", passwordRecovery.getId());

            EmailUtil.sendEmail(Constants.EMAIL_TEMPLATE_PASSWORD_RECOVERY, user, paramRootMap);
        });
    }
}
