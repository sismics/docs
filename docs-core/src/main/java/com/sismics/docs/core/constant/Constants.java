package com.sismics.docs.core.constant;

import java.util.List;

import com.google.common.collect.Lists;

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
    public static final String DEFAULT_ADMIN_PASSWORD = "$2a$05$6Ny3TjrW3aVAL1or2SlcR.fhuDgPKp5jp.P9fBXwVNePgeLqb4i3C";

    /**
     * RAM Lucene directory storage.
     */
    public static final String LUCENE_DIRECTORY_STORAGE_RAM = "RAM";
    
    /**
     * File Lucene directory storage.
     */
    public static final String LUCENE_DIRECTORY_STORAGE_FILE = "FILE";

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
    public static final List<String> SUPPORTED_LANGUAGES = Lists.newArrayList("eng", "fra", "jpn");
}
