package com.sismics.docs.ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * ViewPager for files.
 *
 * @author bgamard.
 */
public class FileViewPager extends ViewPager {

    public FileViewPager(Context context) {
        super(context);
    }

    public FileViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ImageViewTouch) {
            return ((ImageViewTouch) v).canScroll(dx);
        } else {
            return super.canScroll(v, checkV, dx, x, y);
        }
    }
}