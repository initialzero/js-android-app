package com.jaspersoft.android.jaspermobile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.BaseViewHolder;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.DashboardViewBinder;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.FolderViewBinder;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceType;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.ReportViewBinder;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.ResourceViewBinder;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.UndefinedViewBinder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class JasperResourceAdapter extends RecyclerView.Adapter<BaseViewHolder> {

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
        return resourceViewBinder.buildViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder baseViewHolder, int position) {
        baseViewHolder.populateView(jasperResources.get(position));
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
            default:
                return new UndefinedViewBinder(context, viewType);
        }
    }
}
