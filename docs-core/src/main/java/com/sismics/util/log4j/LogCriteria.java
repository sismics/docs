package com.sismics.util.log4j;

import org.apache.commons.lang.StringUtils;

/**
 * Log search criteria.
 *
 * @author jtremeaux 
 */
public class LogCriteria {

    /**
     * Logging level (DEBUG, WARN)...
     */
    private String level;
    
    /**
     * Logger name / tag.
     */
    private String tag;
    
    /**
     * Message logged.
     */
    private String message;

    /**
     * Getter of level.
     *
     * @return level
     */
    public String getLevel() {
        return level;
    }

    /**
     * Setter of level.
     *
     * @param level level
     */
    public void setLevel(String level) {
        this.level = StringUtils.lowerCase(level);
    }

    /**
     * Getter of tag.
     *
     * @return tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * Setter of tag.
     *
     * @param tag tag
     */
    public void setTag(String tag) {
        this.tag = StringUtils.lowerCase(tag);
    }

    /**
     * Getter of message.
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter of message.
     *
     * @param message message
     */
    public void setMessage(String message) {
        this.message = StringUtils.lowerCase(message);
    }
}
