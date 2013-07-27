package com.sismics.docs.core.util;

import com.sismics.util.context.ThreadLocalContext;

/**
 * Entity manager utils.
 *
 * @author jtremeaux 
 */
public class EntityManagerUtil {
    /**
     * Flush the entity manager session.
     */
    public static void flush() {
        ThreadLocalContext.get().getEntityManager().flush();
    }
}
