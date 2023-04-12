package com.sismics.util.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.TransactionUtil;
import com.sismics.util.EnvironmentUtil;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.EMF;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(logContext);
        encoder.setPattern("%date [%t] %-5level %logger{36} - %msg%n");
        encoder.start();
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setMaxFileSize(FileSize.valueOf("5MB"));
        rollingPolicy.setFileNamePattern("docs.%d{yyyy-MM-dd_HH}.log");
        rollingPolicy.setMaxHistory(5);
        rollingPolicy.setContext(logContext);
        rollingPolicy.setParent(appender);
        rollingPolicy.start();
        appender.setContext(logContext);
        appender.setName("FILE");
        appender.setFile(DirectoryUtil.getLogDirectory().resolve("docs.log").toString());
        appender.setEncoder(encoder);
        appender.setRollingPolicy(rollingPolicy);
        appender.setAppend(true);
        appender.start();
        ch.qos.logback.classic.Logger logger = logContext.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setAdditive(false);
        logger.setLevel(Level.INFO);
        logger.addAppender(appender);
        
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
