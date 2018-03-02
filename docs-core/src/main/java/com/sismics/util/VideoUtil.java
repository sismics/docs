package com.sismics.util;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.sismics.util.io.InputStreamReaderThread;
import com.sismics.util.mime.MimeType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Video processing utilities.
 *
 * @author bgamard
 */
public class VideoUtil {
    /**
     * Returns true if this MIME type is a video.
     * @param mimeType MIME type
     * @return True if video
     */
    public static boolean isVideo(String mimeType) {
        return mimeType.equals(MimeType.VIDEO_MP4) || mimeType.equals(MimeType.VIDEO_WEBM);
    }

    /**
     * Generate a thumbnail from a video file.
     *
     * @param file Video file
     * @return Thumbnail
     */
    public static BufferedImage getThumbnail(Path file) throws Exception {
        List<String> result = Lists.newLinkedList(Arrays.asList("ffmpeg", "-i"));
        result.add(file.toAbsolutePath().toString());
        result.addAll(Arrays.asList("-vf", "thumbnail", "-frames:v", "1", "-f", "mjpeg", "-"));
        ProcessBuilder pb = new ProcessBuilder(result);
        Process process = pb.start();

        // Consume the process error stream
        final String commandName = pb.command().get(0);
        new InputStreamReaderThread(process.getErrorStream(), commandName).start();

        // Consume the data as an image
        try (InputStream is = process.getInputStream()) {
            return ImageIO.read(is);
        }
    }

    /**
     * Extract metadata from a video file.
     *
     * @param file Video file
     * @return Metadata
     */
    public static String getMetadata(Path file) {
        List<String> result = Lists.newLinkedList();
        result.add("mediainfo");
        result.add(file.toAbsolutePath().toString());
        ProcessBuilder pb = new ProcessBuilder(result);
        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            return null;
        }

        // Consume the process error stream
        final String commandName = pb.command().get(0);
        new InputStreamReaderThread(process.getErrorStream(), commandName).start();

        // Consume the data as a string
        try (InputStream is = process.getInputStream()) {
            return new String(ByteStreams.toByteArray(is), Charsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
