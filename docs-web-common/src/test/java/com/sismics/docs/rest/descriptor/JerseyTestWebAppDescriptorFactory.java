package com.sismics.docs.rest.descriptor;

import com.sismics.util.filter.RequestContextFilter;
import com.sismics.util.filter.TokenBasedSecurityFilter;
import com.sun.jersey.test.framework.WebAppDescriptor;

import java.io.File;

/**
 * Jersey tests Webapp descriptor.
 * 
 * @author jtremeaux
 */
public class JerseyTestWebAppDescriptorFactory {
    private static String basePath = new File("src/main/webapp").getAbsolutePath();
    
    /**
     * Constructs a new descriptor.
     * 
     * @return Descriptor
     */
    public static WebAppDescriptor build() {
        // Target the base path to the Webapp resources
        System.setProperty("user.dir", basePath);
        System.setProperty("test", "true");
        
        return new WebAppDescriptor.Builder("com.sismics.docs.rest.resource")
            .contextPath("docs")
            .addFilter(RequestContextFilter.class, "requestContextFilter")
            .addFilter(TokenBasedSecurityFilter.class, "tokenBasedSecurityFilter")
            .initParam("com.sun.jersey.spi.container.ContainerRequestFilters", "com.sun.jersey.api.container.filter.LoggingFilter")
            .initParam("com.sun.jersey.spi.container.ContainerResponseFilters", "com.sun.jersey.api.container.filter.LoggingFilter")
            .initParam("com.sun.jersey.config.feature.logging.DisableEntitylogging", "true")
            .build();
    }
}
