package com.sismics.util.log4j;

/**
 * Log entry.
 *
 * @author jtremeaux 
 */
public class LogEntry {
    /**
     * Time stamp.
     */
    private long timestamp;
    
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
     * Constructor of LogEntry.
     * 
     * @param timestamp Timestamp
     * @param level Logging level (DEBUG, WARN)...
     * @param tag Logger name / tag
     * @param message Message logged
     */
    public LogEntry(long timestamp, String level, String tag, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.tag = tag;
        this.message = message;
    }

    /**
     * Getter of timestamp.
     *
     * @return timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Getter of level.
     *
     * @return level
     */
    public String getLevel() {
        return level;
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
     * Getter of message.
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }
}
