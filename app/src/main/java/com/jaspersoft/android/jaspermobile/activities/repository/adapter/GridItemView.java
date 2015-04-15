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

package com.jaspersoft.android.jaspermobile.activities.repository.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.widget.CheckedRelativeLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;

@EViewGroup(R.layout.res_grid_item)
public class GridItemView extends CheckedRelativeLayout implements ResourceView {

    @ViewById(android.R.id.icon)
    protected ImageView mImageIcon;
    @ViewById(R.id.kpiImage)
    protected ImageView kpiImage;
    @ViewById(android.R.id.text1)
    protected TextView mTitleTxt;
    @ViewById(android.R.id.text2)
    protected TextView mSubTitle;
    @ViewById(R.id.timestampStub)
    protected ViewStub mTimestampStub;
    @ViewById(R.id.miscStub)
    protected ViewStub mMiscStub;
    @ViewById(R.id.misc)
    protected TextView mMiscTextView;

    protected TextView mTimestampTxt;
    protected TextView mMiscTxt;

    @DimensionPixelSizeRes(R.dimen.grid_item_size)
    protected int mSize;

    public GridItemView(Context context) {
        this(context, null);
    }

    public GridItemView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.gridLayoutStyle);
    }

    public GridItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @AfterViews
    final void init() {
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(mSize, mSize);
        setLayoutParams(params);
    }

    @NonNull
    @Override
    public TextView getTitleView() {
        return mTitleTxt;
    }

    @Nullable
    @Override
    public TextView getSubTitleView() {
        return mSubTitle;
    }

    @Nullable
    @Override
    public TextView getTimeStampView() {
        if (mTimestampStub != null && mTimestampTxt == null) {
            mTimestampTxt = (TextView) mTimestampStub.inflate();
        }
        return mTimestampTxt;
    }

    @Nullable
    @Override
    public TextView getMiscView() {
        if (mMiscTextView != null) {
            return mMiscTextView;
        }
        if (mMiscStub != null && mMiscTxt == null) {
            mMiscTxt = (TextView) mMiscStub.inflate();
        }
        return mMiscTxt;
    }

    @Nullable
    @Override
    public ImageView getKpiImage() {
        return kpiImage;
    }

    @NonNull
    @Override
    public ImageView getImageView() {
        return mImageIcon;
    }

}
