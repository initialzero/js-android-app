package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class JasperResourceAdapter extends RecyclerView.Adapter<BaseResourceViewHolder> {

    public final static int LOADING_TYPE = -1;
    public final static int LIST_TYPE = 1;
    public final static int GRID_TYPE = 2;

    private OnResourceInteractionListener mItemInteractionListener;
    private List<JasperResource> jasperResources;
    private ViewType viewType;
    private boolean mNextPageIsLoading;
    private ResourceViewHolderFactory mResourceViewHolderFactory;
    private ResourceBinderFactory mResourceBinderFactory;

    public JasperResourceAdapter(Context context, List<JasperResource> jasperResources, ViewType viewType) {
        if (jasperResources != null) {
            this.jasperResources = jasperResources;
        } else {
            this.jasperResources = new ArrayList<>();
        }
        this.viewType = viewType;

        mResourceViewHolderFactory = new ResourceViewHolderFactory(context);
        mResourceBinderFactory = new ResourceBinderFactory(context);
    }

    @Override
    public BaseResourceViewHolder onCreateViewHolder(ViewGroup parent, int resourceType) {
        BaseResourceViewHolder itemViewHolder = mResourceViewHolderFactory.create(parent, resourceType);
        itemViewHolder.setOnItemInteractionListener(new OnResourceItemClickListener());
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(BaseResourceViewHolder baseViewHolder, int position) {
        if (position == jasperResources.size()) {
            return;
        }
        JasperResource jasperResource = jasperResources.get(position);
        ResourceBinder resourceBinder = mResourceBinderFactory.create(jasperResource.getResourceType());
        resourceBinder.bindView(baseViewHolder, jasperResource);
    }

    @Override
    public int getItemCount() {
        int itemCount = jasperResources.size();
        if (mNextPageIsLoading && itemCount != 0) {
            itemCount++;
        }
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= jasperResources.size()) {
            return LOADING_TYPE;
        } else if (viewType == ViewType.LIST) {
            return LIST_TYPE;
        }
        return GRID_TYPE;
    }

    public void setOnItemInteractionListener(OnResourceInteractionListener itemInteractionListener) {
        this.mItemInteractionListener = itemInteractionListener;
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
        mNextPageIsLoading = true;
        notifyItemInserted(jasperResources.size());
    }

    public void hideLoading() {
        mNextPageIsLoading = false;
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
        void onResourceItemClicked(String id);
        void onSecondaryActionClicked(JasperResource jasperResource);
    }

    private class OnResourceItemClickListener implements BaseResourceViewHolder.OnViewClickListener {

        @Override
        public void onViewSingleClick(int position) {
            if (mItemInteractionListener != null) {
                mItemInteractionListener.onResourceItemClicked(jasperResources.get(position).getId());
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
