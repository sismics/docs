package com.sismics.util.listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ServiceRegistry;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * Takes care of registering and de-registering local ImageIO plugins (service providers) for the servlet context.
 * <p>
 * Registers all available plugins on {@code contextInitialized} event, using {@code ImageIO.scanForPlugins()}, to make
 * sure they are available to the current servlet context.
 * De-registers all plugins which have the {@link Thread#getContextClassLoader() current thread's context class loader}
 * as its class loader on {@code contextDestroyed} event, to avoid class/resource leak.
 * </p>
 * Copied from: <a href="https://github.com/haraldk/TwelveMonkeys/blob/master/servlet/src/main/java/com/twelvemonkeys/servlet/image/IIOProviderContextListener.java">https://github.com/haraldk/TwelveMonkeys</a>
 *
 * @author <a href="mailto:harald.kuhr@gmail.com">Harald Kuhr</a>
 * @author last modified by $Author: haraldk$
 * @version $Id: IIOProviderContextListener.java,v 1.0 14.02.12 21:53 haraldk Exp$
 * @see ImageIO#scanForPlugins()
 */
public final class IIOProviderContextListener implements ServletContextListener {
    
    public void contextInitialized(final ServletContextEvent event) {
        event.getServletContext().log("Scanning for locally installed ImageIO plugin providers");

        // Registers all locally available IIO plugins.
        ImageIO.scanForPlugins();
    }

    public void contextDestroyed(final ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();

        // De-register any locally registered IIO plugins. Relies on each web app having its own context class loader.
        LocalFilter localFilter = new LocalFilter(Thread.currentThread().getContextClassLoader()); // scanForPlugins uses context class loader

        IIORegistry registry = IIORegistry.getDefaultInstance();
        Iterator<Class<?>> categories = registry.getCategories();

        while (categories.hasNext()) {
            deregisterLocalProvidersForCategory(registry, localFilter, categories.next(), servletContext);
        }
    }

    private static <T> void deregisterLocalProvidersForCategory(IIORegistry registry, LocalFilter localFilter, Class<T> category, ServletContext context) {
        Iterator<T> providers = registry.getServiceProviders(category, localFilter, false);

        // Copy the providers, as de-registering while iterating over providers will lead to ConcurrentModificationExceptions.
        List<T> providersCopy = new ArrayList<>();
        while (providers.hasNext()) {
            providersCopy.add(providers.next());
        }

        for (T provider : providersCopy) {
            registry.deregisterServiceProvider(provider, category);
            context.log(String.format("Unregistered locally installed provider class: %s", provider.getClass()));
        }
    }

    static class LocalFilter implements ServiceRegistry.Filter {
        private final ClassLoader loader;

        public LocalFilter(ClassLoader loader) {
            this.loader = loader;
        }

        public boolean filter(Object provider) {
            return provider.getClass().getClassLoader() == loader;
        }
    }
}