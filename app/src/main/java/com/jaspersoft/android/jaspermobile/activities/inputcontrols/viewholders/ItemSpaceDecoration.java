package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Add spaces between item in recycler view
 * @author Andrew Tivodar
 * @since 2.2
 */
public class ItemSpaceDecoration extends RecyclerView.ItemDecoration {

    private int mDividerHeight;
    private int mTopPadding;

    /**
     * @param space distance between item in dp
     */
    public ItemSpaceDecoration(int space) {
        this.mDividerHeight = space;
    }

    public ItemSpaceDecoration(int dividerHeight, int topPadding) {
        this.mDividerHeight = dividerHeight;
        this.mTopPadding = topPadding;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = mDividerHeight;

        if (parent.getChildLayoutPosition(view) == 0)
            outRect.top = mTopPadding;
    }
}
