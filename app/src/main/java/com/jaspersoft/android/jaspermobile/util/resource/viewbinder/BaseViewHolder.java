package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    /**
     * Fill resource view with data
     * @param resource data to be displayed in UI
     */
    public abstract void populateView(JasperResource resource);
}
