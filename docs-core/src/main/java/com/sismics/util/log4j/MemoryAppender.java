package com.sismics.util.log4j;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import com.google.common.collect.Lists;
import com.sismics.docs.core.util.jpa.PaginatedList;

/**
 * Memory appender for Log4J.
 *
 * @author jtremeaux
 */
public class MemoryAppender extends AppenderSkeleton {

    /**
     * Maximum size of the queue.
     */
    private int size;
    
    /**
     * Queue of log entries.
     */
    private final Queue<LogEntry> logQueue = new ConcurrentLinkedQueue<LogEntry>();

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
    }

    @Override
    public synchronized void append(LoggingEvent event) {
        while (logQueue.size() > size) {
            logQueue.remove();
        }
        if (closed) {
            LogLog.warn("This appender is already closed, cannot append event.");
            return;
        }
        
        String loggerName = getLoggerName(event);

        LogEntry logEntry = new LogEntry(System.currentTimeMillis(), event.getLevel(), loggerName, event.getMessage().toString());
        logQueue.add(logEntry);
    }

    /**
     * Extracts the class name of the logger, without the package name.
     * 
     * @param event Event
     * @return Class name
     */
    private String getLoggerName(LoggingEvent event) {
        int index = event.getLoggerName().lastIndexOf('.');

        return (index > -1) ?
            event.getLoggerName().substring(index + 1) :
            event.getLoggerName();
    }

    /**
     * Getter of logList.
     *
     * @return logList
     */
    public Queue<LogEntry> getLogList() {
        return logQueue;
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
    public void find(LogCriteria criteria, PaginatedList<LogEntry> list) {
        List<LogEntry> logEntryList = new LinkedList<LogEntry>();
        final Level minLevel = criteria.getMinLevel();
        final String tag = criteria.getTag();
        final String message = criteria.getMessage();
        int resultCount = 0;
        for (Iterator<LogEntry> it = logQueue.iterator(); it.hasNext();) {
            LogEntry logEntry = it.next();
            if ((minLevel == null ||  Integer.compare(logEntry.getLevel().toInt(), minLevel.toInt()) >= 0) &&
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