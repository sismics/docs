package com.sismics.docs.core.util;

import com.sismics.util.EnvironmentUtil;
import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * Utilities to gain access to the storage directories used by the application.
 * 
 * @author jtremeaux
 */
public class DirectoryUtil {
    /**
     * Returns the base data directory.
     * 
     * @return Base data directory
     */
    public static File getBaseDataDirectory() {
        File baseDataDir = null;
        if (EnvironmentUtil.getWebappRoot() != null) {
            // We are in a webapp environment
            if (StringUtils.isNotBlank(EnvironmentUtil.getDocsHome())) {
                // If the docs.home property is set then use it
                baseDataDir = new File(EnvironmentUtil.getDocsHome());
                if (!baseDataDir.isDirectory()) {
                    baseDataDir.mkdirs();
                }
            } else {
                // Use the base of the Webapp directory
                baseDataDir = new File(EnvironmentUtil.getWebappRoot() + File.separator + "sismicsdocs");
                if (!baseDataDir.isDirectory()) {
                    baseDataDir.mkdirs();
                }
            }
        }
        if (baseDataDir == null) {
            // Or else (for unit testing), use a temporary directory
            baseDataDir = new File(System.getProperty("java.io.tmpdir"));
        }
        
        return baseDataDir;
    }
    
    /**
     * Returns the database directory.
     * 
     * @return Database directory.
     */
    public static File getDbDirectory() {
        return getDataSubDirectory("db");
    }

    /**
     * Returns the lucene indexes directory.
     * 
     * @return Lucene indexes directory.
     */
    public static File getLuceneDirectory() {
        return getDataSubDirectory("lucene");
    }
    
    /**
     * Returns the storage directory.
     * 
     * @return Storage directory.
     */
    public static File getStorageDirectory() {
        return getDataSubDirectory("storage");
    }
    
    /**
     * Returns the log directory.
     * 
     * @return Log directory.
     */
    public static File getLogDirectory() {
        return getDataSubDirectory("log");
    }

    /**
     * Returns the themes directory.
     * 
     * @return Theme directory.
     */
    public static File getThemeDirectory() {
        String webappRoot = EnvironmentUtil.getWebappRoot();
        File themeDir = null;
        if (webappRoot != null) {
            themeDir = new File(webappRoot + File.separator + "style" + File.separator + "theme");
        } else {
            themeDir = new File(DirectoryUtil.class.getResource("/style/theme").getFile());
        }
        if (themeDir != null && themeDir.isDirectory()) {
            return themeDir;
        }
        return null;
    }

    /**
     * Returns a subdirectory of the base data directory
     * 
     * @return Subdirectory
     */
    private static File getDataSubDirectory(String subdirectory) {
        File baseDataDir = getBaseDataDirectory();
        File directory = new File(baseDataDir.getPath() + File.separator + subdirectory);
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        return directory;
    }
}
