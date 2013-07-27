package com.sismics.docs.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

/**
 * Stream utilities.
 * 
 * @author bgamard
 */
public class StreamUtil {

    /**
     * Detects if the stream is gzipped, and returns a uncompressed stream according to this.
     * 
     * @param is InputStream
     * @return InputStream
     * @throws IOException 
     */
    public static InputStream detectGzip(InputStream is) throws IOException {
        PushbackInputStream pb = new PushbackInputStream(is, 2);
        byte [] signature = new byte[2];
        pb.read(signature);
        pb.unread(signature);
        if(signature[0] == (byte) GZIPInputStream.GZIP_MAGIC && signature[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8)) {
          return new GZIPInputStream(pb);
        }   else {
          return pb;
        }
    }
}
