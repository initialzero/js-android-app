package com.jaspersoft.android.jaspermobile.activities.repository.adapter.resource;

import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceView;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface ResourceBinder {
    static final String LOG_TAG = BaseResourceBinder.class.getSimpleName();

    void bindView(ResourceView resourceView, ResourceLookup item);
}
