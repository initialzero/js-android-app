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
