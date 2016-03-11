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
    private int mViewType;
    private boolean mIsLoading;
    private ResourceViewHolderFactory mResourceViewHolderFactory;
    private ResourceBinderFactory mResourceBinderFactory;

    public JasperResourceAdapter(Context context, List<JasperResource> jasperResources) {
        this(context, jasperResources, ViewType.LIST);
    }

    public JasperResourceAdapter(Context context, List<JasperResource> jasperResources, ViewType viewType) {
        if (jasperResources != null) {
            this.jasperResources = jasperResources;
        } else {
            this.jasperResources = new ArrayList<>();
        }
        setViewType(viewType);

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
        if (getItemViewType(position) == LOADING_TYPE) {
            return;
        }
        JasperResource jasperResource = jasperResources.get(position);
        ResourceBinder resourceBinder = mResourceBinderFactory.create(jasperResource.getResourceType());
        resourceBinder.bindView(baseViewHolder, jasperResource);
    }

    @Override
    public int getItemCount() {
        return (mIsLoading && jasperResources.size() != 0) ? jasperResources.size() + 1 : jasperResources.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position < jasperResources.size() ? mViewType : LOADING_TYPE;
    }

    public void setOnItemInteractionListener(OnResourceInteractionListener itemInteractionListener) {
        this.mItemInteractionListener = itemInteractionListener;
    }

    public void setViewType(ViewType viewType) {
        this.mViewType = viewType == ViewType.LIST ? LIST_TYPE : GRID_TYPE;
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
