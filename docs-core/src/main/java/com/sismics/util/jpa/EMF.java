package com.sismics.util.jpa;

import com.google.common.base.Strings;
import com.sismics.docs.core.util.DirectoryUtil;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
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

    private static Map<Object, Object> properties;

    private static EntityManagerFactory emfInstance;

    static {
        try {
            properties = getEntityManagerProperties();

            ConfigurationHelper.resolvePlaceHolders(properties);
            ServiceRegistry reg = new StandardServiceRegistryBuilder().applySettings(properties).build();

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
        String databaseUrl = System.getenv("DATABASE_URL");
        String databaseUsername = System.getenv("DATABASE_USER");
        String databasePassword = System.getenv("DATABASE_PASSWORD");

        log.info("Configuring EntityManager from environment parameters");
        Map<Object, Object> props = new HashMap<>();
        Path dbDirectory = DirectoryUtil.getDbDirectory();
        String dbFile = dbDirectory.resolve("docs").toAbsolutePath().toString();
        if (Strings.isNullOrEmpty(databaseUrl)) {
            props.put("hibernate.connection.driver_class", "org.h2.Driver");
            props.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
            props.put("hibernate.connection.url", "jdbc:h2:file:" + dbFile + ";CACHE_SIZE=65536;LOCK_TIMEOUT=10000");
            props.put("hibernate.connection.username", "sa");
        } else {
            props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL94Dialect");
            props.put("hibernate.connection.url", databaseUrl);
            props.put("hibernate.connection.username", databaseUsername);
            props.put("hibernate.connection.password", databasePassword);
        }
        props.put("hibernate.hbm2ddl.auto", "");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");
        props.put("hibernate.max_fetch_depth", "5");
        props.put("hibernate.cache.use_second_level_cache", "false");
        props.put("hibernate.c3p0.min_size", "1");
        props.put("hibernate.c3p0.max_size", "10");
        props.put("hibernate.c3p0.timeout", "5000");
        props.put("hibernate.c3p0.max_statements", "0");
        props.put("hibernate.c3p0.acquire_increment", "1");
        props.put("hibernate.c3p0.idle_test_period", "10");
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

    public static boolean isDriverH2() {
        String driver = getDriver();
        return driver.contains("h2");
    }

    public static boolean isDriverPostgresql() {
        String driver = getDriver();
        return driver.contains("postgresql");
    }

    public static String getDriver() {
        return (String) properties.get("hibernate.connection.driver_class");
    }
}