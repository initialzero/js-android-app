package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class JasperResourceAdapter extends SelectableAdapter<String> {

    public final static int LOADING_TYPE = -1;

    private OnResourceInteractionListener mItemInteractionListener;
    private List<JasperResource> jasperResources;
    private ViewType viewType;
    private boolean mNextPageIsLoading;

    public JasperResourceAdapter(List<JasperResource> jasperResources, ViewType viewType) {
        if (jasperResources != null) {
            this.jasperResources = jasperResources;
        } else {
            this.jasperResources = new ArrayList<>();
        }
        this.viewType = viewType;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int resourceType) {
        if (resourceType == LOADING_TYPE) {
            View itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.item_resource_list_loading, parent, false);
            return new LoadingViewHolder(itemView);
        }
        ResourceViewBinder resourceViewBinder = getResViewBinderForResType(parent.getContext(), JasperResourceType.values()[resourceType], viewType);
        BaseViewHolder itemViewHolder = resourceViewBinder.buildViewHolder(parent);
        itemViewHolder.setOnItemInteractionListener(new OnResourceItemClickListener());
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder baseViewHolder, int position) {
        if (position == jasperResources.size()) {
            return;
        }
        boolean isSelected = mResourceSelector != null && mResourceSelector.isSelected(position);
        baseViewHolder.populateView(jasperResources.get(position), isSelected);
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
        }
        JasperResource resource = jasperResources.get(position);
        return resource.getResourceType().ordinal();
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
        notifyItemInserted(jasperResources.size());
    }

    private ResourceViewBinder getResViewBinderForResType(Context context, JasperResourceType jasperResourceType, ViewType viewType) {
        switch (jasperResourceType) {
            case report:
                return new ReportViewBinder(context, viewType);
            case dashboard:
                return new DashboardViewBinder(context, viewType);
            case folder:
                return new FolderViewBinder(context, viewType);
            case saved_item:
                return new SavedItemViewBinder(context, viewType);
            default:
                return new UndefinedViewBinder(context, viewType);
        }
    }

    @Override
    public String getItemKey(int position) {
        if (jasperResources.size() <= position) return null;

        JasperResource jasperResource = jasperResources.get(position);
        if (jasperResource != null) {
            return jasperResources.get(position).getId();
        }
        return null;
    }

    //---------------------------------------------------------------------
    // Base adapter interaction listener
    //---------------------------------------------------------------------
    public interface OnResourceInteractionListener {
        void onResourceItemClicked(String id);
    }

    private class OnResourceItemClickListener implements BaseViewHolder.OnViewClickListener {

        @Override
        public void onViewSingleClick(int position) {
            if (mItemInteractionListener != null) {
                mItemInteractionListener.onResourceItemClicked(jasperResources.get(position).getId());
            }
        }

        @Override
        public void onViewLongClick(int position) {
            if (mResourceSelector != null) {
                mResourceSelector.changeSelectedState(position);
            }
        }
    }

    private class LoadingViewHolder extends BaseViewHolder {

        public LoadingViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void populateView(JasperResource resource, boolean isSelected) {

        }
    }
}
