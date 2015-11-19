package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class DividerListItemDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int dividerHeight = (int) parent.getContext().getResources().getDimension(R.dimen.resource_item_padding);

        outRect.left = dividerHeight;
        outRect.right = dividerHeight;
        outRect.bottom = dividerHeight;

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = dividerHeight;
        }
    }
}
