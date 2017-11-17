package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.model.jpa.PasswordRecovery;
import com.sismics.docs.core.model.jpa.User;

/**
 * Event fired on user's password lost event.
 *
 * @author jtremeaux 
 */
public class PasswordLostEvent {
    /**
     * User.
     */
    private User user;

    /**
     * Password recovery request.
     */
    private PasswordRecovery passwordRecovery;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PasswordRecovery getPasswordRecovery() {
        return passwordRecovery;
    }

    public void setPasswordRecovery(PasswordRecovery passwordRecovery) {
        this.passwordRecovery = passwordRecovery;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("user", user)
                .add("passwordRecovery", "**hidden**")
                .toString();
    }
}
