package com.sismics.docs.core.constant;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Application constants.
 * 
 * @author jtremeaux
 */
public class Constants {
    /**
     * Default timezone ID.
     */
    public static final String DEFAULT_TIMEZONE_ID = "Europe/London";
    
    /**
     * Administrator's default password ("admin").
     */
    public static final String DEFAULT_ADMIN_PASSWORD = "$2y$10$xg0EEKVUehutDI1m6qQhVeFz7SMQMl1jQzjf2KkVsR2c7aV2vyyjK";

    /**
     * Administrator's default email.
     */
    public static final String DEFAULT_ADMIN_EMAIL = "admin@localhost";

    /**
     * Bcrypt default work factor
     */
    public static final int DEFAULT_BCRYPT_WORK = 10;

    /**
     * Guest user ID.
     */
    public static final String GUEST_USER_ID = "guest";

    /**
     * Default generic user role.
     */
    public static final String DEFAULT_USER_ROLE = "user";
    
    /**
     * Supported document languages.
     */
    public static final List<String> SUPPORTED_LANGUAGES = Lists.newArrayList("eng", "fra", "ita", "deu", "spa", "por", "pol", "rus", "ukr", "ara", "hin", "chi_sim", "chi_tra", "jpn", "tha", "kor", "nld", "tur", "heb", "hun", "fin", "swe", "lav", "dan", "nor", "vie", "ces");

    /**
     * Base URL environment variable.
     */
    public static final String BASE_URL_ENV = "DOCS_BASE_URL";

    /**
     * Default language environment variable.
     */
    public static final String DEFAULT_LANGUAGE_ENV = "DOCS_DEFAULT_LANGUAGE";

    /**
     * SMTP configuration environment variables.
     */
    public static final String SMTP_HOSTNAME_ENV = "DOCS_SMTP_HOSTNAME";
    public static final String SMTP_PORT_ENV = "DOCS_SMTP_PORT";
    public static final String SMTP_USERNAME_ENV = "DOCS_SMTP_USERNAME";
    public static final String SMTP_PASSWORD_ENV = "DOCS_SMTP_PASSWORD";

    /**
     * Global quota environment variable.
     */
    public static final String GLOBAL_QUOTA_ENV = "DOCS_GLOBAL_QUOTA";

    /**
     * Initial admin password environment variable.
     */
    public static final String ADMIN_PASSWORD_INIT_ENV = "DOCS_ADMIN_PASSWORD_INIT";

    /**
     * Initial admin password environment variable.
     */
    public static final String ADMIN_EMAIL_INIT_ENV = "DOCS_ADMIN_EMAIL_INIT";

    /**
     * Work factor to be used by Bcrypt
     */
    public static final String BCRYPT_WORK_ENV = "DOCS_BCRYPT_WORK";

    /**
     * Expiration time of the password recovery in hours.
     */
    public static final int PASSWORD_RECOVERY_EXPIRATION_HOUR = 2;

    /**
     * Email template for password recovery.
     */
    public static final String EMAIL_TEMPLATE_PASSWORD_RECOVERY = "password_recovery";

    /**
     * Email template for route step validate.
     */
    public static final String EMAIL_TEMPLATE_ROUTE_STEP_VALIDATE = "route_step_validate";

    /**
     * mm per inch.
     */
    public static float MM_PER_INCH = 1 / (10 * 2.54f) * 72f;
}
