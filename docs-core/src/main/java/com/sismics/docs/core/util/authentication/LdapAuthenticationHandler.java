package com.sismics.docs.core.util.authentication;

import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.ConfigDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.model.jpa.Config;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.ConfigUtil;
import com.sismics.util.ClasspathScanner;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * LDAP authentication handler.
 *
 * @author bgamard
 */
@ClasspathScanner.Priority(50) // Before the internal database
public class LdapAuthenticationHandler implements AuthenticationHandler {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(LdapAuthenticationHandler.class);

    /**
     * Get a LDAP connection.
     * @return LdapConnection
     */
    private LdapConnection getConnection() {
        ConfigDao configDao = new ConfigDao();
        Config ldapEnabled = configDao.getById(ConfigType.LDAP_ENABLED);
        if (ldapEnabled == null || !Boolean.parseBoolean(ldapEnabled.getValue())) {
            return null;
        }

        LdapConnectionConfig config = new LdapConnectionConfig();
        config.setLdapHost(ConfigUtil.getConfigStringValue(ConfigType.LDAP_HOST));
        config.setLdapPort(ConfigUtil.getConfigIntegerValue(ConfigType.LDAP_PORT));
        config.setUseSsl(ConfigUtil.getConfigBooleanValue(ConfigType.LDAP_USESSL));
        config.setName(ConfigUtil.getConfigStringValue(ConfigType.LDAP_ADMIN_DN));
        config.setCredentials(ConfigUtil.getConfigStringValue(ConfigType.LDAP_ADMIN_PASSWORD));

        return new LdapNetworkConnection(config);
    }

    @Override
    public User authenticate(String username, String password) {
        // Fetch and authenticate the user
        Entry userEntry;
        try (LdapConnection ldapConnection = getConnection()) {
            if (ldapConnection == null) {
                return null;
            }
            ldapConnection.bind();

            EntryCursor cursor = ldapConnection.search(ConfigUtil.getConfigStringValue(ConfigType.LDAP_BASE_DN),
                    ConfigUtil.getConfigStringValue(ConfigType.LDAP_FILTER).replace("USERNAME", username), SearchScope.SUBTREE);
            if (cursor.next()) {
                userEntry = cursor.get();
                ldapConnection.bind(userEntry.getDn(), password);
            } else {
                // User not found
                return null;
            }
        } catch (Exception e) {
            log.error("Error authenticating \"" + username + "\" using the LDAP", e);
            return null;
        }

        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            // The user is valid but never authenticated, create the user now
            log.info("\"" + username + "\" authenticated for the first time, creating the internal user");
            user = new User();
            user.setRoleId(Constants.DEFAULT_USER_ROLE);
            user.setUsername(username);
            user.setPassword(UUID.randomUUID().toString()); // No authentication using the internal database
            Attribute mailAttribute = userEntry.get("mail");
            if (mailAttribute == null || mailAttribute.get() == null) {
                user.setEmail(ConfigUtil.getConfigStringValue(ConfigType.LDAP_DEFAULT_EMAIL));
            } else {
                Value value = mailAttribute.get();
                user.setEmail(value.getString());
            }
            user.setStorageQuota(ConfigUtil.getConfigLongValue(ConfigType.LDAP_DEFAULT_STORAGE));
            try {
                userDao.create(user, "admin");
            } catch (Exception e) {
                log.error("Error while creating the internal user", e);
                return null;
            }
        }

        return user;
    }
}
