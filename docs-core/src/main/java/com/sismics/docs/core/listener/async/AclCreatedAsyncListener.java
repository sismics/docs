package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.event.AclCreatedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener on ACL created.
 * 
 * @author bgamard
 */
public class AclCreatedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AclCreatedAsyncListener.class);

    /**
     * ACL created.
     * 
     * @param event ACL created event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void on(final AclCreatedAsyncEvent event) {
        if (log.isInfoEnabled()) {
            log.info("ACL created event: " + event.toString());
        }

        TransactionUtil.handle(() -> AppContext.getInstance().getIndexingHandler()
                .createAcl(event.getSourceId(), event.getPerm(), event.getTargetId()));
    }
}
