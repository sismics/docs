package com.sismics.util.log4j;

import org.apache.log4j.Level;

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
    private Level level;
    
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
    public LogEntry(long timestamp, Level level, String tag, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.tag = tag;
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Level getLevel() {
        return level;
    }

    public String getTag() {
        return tag;
    }

    public String getMessage() {
        return message;
    }
}
