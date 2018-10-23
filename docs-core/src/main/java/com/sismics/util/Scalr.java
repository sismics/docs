package com.sismics.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImagingOpException;

/**
 * Extends Scalr.
 *
 * @author bgamard
 */
public class Scalr extends org.imgscalr.Scalr {
    /**
     * Rotate an image by a specific amount.
     *
     * @param src Source image
     * @param rotation Rotation angle
     * @param ops Options
     * @return Rotated image
     * @throws IllegalArgumentException
     * @throws ImagingOpException
     */
    public static BufferedImage rotate(BufferedImage src, double rotation, BufferedImageOp... ops) throws IllegalArgumentException, ImagingOpException {
        long t = System.currentTimeMillis();
        if (src == null) {
            throw new IllegalArgumentException("src cannot be null");
        } else {
            if (DEBUG) {
                log(0, "Rotating Image [%s]...", rotation);
            }

            AffineTransform tx = new AffineTransform();
            tx.rotate(Math.toRadians(rotation));

            BufferedImage result = createOptimalImage(src, src.getWidth(), src.getHeight());
            Graphics2D g2d = result.createGraphics();
            g2d.drawImage(src, tx, null);
            g2d.dispose();
            if (DEBUG) {
                log(0, "Rotation Applied in %d ms, result [width=%d, height=%d]", System.currentTimeMillis() - t, result.getWidth(), result.getHeight());
            }

            if (ops != null && ops.length > 0) {
                result = apply(result, ops);
            }

            return result;
        }
    }
}
