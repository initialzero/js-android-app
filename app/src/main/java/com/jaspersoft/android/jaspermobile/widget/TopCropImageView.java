/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class TopCropImageView extends ImageView {

    private ScaleType mScaleType;

    public enum ScaleType {
        MATRIX(0),
        FIT_XY(1),
        FIT_START(2),
        FIT_CENTER(3),
        FIT_END(4),
        CENTER(5),
        CENTER_CROP(6),
        CENTER_INSIDE(7),
        TOP_CROP(8);

        ScaleType(int ni) {
            nativeInt = ni;
        }

        final int nativeInt;
    }

    public TopCropImageView(Context context) {
        super(context);
    }

    public TopCropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TopCropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScaleType(ScaleType scaleType) {
        mScaleType = scaleType;
        if (scaleType == ScaleType.TOP_CROP) {
            super.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            super.setScaleType(ImageView.ScaleType.valueOf(scaleType.name()));
        }
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (mScaleType == ScaleType.TOP_CROP) {
            recomputeImgMatrix();
        }
        return changed;
    }

    private void recomputeImgMatrix() {
        final Matrix matrix = getImageMatrix();
        float scale;
        final int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
        final int vheight = getHeight() - getPaddingTop() - getPaddingBottom();
        final int dwidth = getDrawable().getIntrinsicWidth();
        final int dheight = getDrawable().getIntrinsicHeight();

        if (dwidth * vheight > vwidth * dheight) {
            scale = (float) vheight / (float) dheight;
        } else {
            scale = (float) vwidth / (float) dwidth;
        }
        matrix.setScale(scale, scale);
    }
}
