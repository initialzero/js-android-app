/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.view.ViewGroup;

import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.widget.JasperRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class JasperResourceAdapter extends JasperRecyclerView.Adapter<BaseResourceViewHolder> {

    private OnResourceInteractionListener mItemInteractionListener;
    private List<JasperResource> jasperResources;
    private boolean mIsLoading;
    private boolean mHideSecondaryAction;
    private ResourceViewHolderFactory mResourceViewHolderFactory;
    private ResourceBinderFactory mResourceBinderFactory;

    public JasperResourceAdapter(Context context) {
        this.jasperResources = new ArrayList<>();

        mResourceViewHolderFactory = new ResourceViewHolderFactory(context);
        mResourceBinderFactory = new ResourceBinderFactory(context);
    }

    public void setSecondaryActionHidden(boolean hideSecondaryAction) {
        mHideSecondaryAction = hideSecondaryAction;
    }

    @Override
    public BaseResourceViewHolder onCreateViewHolder(ViewGroup parent, int resourceType) {
        BaseResourceViewHolder itemViewHolder = mResourceViewHolderFactory.create(parent, resourceType);
        itemViewHolder.setOnItemInteractionListener(new OnResourceItemClickListener());
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(BaseResourceViewHolder baseViewHolder, int position) {
        if (getItemViewType(position) == LOADING_TYPE) {
            return;
        }
        JasperResource jasperResource = jasperResources.get(position);
        ResourceBinder resourceBinder = mResourceBinderFactory.create(jasperResource.getResourceType());
        resourceBinder.bindView(baseViewHolder, jasperResource);
        resourceBinder.setActionResource(baseViewHolder, mHideSecondaryAction ? null : jasperResource);
    }

    @Override
    public int getItemCount() {
        return (mIsLoading && jasperResources.size() != 0) ? jasperResources.size() + 1 : jasperResources.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position < jasperResources.size() ? getViewType() : LOADING_TYPE;
    }

    public void setOnItemInteractionListener(OnResourceInteractionListener itemInteractionListener) {
        this.mItemInteractionListener = itemInteractionListener;
    }

    public void setResources(List<JasperResource> jasperResources) {
        int pos = this.jasperResources.size();
        int notifyCount = jasperResources.size() - pos;
        this.jasperResources = jasperResources;
        notifyItemRangeInserted(pos, notifyCount);
    }

    public void addAll(List<JasperResource> jasperResources) {
        int pos = this.jasperResources.size();
        this.jasperResources.addAll(jasperResources);
        notifyItemRangeInserted(pos, jasperResources.size());
    }

    public void clear() {
        jasperResources = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void showLoading() {
        if (mIsLoading) return;

        mIsLoading = true;
        notifyItemInserted(jasperResources.size());
    }

    public void hideLoading() {
        if (!mIsLoading) return;

        mIsLoading = false;
        notifyItemRemoved(jasperResources.size());
    }

    public void remove(JasperResource jasperResource) {
        int index = jasperResources.indexOf(jasperResource);
        boolean removed = jasperResources.remove(jasperResource);
        if (removed) {
            notifyItemRemoved(index);
        }
    }

    //---------------------------------------------------------------------
    // Base adapter interaction listener
    //---------------------------------------------------------------------

    public interface OnResourceInteractionListener {
        void onResourceItemClicked(JasperResource jasperResource);

        void onSecondaryActionClicked(JasperResource jasperResource);
    }

    private class OnResourceItemClickListener implements BaseResourceViewHolder.OnViewClickListener {

        @Override
        public void onViewSingleClick(int position) {
            if (mItemInteractionListener != null) {
                mItemInteractionListener.onResourceItemClicked(jasperResources.get(position));
            }
        }

        @Override
        public void onSecondaryActionClick(int position) {
            if (mItemInteractionListener != null) {
                JasperResource jasperResource = jasperResources.get(position);
                mItemInteractionListener.onSecondaryActionClicked(jasperResource);
            }
        }
    }

}
