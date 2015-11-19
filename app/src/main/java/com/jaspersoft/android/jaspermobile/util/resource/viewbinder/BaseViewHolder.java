package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

    protected OnViewClickListener mItemInteractionListener;

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public void setOnItemInteractionListener(OnViewClickListener itemInteractionListener){
        this.mItemInteractionListener = itemInteractionListener;
    }

    /**
     * Fill resource view with data
     * @param resource data to be displayed in UI
     * @param isSelected is item selected
     */
    public abstract void populateView(JasperResource resource, boolean isSelected);

    //---------------------------------------------------------------------
    // Base ViewHolder's click listener
    //---------------------------------------------------------------------
    public interface OnViewClickListener {
        void onViewSingleClick(int position);
        void onViewInfoClick(int position);
    }
}
