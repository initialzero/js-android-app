package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

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

    public DividerGridItemDecoration() {
        this.spanCount = 1;
    }

    public void setSpanCount(int spanCount) {
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int dividerSize = (int) parent.getContext().getResources().getDimension(R.dimen.resource_item_padding);

        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;

        outRect.left = dividerSize - column * dividerSize / spanCount;
        outRect.right = (column + 1) * dividerSize / spanCount;
        outRect.bottom = dividerSize;

        // Add top margin only for the first items row to avoid double space between items
        if (position < spanCount) {
            outRect.top = dividerSize;
        }
    }
}
