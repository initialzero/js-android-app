package com.jaspersoft.android.jaspermobile.activities.repository.adapter.resource;

import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * @author Tom Koptel
 * @since 2.0
 */
final class ResourceAssetFactory {
    private ResourceAssetFactory() {
    }

    public static ResourceAsset create(String type) {
        ResourceLookup.ResourceType resourceType = ResourceLookup.ResourceType.valueOf(type);

        switch (resourceType) {
            case folder:
                return new FolderResourceAsset();
            case legacyDashboard:
            case dashboard:
                return new DashboardResourceAsset();
            case reportUnit:
                return new ReportResourceAsset();
            default:
                throw new UnsupportedOperationException("Unsupported resource type: " + resourceType);
        }
    }
}
