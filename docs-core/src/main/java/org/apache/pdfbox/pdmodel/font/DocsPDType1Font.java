package org.apache.pdfbox.pdmodel.font;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.EncodedFont;
import org.apache.fontbox.FontBoxFont;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.encoding.*;
import org.apache.pdfbox.util.Matrix;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.pdfbox.pdmodel.font.UniUtil.getUniNameOfCodePoint;

/**
 * Safe non-crashing font even if no glyph are present.
 * Will replace unknown glyphs by a space.
 *
 * @author bgamard
 */
public class DocsPDType1Font extends PDSimpleFont {
    private static final Log LOG = LogFactory.getLog(DocsPDType1Font.class);

    // alternative names for glyphs which are commonly encountered
    private static final Map<String, String> ALT_NAMES = new HashMap<>();

    static {
        ALT_NAMES.put("ff", "f_f");
        ALT_NAMES.put("ffi", "f_f_i");
        ALT_NAMES.put("ffl", "f_f_l");
        ALT_NAMES.put("fi", "f_i");
        ALT_NAMES.put("fl", "f_l");
        ALT_NAMES.put("st", "s_t");
        ALT_NAMES.put("IJ", "I_J");
        ALT_NAMES.put("ij", "i_j");
        ALT_NAMES.put("ellipsis", "elipsis"); // misspelled in ArialMT
    }

    public static final DocsPDType1Font HELVETICA = new DocsPDType1Font("Helvetica");
    public static final DocsPDType1Font HELVETICA_BOLD = new DocsPDType1Font("Helvetica-Bold");

    /**
     * embedded or system font for rendering.
     */
    private final FontBoxFont genericFont;

    private final boolean isEmbedded;
    private final boolean isDamaged;
    private Matrix fontMatrix;
    private final AffineTransform fontMatrixTransform;
    private BoundingBox fontBBox;

    /**
     * to improve encoding speed.
     */
    private final Map<Integer, byte[]> codeToBytesMap;

    /**
     * Creates a Type 1 standard 14 font for embedding.
     *
     * @param baseFont One of the standard 14 PostScript names
     */
    private DocsPDType1Font(String baseFont) {
        super(baseFont);

        dict.setItem(COSName.SUBTYPE, COSName.TYPE1);
        dict.setName(COSName.BASE_FONT, baseFont);
        if ("ZapfDingbats".equals(baseFont)) {
            encoding = ZapfDingbatsEncoding.INSTANCE;
        } else if ("Symbol".equals(baseFont)) {
            encoding = SymbolEncoding.INSTANCE;
        } else {
            encoding = WinAnsiEncoding.INSTANCE;
            dict.setItem(COSName.ENCODING, COSName.WIN_ANSI_ENCODING);
        }

        // standard 14 fonts may be accessed concurrently, as they are singletons
        codeToBytesMap = new ConcurrentHashMap<>();

        FontMapping<FontBoxFont> mapping = FontMappers.instance()
                .getFontBoxFont(getBaseFont(),
                        getFontDescriptor());
        genericFont = mapping.getFont();

        if (mapping.isFallback()) {
            String fontName;
            try {
                fontName = genericFont.getName();
            } catch (IOException e) {
                fontName = "?";
            }
            LOG.warn("Using fallback font " + fontName + " for base font " + getBaseFont());
        }
        isEmbedded = false;
        isDamaged = false;
        fontMatrixTransform = new AffineTransform();
    }

    /**
     * Returns the PostScript name of the font.
     */
    private String getBaseFont() {
        return dict.getNameAsString(COSName.BASE_FONT);
    }

    @Override
    public float getHeight(int code) throws IOException {
        String name = codeToName(code);
        if (getStandard14AFM() != null) {
            String afmName = getEncoding().getName(code);
            return getStandard14AFM().getCharacterHeight(afmName);
        } else {
            return (float) genericFont.getPath(name).getBounds().getHeight();
        }
    }

    @Override
    protected byte[] encode(int unicode) throws IOException {
        byte[] bytes = codeToBytesMap.get(unicode);
        if (bytes != null) {
            return bytes;
        }

        String name = getGlyphList().codePointToName(unicode);
        if (isStandard14()) {
            // genericFont not needed, thus simplified code
            // this is important on systems with no installed fonts
            if (!encoding.contains(name)) {
                return " ".getBytes();
            }
            if (".notdef".equals(name)) {
                return " ".getBytes();
            }
        } else {
            if (!encoding.contains(name)) {
                return " ".getBytes();
            }

            String nameInFont = getNameInFont(name);

            if (nameInFont.equals(".notdef") || !genericFont.hasGlyph(nameInFont)) {
                return " ".getBytes();
            }
        }

        Map<String, Integer> inverted = encoding.getNameToCodeMap();
        int code = inverted.get(name);
        bytes = new byte[]{(byte) code};
        codeToBytesMap.put(code, bytes);
        return bytes;
    }

    @Override
    public float getWidthFromFont(int code) throws IOException {
        String name = codeToName(code);

        // width of .notdef is ignored for substitutes, see PDFBOX-1900
        if (!isEmbedded && ".notdef".equals(name)) {
            return 250;
        }
        float width = genericFont.getWidth(name);

        Point2D p = new Point2D.Float(width, 0);
        fontMatrixTransform.transform(p, p);
        return (float) p.getX();
    }

    @Override
    public boolean isEmbedded() {
        return isEmbedded;
    }

    @Override
    public float getAverageFontWidth() {
        if (getStandard14AFM() != null) {
            return getStandard14AFM().getAverageCharacterWidth();
        } else {
            return super.getAverageFontWidth();
        }
    }

    @Override
    public int readCode(InputStream in) throws IOException {
        return in.read();
    }

    @Override
    protected Encoding readEncodingFromFont() throws IOException {
        if (!isEmbedded() && getStandard14AFM() != null) {
            // read from AFM
            return new Type1Encoding(getStandard14AFM());
        } else {
            // extract from Type1 font/substitute
            if (genericFont instanceof EncodedFont) {
                return Type1Encoding.fromFontBox(((EncodedFont) genericFont).getEncoding());
            } else {
                // default (only happens with TTFs)
                return StandardEncoding.INSTANCE;
            }
        }
    }

    @Override
    public FontBoxFont getFontBoxFont() {
        return genericFont;
    }

    @Override
    public String getName() {
        return getBaseFont();
    }

    @Override
    public BoundingBox getBoundingBox() throws IOException {
        if (fontBBox == null) {
            fontBBox = generateBoundingBox();
        }
        return fontBBox;
    }

    private BoundingBox generateBoundingBox() throws IOException {
        if (getFontDescriptor() != null) {
            PDRectangle bbox = getFontDescriptor().getFontBoundingBox();
            if (bbox != null &&
                    (bbox.getLowerLeftX() != 0 || bbox.getLowerLeftY() != 0 ||
                            bbox.getUpperRightX() != 0 || bbox.getUpperRightY() != 0)) {
                return new BoundingBox(bbox.getLowerLeftX(), bbox.getLowerLeftY(),
                        bbox.getUpperRightX(), bbox.getUpperRightY());
            }
        }
        return genericFont.getFontBBox();
    }

    private String codeToName(int code) throws IOException {
        String name = getEncoding().getName(code);
        return getNameInFont(name);
    }

    /**
     * Maps a PostScript glyph name to the name in the underlying font, for example when
     * using a TTF font we might map "W" to "uni0057".
     */
    private String getNameInFont(String name) throws IOException {
        if (isEmbedded() || genericFont.hasGlyph(name)) {
            return name;
        } else {
            // try alternative name
            String altName = ALT_NAMES.get(name);
            if (altName != null && !name.equals(".notdef") && genericFont.hasGlyph(altName)) {
                return altName;
            } else {
                // try unicode name
                String unicodes = getGlyphList().toUnicode(name);
                if (unicodes != null && unicodes.length() == 1) {
                    String uniName = getUniNameOfCodePoint(unicodes.codePointAt(0));
                    if (genericFont.hasGlyph(uniName)) {
                        return uniName;
                    }
                }
            }
        }
        return ".notdef";
    }

    @Override
    public GeneralPath getPath(String name) throws IOException {
        // Acrobat does not draw .notdef for Type 1 fonts, see PDFBOX-2421
        // I suspect that it does do this for embedded fonts though, but this is untested
        if (name.equals(".notdef") && !isEmbedded) {
            return new GeneralPath();
        } else {
            return genericFont.getPath(getNameInFont(name));
        }
    }

    @Override
    public boolean hasGlyph(String name) throws IOException {
        return genericFont.hasGlyph(getNameInFont(name));
    }

    @Override
    public final Matrix getFontMatrix() {
        if (fontMatrix == null) {
            // PDF specified that Type 1 fonts use a 1000upem matrix, but some fonts specify
            // their own custom matrix anyway, for example PDFBOX-2298
            List<Number> numbers = null;
            try {
                numbers = genericFont.getFontMatrix();
            } catch (IOException e) {
                fontMatrix = DEFAULT_FONT_MATRIX;
            }

            if (numbers != null && numbers.size() == 6) {
                fontMatrix = new Matrix(
                        numbers.get(0).floatValue(), numbers.get(1).floatValue(),
                        numbers.get(2).floatValue(), numbers.get(3).floatValue(),
                        numbers.get(4).floatValue(), numbers.get(5).floatValue());
            } else {
                return super.getFontMatrix();
            }
        }
        return fontMatrix;
    }

    @Override
    public boolean isDamaged() {
        return isDamaged;
    }
}
