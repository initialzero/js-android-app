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

    private int space;

    /**
     * @param space distance between item in dp
     */
    public ItemSpaceDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = space;

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildPosition(view) == 0)
            outRect.top = space;
    }
}
