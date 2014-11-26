package com.sismics.docs.ui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * A circular button made of paper that lifts and emits ink reactions on press.
 * <p/>
 * This widget supports two sizes: {@link #SIZE_NORMAL} and {@link #SIZE_MINI}
 * according to <a href="http://www.google.com/design/spec/patterns/promoted-actions.html">Promoted Actions</a> pattern.
 * <p/>
 * Like an {@link ImageView} this widget require {@code android:src} attribute.
 * According to official documentation this drawable should be not more than {@code 24dp}.
 * <p/>
 * Use theme to customize all floating buttons in your app:
 * <p/>
 * Declare own style:
 * <pre>
 * &lt;style name=&quot;AppTheme.Fab&quot; parent=&quot;FloatingActionButton&quot;&gt;
 *   &lt;item name=&quot;floatingActionButtonColor&quot;&gt;@color/my_fab_color&lt;/item&gt;
 * &lt;/style&gt;
 * </pre>
 * Link this style in your theme:
 * <pre>
 * &lt;style name=&quot;AppTheme&quot; parent=&quot;android:Theme&quot;&gt;
 *   &lt;item name=&quot;floatingActionButtonStyle&quot;&gt;@style/AppTheme.Fab&lt;/item&gt;
 * &lt;/style&gt;
 * </pre>
 * <p/>
 * Customizing in layout.xml:
 * <pre>
 * &lt;com.shamanland.fab.FloatingActionButton
 *   android:layout_width=&quot;wrap_content&quot;
 *   android:layout_height=&quot;wrap_content&quot;
 *   android:src=&quot;@drawable/ic_action_my&quot;
 *   app:floatingActionButtonColor=&quot;@color/my_fab_color&quot;
 *   app:floatingActionButtonSize=&quot;mini&quot;
 *   /&gt;
 * </pre>
 * <p/>
 * Customizing in java-code:
 * <pre>
 * FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
 * fab.setSize(FloatingActionButton.SIZE_MINI);
 * fab.setColor(Color.RED);
 * // NOTE invoke this method after setting new values!
 * fab.initBackground();
 * // NOTE standard method of ImageView
 * fab.setImageResource(R.drawable.ic_action_my);
 * </pre>
 */
public class FloatingActionButton extends ImageButton {
    /**
     * Constant representing normal size {@code 56dp}. Value: 0x0
     */
    public static final int SIZE_NORMAL = 0;

    /**
     * Constant representing mini size {@code 40dp}. Value: 0x1
     */
    public static final int SIZE_MINI = 1;

    private int mSize;
    private int mColor;
    private ColorStateList mColorStateList;

    private GradientDrawable mCircleDrawable;

    /**
     * Gets abstract size of this button.
     *
     * @return {@link #SIZE_NORMAL} or {@link #SIZE_MINI}
     */
    public int getSize() {
        return mSize;
    }

    /**
     * Sets abstract size for this button.
     * <p/>
     * Xml attribute: {@code app:floatingActionButtonSize}
     *
     * @param size {@link #SIZE_NORMAL} or {@link #SIZE_MINI}
     */
    public void setSize(int size) {
        mSize = size;
    }

    /**
     * Gets background color of this button.
     *
     * @return color
     */
    public int getColor() {
        return mColor;
    }

    /**
     * Sets background color for this button.
     * <p/>
     * Xml attribute: {@code app:floatingActionButtonColor}
     *
     * @param color color
     */
    public void setColor(int color) {
        mColor = color;
    }

    /**
     * Gets color state list used as background for this button.
     *
     * @return may be null
     */
    public ColorStateList getColorStateList() {
        return mColorStateList;
    }

    /**
     * Sets color state list as background for this button.
     * <p/>
     * Xml attribute: {@code app:floatingActionButtonColor}
     *
     * @param colorStateList color
     */
    public void setColorStateList(ColorStateList colorStateList) {
        mColorStateList = colorStateList;
    }

    public FloatingActionButton(Context context) {
        super(context);
        init(context, null, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, com.shamanland.fab.R.attr.floatingActionButtonStyle);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a;

        try {
            if (isInEditMode()) {
                return;
            }

            if (attrs == null) {
                return;
            }

            Resources.Theme theme = context.getTheme();
            if (theme == null) {
                return;
            }

            a = theme.obtainStyledAttributes(attrs, com.shamanland.fab.R.styleable.FloatingActionButton, defStyle, com.shamanland.fab.R.style.FloatingActionButton_Dark);
            if (a == null) {
                return;
            }
        } finally {
            mSize = SIZE_NORMAL;
            mColor = Color.GRAY;
            mColorStateList = null;
        }

        try {
            initAttrs(a);
        } finally {
            a.recycle();
        }

        initBackground();
    }

    private void initAttrs(TypedArray a) {
        setSize(a.getInteger(com.shamanland.fab.R.styleable.FloatingActionButton_floatingActionButtonSize, SIZE_NORMAL));
        setColor(a.getColor(com.shamanland.fab.R.styleable.FloatingActionButton_floatingActionButtonColor, Color.GRAY));
        setColorStateList(a.getColorStateList(com.shamanland.fab.R.styleable.FloatingActionButton_floatingActionButtonColor));
    }

    /**
     * Inflate and initialize background drawable for this view with arguments
     * inflated from xml or specified using {@link #setSize(int)} or {@link #setColor(int)}
     * <p/>
     * Invoked from constructor, but it's allowed to invoke this method manually from code.
     */
    public void initBackground() {
        final int backgroundId;

        if (mSize == SIZE_MINI) {
            backgroundId = com.shamanland.fab.R.drawable.com_shamanland_fab_circle_mini;
        } else {
            backgroundId = com.shamanland.fab.R.drawable.com_shamanland_fab_circle_normal;
        }

        Drawable background = getResources().getDrawable(backgroundId);

        if (background instanceof LayerDrawable) {
            LayerDrawable layers = (LayerDrawable) background;
            if (layers.getNumberOfLayers() == 2) {
                Drawable shadow = layers.getDrawable(0);
                Drawable circle = layers.getDrawable(1);

                if (shadow instanceof GradientDrawable) {
                    ((GradientDrawable) shadow.mutate()).setGradientRadius(getShadowRadius(shadow, circle));
                }

                if (circle instanceof GradientDrawable) {
                    mCircleDrawable = (GradientDrawable) circle.mutate();
                    mCircleDrawable.setColor(mColor);
                }
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            //noinspection deprecation
            setBackgroundDrawable(background);
        } else {
            setBackground(background);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        if (mCircleDrawable != null && mColorStateList != null) {
            mCircleDrawable.setColor(mColorStateList.getColorForState(getDrawableState(), mColor));

            // NOTE maybe this line is required only for Gingerbread
            invalidate();
        }
    }

    /**
     * Calculates required radius of shadow.
     *
     * @param shadow underlay drawable
     * @param circle overlay drawable
     * @return calculated radius, always >= 1
     */
    protected static int getShadowRadius(Drawable shadow, Drawable circle) {
        int radius = 0;

        if (shadow != null && circle != null) {
            Rect rect = new Rect();
            radius = (circle.getIntrinsicWidth() + (shadow.getPadding(rect) ? rect.left + rect.right : 0)) / 2;
        }

        return Math.max(1, radius);
    }
}