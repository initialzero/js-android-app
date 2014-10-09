/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class AutoLayerTextView extends TextView {
    public AutoLayerTextView(Context context) {
        super(context);
    }

    public AutoLayerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoLayerTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    public void setBackgroundDrawable(Drawable d) {
        if (d == null) return;
        AutoLayerDrawable layer = new AutoLayerDrawable(d);
        super.setBackgroundDrawable(layer);
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    public void setBackground(Drawable d) {
        if (d == null) return;
        AutoLayerDrawable layer = new AutoLayerDrawable(d);

        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            super.setBackgroundDrawable(layer);
        } else {
            super.setBackground(layer);
        }
    }

    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(Drawable leftOriginal, Drawable topOriginal,
                                                        Drawable rightOriginal, Drawable bottomOriginal) {
        Drawable left = leftOriginal;
        Drawable top = topOriginal;
        Drawable right = rightOriginal;
        Drawable bottom = bottomOriginal;

        if (leftOriginal != null) {
            left = new AutoLayerDrawable(leftOriginal);
        }
        if (rightOriginal != null) {
            right = new AutoLayerDrawable(rightOriginal);
        }
        if (topOriginal != null) {
            top = new AutoLayerDrawable(topOriginal);
        }
        if (bottomOriginal != null) {
            bottom = new AutoLayerDrawable(bottomOriginal);
        }
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
    }
}