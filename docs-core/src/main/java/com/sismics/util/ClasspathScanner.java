package com.sismics.util;

import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.sismics.docs.core.util.format.PdfFormatHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * Classes scanner.
 */
public class ClasspathScanner<T> {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PdfFormatHandler.class);

    /**
     * Find classes assignable from another.
     *
     * @param topClass Top class or interface
     * @param pkg In this package
     * @return Set of classes
     */
    @SuppressWarnings("unchecked")
    public Set<Class<T>> findClasses(Class<T> topClass, String pkg) {
        Set<Class<T>> classes = Sets.newHashSet();
        try {
        for (ClassPath.ClassInfo classInfo : ClassPath.from(topClass.getClassLoader()).getTopLevelClasses(pkg)) {
            Class<?> clazz = classInfo.load();
            if (topClass.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
                classes.add((Class<T>) clazz);
            }
        }
        } catch (Exception e) {
            log.error("Error loading format handlers", e);
        }
        log.info("Found " + classes.size() + " classes for " + topClass.getSimpleName());
        return classes;
    }
}
