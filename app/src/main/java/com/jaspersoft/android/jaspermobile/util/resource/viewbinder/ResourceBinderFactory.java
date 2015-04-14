package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.text.TextUtils;

import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceAdapter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * @author Tom Koptel
 * @since 1.9
 */
final class ResourceBinderFactory {
    private ResourceBinderFactory() {
    }

    public static ResourceBinderFactory newInstance() {
        return new ResourceBinderFactory();
    }

    public ResourceBinder create(Context context, ResourceAdapter.KpiResourceLookup item) {
        String type = item.getResource().getResourceType().toString();

        ResourceLookup.ResourceType resourceType = ResourceLookup.ResourceType.valueOf(type);

        switch (resourceType) {
            case folder:
                return new FolderResourceBinder(context);
            case legacyDashboard:
            case dashboard:
                return new DashboardResourceBinder(context);
            case reportUnit:
                if (isKpi(item)) {
                    return new KpiResourceBinder(context);
                } else {
                    return new ReportResourceBinder(context);
                }
            default:
                throw new UnsupportedOperationException("Unsupported resource type: " + resourceType);
        }
    }

    protected boolean isKpi(ResourceAdapter.KpiResourceLookup item) {
        if (item == null) {
            return false;
        } else {
            return !TextUtils.isEmpty(item.getKpiUri());
        }
    }
}
