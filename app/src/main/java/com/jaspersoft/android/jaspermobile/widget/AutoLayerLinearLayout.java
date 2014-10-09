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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class AutoLayerLinearLayout extends LinearLayout {

    private final Drawable mForegroundSelector;

    //---------------------------------------------------------------------
    // Constructors
    //---------------------------------------------------------------------

    public AutoLayerLinearLayout(Context context) {
        this(context, null);
    }

    public AutoLayerLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoLayerLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (isInEditMode()) {
            mForegroundSelector = getResources().getDrawable(android.R.drawable.list_selector_background);
        } else {
            TypedArray typedAttributes = context.obtainStyledAttributes(attrs, R.styleable.AutoLayerLayout, 0, 0);
            mForegroundSelector = typedAttributes.getDrawable(R.styleable.AutoLayerLayout_foregroundSelector);
            mForegroundSelector.setCallback(this);
            typedAttributes.recycle();
        }
    }

    //---------------------------------------------------------------------
    // Public methods
    //---------------------------------------------------------------------

    public Drawable getForegroundSelector() {
        return mForegroundSelector;
    }

    @TargetApi(11)
    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mForegroundSelector != null) {
            mForegroundSelector.jumpToCurrentState();
        }
    }

    //---------------------------------------------------------------------
    // Protected methods
    //---------------------------------------------------------------------

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mForegroundSelector != null) {
            mForegroundSelector.setState(getDrawableState());
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (mForegroundSelector != null) {
            mForegroundSelector.setBounds(0, 0, width, height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mForegroundSelector.draw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mForegroundSelector != null) {
            mForegroundSelector.draw(canvas);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || (who == mForegroundSelector);
    }

}
