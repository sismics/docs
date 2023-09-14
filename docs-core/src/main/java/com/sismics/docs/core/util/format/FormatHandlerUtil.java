package com.sismics.docs.core.util.format;

import com.google.common.collect.Lists;
import com.sismics.util.ClasspathScanner;

import java.util.List;

/**
 * Format handler utilities.
 *
 * @author bgamard
 */
public class FormatHandlerUtil {
    /**
     * List of format handlers scanned in the classpath.
     */
    private static final List<Class<? extends FormatHandler>> FORMAT_HANDLERS = Lists.newArrayList(
            new ClasspathScanner<FormatHandler>().findClasses(FormatHandler.class, "com.sismics.docs.core.util.format"));

    /**
     * Find a suitable format handler for this MIME type.
     *
     * @param mimeType MIME type
     * @return Instancied format handler
     */
    public static FormatHandler find(String mimeType) {
        try {
            for (Class<? extends FormatHandler> formatHandlerClass : FORMAT_HANDLERS) {
                FormatHandler formatHandler = formatHandlerClass.getDeclaredConstructor().newInstance();
                if (formatHandler.accept(mimeType)) {
                    return formatHandler;
                }
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }
}
