package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.view.ViewGroup;

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

    private OnResourceInteractionListener mItemInteractionListener;
    private List<JasperResource> jasperResources;
    private ViewType viewType;

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
        ResourceViewBinder resourceViewBinder = getResViewBinderForResType(parent.getContext(), JasperResourceType.values()[resourceType], viewType);
        BaseViewHolder itemViewHolder = resourceViewBinder.buildViewHolder(parent);
        itemViewHolder.setOnItemInteractionListener(new OnResourceItemClickListener());
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder baseViewHolder, int position) {
        boolean isSelected = mResourceSelector != null && mResourceSelector.isSelected(position);
        baseViewHolder.populateView(jasperResources.get(position), isSelected);
    }

    @Override
    public int getItemCount() {
        return jasperResources.size();
    }

    @Override
    public int getItemViewType(int position) {
        JasperResource resource = jasperResources.get(position);
        return resource.getResourceType().ordinal();
    }

    public void setOnItemInteractionListener(OnResourceInteractionListener itemInteractionListener) {
        this.mItemInteractionListener = itemInteractionListener;
    }

    public void addAll(List<JasperResource> jasperResources) {
        this.jasperResources.addAll(jasperResources);
        notifyDataSetChanged();
    }

    public void clear() {
        jasperResources = new ArrayList<>();
        notifyDataSetChanged();
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
}
