/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class DividerGridItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int mDividerSize;

    public DividerGridItemDecoration(int dividerSize) {
        this.spanCount = 1;
        mDividerSize = dividerSize;
    }

    public void setSpanCount(int spanCount) {
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;

        outRect.left = mDividerSize - column * mDividerSize / spanCount;
        outRect.right = (column + 1) * mDividerSize / spanCount;
        outRect.bottom = mDividerSize;

        // Add top margin only for the first items row to avoid double space between items
        if (position < spanCount) {
            outRect.top = mDividerSize;
        }
    }
}
