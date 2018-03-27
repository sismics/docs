package com.sismics.util;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Classes scanner.
 */
public class ClasspathScanner<T> {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ClasspathScanner.class);

    /**
     * Find classes assignable from another.
     *
     * @param topClass Top class or interface
     * @param pkg In this package
     * @return List of classes
     */
    @SuppressWarnings("unchecked")
    public List<Class<T>> findClasses(Class<T> topClass, String pkg) {
        List<Class<T>> classes = Lists.newArrayList();
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

        classes.sort((o1, o2) -> {
            Priority priority1 = o1.getDeclaredAnnotation(Priority.class);
            Priority priority2 = o2.getDeclaredAnnotation(Priority.class);
            return Integer.compare(priority1 == null ? Integer.MAX_VALUE : priority1.value(),
                    priority2 == null ? Integer.MAX_VALUE : priority2.value());
        });

        log.info("Found " + classes.size() + " classes for " + topClass.getSimpleName());
        return classes;
    }

    /**
     * Classpath scanning priority.
     */
    public @interface Priority {
        int value() default Integer.MAX_VALUE;
    }
}
