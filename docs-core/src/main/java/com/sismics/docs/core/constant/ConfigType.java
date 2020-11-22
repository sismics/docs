package com.sismics.docs.core.constant;

/**
 * Configuration parameters. 
 *
 * @author jtremeaux 
 */
public enum ConfigType {
    /**
     * Lucene directory storage type.
     */
    LUCENE_DIRECTORY_STORAGE,
    /**
     * Theme configuration.
     */
    THEME,

    /**
     * Guest login.
     */
    GUEST_LOGIN,

    /**
     * Default language.
     */
    DEFAULT_LANGUAGE,

    /**
     * SMTP server configuration.
     */
    SMTP_HOSTNAME,
    SMTP_PORT,
    SMTP_FROM,
    SMTP_USERNAME,
    SMTP_PASSWORD,

    /**
     * Inbox scanning configuration.
     */
    INBOX_ENABLED,
    INBOX_HOSTNAME,
    INBOX_PORT,
    INBOX_USERNAME,
    INBOX_PASSWORD,
    INBOX_FOLDER,
    INBOX_TAG,
    INBOX_AUTOMATIC_TAGS,
    INBOX_DELETE_IMPORTED,

    /**
     * LDAP connection.
     */
    LDAP_ENABLED,
    LDAP_HOST,
    LDAP_PORT,
    LDAP_ADMIN_DN,
    LDAP_ADMIN_PASSWORD,
    LDAP_BASE_DN,
    LDAP_FILTER,
    LDAP_DEFAULT_EMAIL,
    LDAP_DEFAULT_STORAGE
}
