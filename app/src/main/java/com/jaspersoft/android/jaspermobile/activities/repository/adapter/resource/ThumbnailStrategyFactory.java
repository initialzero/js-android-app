package com.jaspersoft.android.jaspermobile.activities.repository.adapter.resource;

import android.content.Context;

import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * @author Tom Koptel
 * @since 2.0
 */
final class ThumbnailStrategyFactory {

    private ThumbnailStrategyFactory() {
    }

    public static ThumbnailStrategy create(Context context, String type) {
        ResourceLookup.ResourceType resourceType = ResourceLookup.ResourceType.valueOf(type);
        ResourceAsset resourceAsset = ResourceAssetFactory.create(type);

        switch (resourceType) {
            case folder:
            case legacyDashboard:
            case dashboard:
                return new BaseThumbnailStrategy(resourceAsset);
            case reportUnit:
                return new ReportThumbnailStrategy(context, resourceAsset);
            default:
                throw new UnsupportedOperationException("Unsupported resource type: " + resourceType);
        }
    }
}
