package com.sismics.util.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.google.common.collect.Lists;
import com.sismics.docs.core.util.jpa.PaginatedList;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Memory appender for Logback.
 *
 * @author jtremeaux
 */
public class MemoryAppender extends AppenderBase<ILoggingEvent> {

    /**
     * Maximum size of the queue.
     */
    private int size;
    
    /**
     * Queue of log entries.
     */
    private static final Queue<LogEntry> logQueue = new ConcurrentLinkedQueue<>();

    @Override
    protected void append(ILoggingEvent event) {
        while (logQueue.size() > size) {
            logQueue.remove();
        }


        String loggerName = getLoggerName(event);

        LogEntry logEntry = new LogEntry(System.currentTimeMillis(), event.getLevel(), loggerName, event.getMessage());
        logQueue.add(logEntry);
    }

    /**
     * Extracts the class name of the logger, without the package name.
     * 
     * @param event Event
     * @return Class name
     */
    private String getLoggerName(ILoggingEvent event) {
        int index = event.getLoggerName().lastIndexOf('.');

        return (index > -1) ?
            event.getLoggerName().substring(index + 1) :
            event.getLoggerName();
    }

    /**
     * Getter of size.
     *
     * @return size
     */
    public int getSize() {
        return size;
    }

    /**
     * Setter of size.
     *
     * @param size size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Find some logs.
     * 
     * @param criteria Search criteria
     * @param list Paginated list (modified by side effect)
     */
    public static void find(LogCriteria criteria, PaginatedList<LogEntry> list) {
        List<LogEntry> logEntryList = new LinkedList<LogEntry>();
        final Level minLevel = criteria.getMinLevel();
        final String tag = criteria.getTag();
        final String message = criteria.getMessage();
        int resultCount = 0;
        for (LogEntry logEntry : logQueue) {
            if ((minLevel == null || logEntry.getLevel().toInt() >= minLevel.toInt()) &&
                    (tag == null || logEntry.getTag().toLowerCase().equals(tag)) &&
                    (message == null || logEntry.getMessage().toLowerCase().contains(message))) {
                logEntryList.add(logEntry);
                resultCount++;
            }
        }
        
        int fromIndex = logEntryList.size() - list.getOffset() - list.getLimit();
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        int toIndex = logEntryList.size() - list.getOffset();
        if (toIndex > logEntryList.size()) {
            toIndex = logEntryList.size();
        }
        List<LogEntry> logEntrySubList = Lists.reverse(logEntryList.subList(fromIndex, toIndex));
        list.setResultCount(resultCount);
        list.setResultList(logEntrySubList);
    }
}