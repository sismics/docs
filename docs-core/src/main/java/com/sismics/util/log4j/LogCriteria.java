package com.sismics.util.log4j;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;

/**
 * Log search criteria.
 *
 * @author jtremeaux 
 */
public class LogCriteria {

    /**
     * Minimum logging level (DEBUG, WARN)...
     */
    private Level minLevel;
    
    /**
     * Logger name / tag.
     */
    private String tag;
    
    /**
     * Message logged.
     */
    private String message;

    public Level getMinLevel() {
        return minLevel;
    }

    public LogCriteria setMinLevel(Level level) {
        this.minLevel = level;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public LogCriteria setTag(String tag) {
        this.tag = StringUtils.lowerCase(tag);
        return this;
    }

    public String getMessage() {
        return message;
    }

    public LogCriteria setMessage(String message) {
        this.message = StringUtils.lowerCase(message);
        return this;
    }
}
