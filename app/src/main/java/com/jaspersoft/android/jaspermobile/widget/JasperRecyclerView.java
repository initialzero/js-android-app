package com.jaspersoft.android.jaspermobile.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.DividerGridItemDecoration;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.DividerListItemDecoration;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceAdapter;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class JasperRecyclerView extends RecyclerView {
    private final static int MIN_COLUMN_COUNT = 2;

    private int columnWidth;
    private RecyclerView.ItemDecoration decoration;

    public JasperRecyclerView(Context context) {
        super(context);
        init(context, null);
    }

    public JasperRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public JasperRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            int[] attrsArray = {android.R.attr.columnWidth};
            TypedArray array = context.obtainStyledAttributes(attrs, attrsArray);
            columnWidth = array.getDimensionPixelSize(0, -1);
            array.recycle();
        }
        setHasFixedSize(true);
        getItemAnimator().setChangeDuration(0);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);

        LayoutManager layoutManager = getLayoutManager();
        if (columnWidth > 0 && layoutManager instanceof GridLayoutManager) {
            int spanCount = Math.max(MIN_COLUMN_COUNT, getMeasuredWidth() / columnWidth);
            ((GridLayoutManager) layoutManager).setSpanCount(spanCount);

            if (decoration != null) {
                ((DividerGridItemDecoration) decoration).setSpanCount(spanCount);
            }
        }
    }

    public void setViewType(ViewType viewType) {
        final RecyclerView.LayoutManager listLayoutManager;
        removeItemDecoration(decoration);

        if (viewType == ViewType.LIST) {
            listLayoutManager = new LinearLayoutManager(getContext());
            decoration = new DividerListItemDecoration();
        } else {
            listLayoutManager = new GridLayoutManager(getContext(), MIN_COLUMN_COUNT);
            ((GridLayoutManager) listLayoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    RecyclerView.Adapter adapter = getAdapter();
                    if (adapter != null) {
                        return adapter.getItemViewType(position) == JasperResourceAdapter.LOADING_TYPE ? ((GridLayoutManager) listLayoutManager).getSpanCount() : 1 ;
                    }
                    return 1;
                }
            });
            decoration = new DividerGridItemDecoration(MIN_COLUMN_COUNT);
        }

        setLayoutManager(listLayoutManager);
        addItemDecoration(decoration);
    }
}
