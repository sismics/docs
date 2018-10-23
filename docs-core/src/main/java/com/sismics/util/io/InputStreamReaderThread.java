package com.sismics.util.io;

import com.google.common.io.Closer;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Thread that consumes data from an input stream and logs it.
 *
 * @author jtremeaux
 */
public class InputStreamReaderThread extends Thread {

    private static final Logger logger = Logger.getLogger(InputStreamReaderThread.class);

    private InputStream is;

    private String name;

    private Closer closer = Closer.create();

    public InputStreamReaderThread(InputStream input, String name) {
        super(name + " InputStreamReader thread");
        this.is = closer.register(input);
        this.name = name;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = closer.register(new BufferedReader(new InputStreamReader(is)));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format(name + ": %s", line));
                }
            }
        } catch (IOException x) {
            // NOP
        } finally {
            try {
                closer.close();
            } catch (Exception e) {
                // NOP
            }
        }
    }
}
