/**
 * Copyright @ 2012 Quan Nguyen
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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.imageio.IIOImage;

import com.sun.jna.Pointer;

/**
 * An object layer on top of
 * <code>TessAPI</code>, provides character recognition support for common image
 * formats, and multi-page TIFF images beyond the uncompressed, binary TIFF
 * format supported by Tesseract OCR engine. The extended capabilities are
 * provided by the
 * <code>Java Advanced Imaging Image I/O Tools</code>. <br /><br /> Support for
 * PDF documents is available through
 * <code>Ghost4J</code>, a
 * <code>JNA</code> wrapper for
 * <code>GPL Ghostscript</code>, which should be installed and included in
 * system path. <br /><br /> Any program that uses the library will need to
 * ensure that the required libraries (the
 * <code>.jar</code> files for
 * <code>jna</code>,
 * <code>jai-imageio</code>, and
 * <code>ghost4j</code>) are in its compile and run-time
 * <code>classpath</code>.
 */
public class Tesseract {

    private static Tesseract instance;
    private final static Rectangle EMPTY_RECTANGLE = new Rectangle();
    private String language = "eng";
    private String datapath = null;
    private int psm = TessAPI.TessPageSegMode.PSM_AUTO;
    private boolean hocr;
    private int pageNum;
    private int ocrEngineMode = TessAPI.TessOcrEngineMode.OEM_DEFAULT;
    private Properties prop = new Properties();
    public final static String htmlBeginTag =
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\""
            + " \"http://www.w3.org/TR/html4/loose.dtd\">\n"
            + "<html>\n<head>\n<title></title>\n"
            + "<meta http-equiv=\"Content-Type\" content=\"text/html;"
            + "charset=utf-8\" />\n<meta name='ocr-system' content='tesseract'/>\n"
            + "</head>\n<body>\n";
    public final static String htmlEndTag = "</body>\n</html>\n";

    /**
     * Private constructor.
     */
    private Tesseract() {
        System.setProperty("jna.encoding", "UTF8");
    }

    /**
     * Gets an instance of the class library.
     *
     * @return instance
     */
    public static synchronized Tesseract getInstance() {
        if (instance == null) {
            instance = new Tesseract();
        }

        return instance;
    }

    /**
     * Sets tessdata path.
     *
     * @param datapath the tessdata path to set
     */
    public void setDatapath(String datapath) {
        this.datapath = datapath;
    }

    /**
     * Sets language for OCR.
     *
     * @param language the language code, which follows ISO 639-3 standard.
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Sets OCR engine mode.
     *
     * @param ocrEngineMode the OcrEngineMode to set
     */
    public void setOcrEngineMode(int ocrEngineMode) {
        this.ocrEngineMode = ocrEngineMode;
    }

    /**
     * Sets page segmentation mode.
     *
     * @param mode the page segmentation mode to set
     */
    public void setPageSegMode(int mode) {
        this.psm = mode;
    }

    /**
     * Enables hocr output.
     *
     * @param hocr to enable or disable hocr output
     */
    public void setHocr(boolean hocr) {
        this.hocr = hocr;
        prop.setProperty("tessedit_create_hocr", hocr ? "1" : "0");
    }

    /**
     * Set the value of Tesseract's internal parameter.
     *
     * @param key variable name, e.g.,
     * <code>tessedit_create_hocr</code>,
     * <code>tessedit_char_whitelist</code>, etc.
     * @param value value for corresponding variable, e.g., "1", "0",
     * "0123456789", etc.
     */
    public void setTessVariable(String key, String value) {
        prop.setProperty(key, value);
    }

    /**
     * Performs OCR operation.
     *
     * @param bi a buffered image
     * @return the recognized text
     * @throws TesseractException
     */
    public String doOCR(BufferedImage bi) throws TesseractException {
        return doOCR(bi, null);
    }

    /**
     * Performs OCR operation.
     *
     * @param bi a buffered image
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or
     * <code>null</code> indicates the whole image.
     * @return the recognized text
     * @throws TesseractException
     */
    public String doOCR(BufferedImage bi, Rectangle rect) throws TesseractException {
        IIOImage oimage = new IIOImage(bi, null, null);
        List<IIOImage> imageList = new ArrayList<IIOImage>();
        imageList.add(oimage);
        return doOCR(imageList, rect);
    }

    /**
     * Performs OCR operation.
     *
     * @param imageList a list of
     * <code>IIOImage</code> objects
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or
     * <code>null</code> indicates the whole image.
     * @return the recognized text
     * @throws TesseractException
     */
    public String doOCR(List<IIOImage> imageList, Rectangle rect) throws TesseractException {
        StringBuilder sb = new StringBuilder();
        pageNum = 0;

        for (IIOImage oimage : imageList) {
            pageNum++;
            try {
                ByteBuffer buf = ImageIOHelper.getImageByteBuffer(oimage);
                RenderedImage ri = oimage.getRenderedImage();
                String pageText = doOCR(ri.getWidth(), ri.getHeight(), buf, rect, ri.getColorModel().getPixelSize());
                sb.append(pageText);
            } catch (IOException ioe) {
                //skip the problematic image
                System.err.println(ioe.getMessage());
            }
        }

        if (hocr) {
            sb.insert(0, htmlBeginTag).append(htmlEndTag);
        }
        return sb.toString();
    }

    /**
     * Performs OCR operation. Use
     * <code>SetImage</code>, (optionally)
     * <code>SetRectangle</code>, and one or more of the
     * <code>Get*Text</code> functions.
     *
     * @param xsize width of image
     * @param ysize height of image
     * @param buf pixel data
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or
     * <code>null</code> indicates the whole image.
     * @param bpp bits per pixel, represents the bit depth of the image, with 1
     * for binary bitmap, 8 for gray, and 24 for color RGB.
     * @return the recognized text
     * @throws TesseractException
     */
    public String doOCR(int xsize, int ysize, ByteBuffer buf, Rectangle rect, int bpp) throws TesseractException {
        TessAPI api = TessAPI.INSTANCE;
        TessAPI.TessBaseAPI handle = api.TessBaseAPICreate();
        api.TessBaseAPIInit2(handle, datapath, language, ocrEngineMode);
        api.TessBaseAPISetPageSegMode(handle, psm);
        
        Enumeration<?> em = prop.propertyNames();
        while (em.hasMoreElements()) {
            String key = (String) em.nextElement();
            api.TessBaseAPISetVariable(handle, key, prop.getProperty(key));
        }

        int bytespp = bpp / 8;
        int bytespl = (int) Math.ceil(xsize * bpp / 8.0);
        api.TessBaseAPISetImage(handle, buf, xsize, ysize, bytespp, bytespl);

        if (rect != null && !rect.equals(EMPTY_RECTANGLE)) {
            api.TessBaseAPISetRectangle(handle, rect.x, rect.y, rect.width, rect.height);
        }

        Pointer utf8Text = hocr ? api.TessBaseAPIGetHOCRText(handle, pageNum - 1) : api.TessBaseAPIGetUTF8Text(handle);
        String str = utf8Text.getString(0);
        api.TessDeleteText(utf8Text);
        api.TessBaseAPIDelete(handle);
        
        return str;
    }
}
