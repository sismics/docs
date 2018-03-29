package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.dao.dto.UserDto;
import com.sismics.docs.core.model.jpa.PasswordRecovery;

/**
 * Event fired on user's password lost event.
 *
 * @author jtremeaux 
 */
public class PasswordLostEvent {
    /**
     * User.
     */
    private UserDto user;

    /**
     * Password recovery request.
     */
    private PasswordRecovery passwordRecovery;

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
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
