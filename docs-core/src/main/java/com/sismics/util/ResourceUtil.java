package com.sismics.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.collect.Lists;

/**
 * Resource utilities.
 *
 * @author jtremeaux 
 */
public class ResourceUtil {

    /**
     * List files inside a directory. The path can be a directory on the filesystem, or inside a JAR.
     * 
     * @param clazz Class
     * @param path Path
     * @param filter Filter
     * @return List of files
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<String> list(Class<?> clazz, String path, FilenameFilter filter) throws URISyntaxException, IOException {
        // Path is a directory on the filesystem
        URL dirUrl = clazz.getResource(path);
        if (dirUrl != null && dirUrl.getProtocol().equals("file")) {
            return Arrays.asList(new File(dirUrl.toURI()).list(filter));
        }

        // Path is a directory inside the same JAR as clazz
        if (dirUrl == null) {
            String className = clazz.getName().replace(".", "/") + ".class";
            dirUrl = clazz.getClassLoader().getResource(className);
        }

        if (dirUrl.getProtocol().equals("jar")) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (!path.endsWith("/")) {
                path = path + "/";
            }
            
            // Extract the JAR path
            String jarPath = dirUrl.getPath().substring(5, dirUrl.getPath().indexOf("!"));
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            
            Enumeration<JarEntry> entries = jar.entries();
            Set<String> fileSet = new HashSet<String>();
            while (entries.hasMoreElements()) {
                // Filter according to the path
                String entryName = entries.nextElement().getName();
                if (!entryName.startsWith(path)) {
                    continue;
                }
                String name = entryName.substring(path.length());
                if (!"".equals(name)) {
                    // If it is a subdirectory, just return the directory name
                    int checkSubdir = name.indexOf("/");
                    if (checkSubdir >= 0) {
                        name = name.substring(0, checkSubdir);
                    }
                    
                    if (filter == null || filter.accept(null, name)) {
                        fileSet.add(name);
                    }
                }
            }
            return Lists.newArrayList(fileSet);
        }
        
        throw new UnsupportedOperationException(MessageFormat.format("Cannot list files for URL {0}", dirUrl));
    }

    /**
     * List files inside a directory. The path can be a directory on the filesystem, or inside a JAR.
     * 
     * @param clazz Class
     * @param path Path
     * @return List of files
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<String> list(Class<?> clazz, String path) throws URISyntaxException, IOException {
        return list(clazz, path, null);
    }
}
