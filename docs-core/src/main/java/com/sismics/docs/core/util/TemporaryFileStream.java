package com.sismics.docs.core.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Utilities for writing and reading to a temporary file.
 * 
 * @author bgamard
 */
public class TemporaryFileStream implements Closeable {
    /**
     * Temporary file.
     */
    private Path tempFile;
    
    /**
     * Construct a temporary file.
     * 
     * @throws IOException
     */
    public TemporaryFileStream() throws IOException {
        tempFile = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
    }
    
    /**
     * Open a stream for writing.
     * 
     * @return OutputStream
     * @throws IOException
     */
    public OutputStream openWriteStream() throws IOException {
        return Files.newOutputStream(tempFile);
    }
    
    /**
     * Open a stream for reading.
     * 
     * @return InputStream
     * @throws IOException
     */
    public InputStream openReadStream() throws IOException {
        return Files.newInputStream(tempFile);
    }
    
    @Override
    public void close() throws IOException {
        Files.delete(tempFile);
    }
}