package com.sismics.docs.rest;

import java.io.File;
import java.net.URLDecoder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.junit.After;
import org.junit.Before;

import com.sismics.docs.rest.descriptor.JerseyTestWebAppDescriptorFactory;
import com.sismics.docs.rest.util.ClientUtil;
import com.sun.jersey.test.framework.JerseyTest;

/**
 * Base class of integration tests with Jersey.
 * 
 * @author jtremeaux
 */
public abstract class BaseJerseyTest extends JerseyTest {
    /**
     * Test HTTP server.
     */
    protected HttpServer httpServer;
    
    /**
     * Utility class for the REST client.
     */
    protected ClientUtil clientUtil;
    
    /**
     * Constructor of BaseJerseyTest.
     */
    public BaseJerseyTest() {
        super(JerseyTestWebAppDescriptorFactory.build());
        this.clientUtil = new ClientUtil(resource());
    }
    
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        String httpRoot = URLDecoder.decode(new File(getClass().getResource("/").getFile()).getAbsolutePath(), "utf-8");
        httpServer = HttpServer.createSimpleServer(httpRoot, "localhost", 9997);
        // Disable file cache to fix https://java.net/jira/browse/GRIZZLY-1350
        ((StaticHttpHandler) httpServer.getServerConfiguration().getHttpHandlers().keySet().iterator().next()).setFileCacheEnabled(false);
        httpServer.start();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        httpServer.stop();
    }
}
