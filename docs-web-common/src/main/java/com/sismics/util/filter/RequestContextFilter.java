package com.sismics.util.filter;

import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.TransactionUtil;
import com.sismics.util.EnvironmentUtil;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.EMF;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Filter used to process a couple things in the request context.
 * 
 * @author jtremeaux
 */
public class RequestContextFilter implements Filter {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(RequestContextFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialize the app directory
        if (!filterConfig.getServletContext().getServerInfo().startsWith("Grizzly")) {
            EnvironmentUtil.setWebappContext(true);
        }
        try {
            if (log.isInfoEnabled()) {
                log.info(MessageFormat.format("Using base data directory: {0}", DirectoryUtil.getBaseDataDirectory()));
            }
        } catch (Exception e) {
            log.error("Error initializing base data directory", e);
        }
        
        
        // Initialize file logger
        RollingFileAppender fileAppender = new RollingFileAppender();
        fileAppender.setName("FILE");
        fileAppender.setFile(DirectoryUtil.getLogDirectory().resolve("docs.log").toString());
        fileAppender.setLayout(new PatternLayout("%d{DATE} %p %l %m %n"));
        fileAppender.setThreshold(Level.INFO);
        fileAppender.setAppend(true);
        fileAppender.setMaxFileSize("5MB");
        fileAppender.setMaxBackupIndex(5);
        fileAppender.activateOptions();
        org.apache.log4j.Logger.getRootLogger().addAppender(fileAppender);
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        
        // Initialize the application context
        TransactionUtil.handle(AppContext::getInstance);
    }

    @Override
    public void destroy() {
        AppContext.getInstance().shutDown();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        EntityManager em;
        
        try {
            em = EMF.get().createEntityManager();
        } catch (Exception e) {
            throw new ServletException("Cannot create entity manager", e);
        }
        ThreadLocalContext context = ThreadLocalContext.get();
        context.setEntityManager(em);
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        
        try {
            addCacheHeaders(response);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            ThreadLocalContext.cleanup();
            
            // IOException are thrown if the client closes the connection before completion
            if (!(e instanceof IOException)) {
                log.error("An exception occured, rolling back current transaction", e);

                // If an unprocessed error comes up from the application layers (Jersey...), rollback the transaction (should not happen)
                if (em.isOpen()) {
                    if (em.getTransaction() != null && em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    
                    try {
                        em.close();
                    } catch (Exception ce) {
                        log.error("Error closing entity manager", ce);
                    }
                }
                throw new ServletException(e);
            }
        }

        // No error processing the request : commit / rollback the current transaction depending on the HTTP code
        if (em.isOpen()) {
            if (em.getTransaction() != null && em.getTransaction().isActive()) {
                HttpServletResponse r = (HttpServletResponse) response;
                int statusClass = r.getStatus() / 100;
                if (statusClass == 2 || statusClass == 3) {
                    try {
                        em.getTransaction().commit();
                    } catch (Exception e) {
                        log.error("Error during commit", e);
                        r.sendError(500);
                    }
                } else {
                    em.getTransaction().rollback();
                }
                
                try {
                    em.close();
                } catch (Exception e) {
                    log.error("Error closing entity manager", e);
                }
            }
        }

        // Fire all pending async events after request transaction commit.
        // This way, all modifications done during this request are available in the listeners.
        context.fireAllAsyncEvents();

        ThreadLocalContext.cleanup();
    }

    /**
     * Add no-cache header.
     *
     * @param response Response
     */
    private void addCacheHeaders(ServletResponse response) {
        HttpServletResponse r = (HttpServletResponse) response;
        r.addHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        r.addHeader(HttpHeaders.EXPIRES, "0");
    }
}
