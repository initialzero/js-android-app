package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;

import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * @author Tom Koptel
 * @since 1.9
 */
final class ResourceBinderFactory {
    private ResourceBinderFactory() {
    }

    public static ResourceBinder create(Context context, String type) {
        ResourceLookup.ResourceType resourceType = ResourceLookup.ResourceType.valueOf(type);

        switch (resourceType) {
            case folder:
                return new FolderResourceBinder(context);
            case legacyDashboard:
            case dashboard:
                return new DashboardResourceBinder(context);
            case reportUnit:
                return new ReportResourceBinder(context);
            default:
                throw new UnsupportedOperationException("Unsupported resource type: " + resourceType);
        }
    }
}
