package com.sismics.docs.core.listener.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;

/**
 * Listener for all unprocessed events.
 * 
 * @author jtremeaux
 */
public class DeadEventListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DeadEventListener.class);

    /**
     * Process every dead event.
     * 
     * @param deadEvent Catchall event
     */
    @Subscribe
    public void onDeadEvent(DeadEvent deadEvent) {
        log.error("Dead event catched: " + deadEvent.toString());
    }
}
