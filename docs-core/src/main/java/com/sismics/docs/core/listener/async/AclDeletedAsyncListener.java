package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.event.AclDeletedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener on ACL deleted.
 * 
 * @author bgamard
 */
public class AclDeletedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AclDeletedAsyncListener.class);

    /**
     * ACL deleted.
     *
     * @param event ACL deleted event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void on(final AclDeletedAsyncEvent event) {
        if (log.isInfoEnabled()) {
            log.info("ACL deleted event: " + event.toString());
        }

        TransactionUtil.handle(() -> AppContext.getInstance().getIndexingHandler()
                .deleteAcl(event.getSourceId(), event.getPerm(), event.getTargetId()));
    }
}
