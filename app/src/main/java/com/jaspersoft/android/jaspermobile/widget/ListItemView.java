/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.ResourceView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;

@EViewGroup(R.layout.res_list_item)
public class ListItemView extends CheckedRelativeLayout implements ResourceView {

    @ViewById(android.R.id.icon)
    protected ImageView mImageIcon;
    @ViewById(android.R.id.text1)
    protected TextView mTitleTxt;
    @ViewById(android.R.id.text2)
    protected TextView mSubTitle;
    @ViewById(R.id.timestampStub)
    protected ViewStub mTimestampStub;
    @ViewById(R.id.action)
    protected ImageButton mActionButton;

    protected TextView mTimestampTxt;

    @DimensionPixelSizeRes(R.dimen.list_item_height)
    protected int mHeight;

    public ListItemView(Context context) {
        this(context, null);
    }

    public ListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.listLayoutStyle);
    }

    public ListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @AfterViews
    final void init() {
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, mHeight);
        setLayoutParams(params);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitleTxt.setText(title);
    }

    @Override
    public void setSubTitle(CharSequence subTitle) {
        mSubTitle.setText(subTitle);
    }

    @Override
    public void setInfo(CharSequence timestamp) {
        if (mTimestampStub != null) {
            if (mTimestampTxt == null) {
                mTimestampTxt = (TextView) mTimestampStub.inflate();
            }
            mTimestampTxt.setText(timestamp);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTitleTxt.getLayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.addRule(START_OF, mTimestampTxt.getId());
            }
            params.addRule(LEFT_OF, mTimestampTxt.getId());
            mTitleTxt.setLayoutParams(params);
        }
    }

    @Override
    public void setAction(int actionImage, final ResourceActionListener resourceActionListener) {
        boolean isListenerProvided = resourceActionListener == null;

        mActionButton.setVisibility(isListenerProvided ? VISIBLE : GONE);
        if (isListenerProvided) return;

        mActionButton.setVisibility(VISIBLE);
        mActionButton.setImageResource(actionImage);
        mActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                    resourceActionListener.onResourceActionClick();
            }
        });
    }


    @Override
    public ImageView getImageView() {
        return mImageIcon;
    }

}
