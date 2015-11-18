package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.support.v7.widget.RecyclerView;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public abstract class SelectableAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> {

    protected ResourceSelector mResourceSelector;

    public final void setResourceSelector(ResourceSelector mResourceSelector) {
        this.mResourceSelector = mResourceSelector;
    }

    public abstract T getItemKey(int position);
}
