/**
 * Copyright @ 2008 Quan Nguyen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.sismics.tess4j;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import org.w3c.dom.NodeList;

import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReaderSpi;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;
import com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam;

public class ImageIOHelper {

    final static String TIFF_FORMAT = "tiff";


    /**
     * Gets pixel data of an
     * <code>IIOImage</code> object.
     *
     * @param oimage an
     * <code>IIOImage</code> object
     * @return a byte buffer of pixel data
     * @throws Exception
     */
    public static ByteBuffer getImageByteBuffer(BufferedImage oimage) throws IOException {
        // Get tif writer and set output to file
        ImageWriter writer = new TIFFImageWriterSpi().createWriterInstance();

        // Set up the writeParam
        // We are using the old JAI ImageIO plugin, because for some reason, OCR don't work with TwelveMonkeys' plugin
        ImageWriteParam tiffWriteParam = new TIFFImageWriteParam(Locale.US);
        tiffWriteParam.setCompressionMode(ImageWriteParam.MODE_DISABLED);
        
        // Get the stream metadata
        IIOMetadata streamMetadata = writer.getDefaultStreamMetadata(tiffWriteParam);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
        writer.setOutput(ios);
        writer.write(streamMetadata, new IIOImage(oimage, null, null), tiffWriteParam);
        writer.dispose();
        
        // Read the writed image
        ios.seek(0);
        ImageReader reader = new TIFFImageReaderSpi().createReaderInstance();
        ImageReadParam param = reader.getDefaultReadParam();
        reader.setInput(ios, true, true);
        BufferedImage bi;
        try {
            bi = reader.read(0, param);
        } finally {
            reader.dispose();
            ios.close();
        }
        
        return convertImageData(bi);
    }

    /**
     * Converts <code>BufferedImage</code> to <code>ByteBuffer</code>.
     * 
     * @param bi Input image
     * @return pixel data
     */
    public static ByteBuffer convertImageData(BufferedImage bi) {
        byte[] pixelData = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
 //        return ByteBuffer.wrap(pixelData);
        ByteBuffer buf = ByteBuffer.allocateDirect(pixelData.length);
        buf.order(ByteOrder.nativeOrder());
        buf.put(pixelData);
        buf.flip();
        return buf;
    }

    /**
     * Reads image meta data.
     *
     * @param oimage
     * @return a map of meta data
     */
    public static Map<String, String> readImageData(IIOImage oimage) {
        Map<String, String> dict = new HashMap<String, String>();

        IIOMetadata imageMetadata = oimage.getMetadata();
        if (imageMetadata != null) {
            IIOMetadataNode dimNode = (IIOMetadataNode) imageMetadata.getAsTree("javax_imageio_1.0");
            NodeList nodes = dimNode.getElementsByTagName("HorizontalPixelSize");
            int dpiX;
            if (nodes.getLength() > 0) {
                float dpcWidth = Float.parseFloat(nodes.item(0).getAttributes().item(0).getNodeValue());
                dpiX = (int) Math.round(25.4f / dpcWidth);
            } else {
                dpiX = Toolkit.getDefaultToolkit().getScreenResolution();
            }
            dict.put("dpiX", String.valueOf(dpiX));

            nodes = dimNode.getElementsByTagName("VerticalPixelSize");
            int dpiY;
            if (nodes.getLength() > 0) {
                float dpcHeight = Float.parseFloat(nodes.item(0).getAttributes().item(0).getNodeValue());
                dpiY = (int) Math.round(25.4f / dpcHeight);
            } else {
                dpiY = Toolkit.getDefaultToolkit().getScreenResolution();
            }
            dict.put("dpiY", String.valueOf(dpiY));
        }

        return dict;
    }
}
