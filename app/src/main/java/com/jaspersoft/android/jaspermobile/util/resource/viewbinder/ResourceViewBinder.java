package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.view.ViewGroup;

import com.jaspersoft.android.jaspermobile.util.ViewType;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public abstract class ResourceViewBinder implements ResourcePresentationProvider {

    protected Context mContext;
    private ViewType mViewType;

    public ResourceViewBinder(Context context, ViewType mViewType) {
        this.mContext = context;
        this.mViewType = mViewType;
    }

    public BaseViewHolder buildViewHolder(ViewGroup parent){
        if (mViewType == ViewType.LIST) {
            return provideListViewHolder(parent);
        } else {
            return provideGridViewHolder(parent);
        }
    }
}
