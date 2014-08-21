/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class LinearCardView extends AutoLayerLinearLayout {

    private final View mViewHeader;
    private final TextView mTitleTxt;
    private final TextView mSubTitleTxt;

    private Drawable mHeaderDrawable;
    private String mHeaderTitle;
    private String mHeaderSubTitle;

    //---------------------------------------------------------------------
    // Constructors
    //---------------------------------------------------------------------

    public LinearCardView(Context context) {
        this(context, null);
    }

    public LinearCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray typedAttributes = context.obtainStyledAttributes(attrs, R.styleable.LinearCardView, 0, 0);
        mHeaderDrawable = typedAttributes.getDrawable(R.styleable.LinearCardView_header_background);
        mHeaderTitle = typedAttributes.getString(R.styleable.LinearCardView_body_title);
        mHeaderSubTitle = typedAttributes.getString(R.styleable.LinearCardView_body_subtitle);
        typedAttributes.recycle();

        LayoutInflater.from(context).inflate(R.layout.linear_card_layout, this);
        mViewHeader = findViewById(R.id.card_header);
        mTitleTxt = (TextView) findViewById(R.id.card_title_txt);
        mSubTitleTxt = (TextView) findViewById(R.id.card_subtitle_txt);

        setSupportBackground(mViewHeader, mHeaderDrawable);
        mTitleTxt.setText(mHeaderTitle);
        mSubTitleTxt.setText(mHeaderSubTitle);
    }

    //---------------------------------------------------------------------
    // Public methods
    //---------------------------------------------------------------------

    public void setBodyTitle(String title) {
        mHeaderTitle = title;
        if (mTitleTxt != null) {
            mTitleTxt.setText(mHeaderTitle);
        }
    }

    public void setBodySubTitle(String subTitle) {
        mHeaderSubTitle = subTitle;
        if (mSubTitleTxt != null) {
            mSubTitleTxt.setText(mHeaderSubTitle);
        }
    }

    public void setHeaderDrawable(Drawable drawable) {
        mHeaderDrawable = drawable;
        setSupportBackground(mViewHeader, drawable);
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void setSupportBackground(View view, Drawable background) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(background);
        } else {
            view.setBackground(background);
        }
    }

}
