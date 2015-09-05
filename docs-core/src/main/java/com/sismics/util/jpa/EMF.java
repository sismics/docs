package com.sismics.util.jpa;

import com.sismics.docs.core.util.DirectoryUtil;
import org.hibernate.cfg.Environment;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Entity manager factory.
 * 
 * @author jtremeaux
 */
public final class EMF {
    private static final Logger log = LoggerFactory.getLogger(EMF.class);

    private static EntityManagerFactory emfInstance;

    static {
        try {
            Map<Object, Object> properties = getEntityManagerProperties();

            Environment.verifyProperties(properties);
            ConfigurationHelper.resolvePlaceHolders(properties);
            ServiceRegistry reg = new ServiceRegistryBuilder().applySettings(properties).buildServiceRegistry();

            DbOpenHelper openHelper = new DbOpenHelper(reg) {
                
                @Override
                public void onCreate() throws Exception {
                    executeAllScript(0);
                }

                @Override
                public void onUpgrade(int oldVersion, int newVersion) throws Exception {
                    for (int version = oldVersion + 1; version <= newVersion; version++) {
                        executeAllScript(version);
                    }
                }
            };
            openHelper.open();
            
            emfInstance = Persistence.createEntityManagerFactory("transactions-optional", getEntityManagerProperties());
            
        } catch (Throwable t) {
            log.error("Error creating EMF", t);
        }
    }
    
    private static Map<Object, Object> getEntityManagerProperties() {
        // Use properties file if exists
        try {
            URL hibernatePropertiesUrl = EMF.class.getResource("/hibernate.properties");
            if (hibernatePropertiesUrl != null) {
                log.info("Configuring EntityManager from hibernate.properties");
                
                InputStream is = hibernatePropertiesUrl.openStream();
                Properties properties = new Properties();
                properties.load(is);
                return properties;
            }
        } catch (IOException | IllegalArgumentException e) {
            log.error("Error reading hibernate.properties", e);
        }
        
        // Use environment parameters
        log.info("Configuring EntityManager from environment parameters");
        Map<Object, Object> props = new HashMap<Object, Object>();
        props.put("hibernate.connection.driver_class", "org.h2.Driver");
        File dbDirectory = DirectoryUtil.getDbDirectory();
        String dbFile = dbDirectory.getAbsoluteFile() + File.separator + "docs";
        props.put("hibernate.connection.url", "jdbc:h2:file:" + dbFile + ";CACHE_SIZE=65536");
        props.put("hibernate.connection.username", "sa");
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");
        props.put("hibernate.max_fetch_depth", "5");
        props.put("hibernate.cache.use_second_level_cache", "false");
        return props;
    }
    
    /**
     * Private constructor.
     */
    private EMF() {
    }

    /**
     * Returns an instance of EMF.
     * 
     * @return Instance of EMF
     */
    public static EntityManagerFactory get() {
        return emfInstance;
    }
}