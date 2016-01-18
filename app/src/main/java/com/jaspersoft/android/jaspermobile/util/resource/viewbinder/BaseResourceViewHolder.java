package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public abstract class BaseResourceViewHolder extends RecyclerView.ViewHolder implements ResourceView{

    protected OnViewClickListener mItemInteractionListener;

    public BaseResourceViewHolder(View itemView) {
        super(itemView);
    }

    public void setOnItemInteractionListener(OnViewClickListener itemInteractionListener){
        this.mItemInteractionListener = itemInteractionListener;
    }

    //---------------------------------------------------------------------
    // Base ViewHolder's click listener
    //---------------------------------------------------------------------
    public interface OnViewClickListener {
        void onViewSingleClick(int position);
        void onViewInfoClick(int position);
    }
}
