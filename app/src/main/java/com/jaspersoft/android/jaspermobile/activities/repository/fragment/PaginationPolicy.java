package com.jaspersoft.android.jaspermobile.activities.repository.fragment;

import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public interface PaginationPolicy {
    boolean hasNextPage();
    int calculateNextOffset();
    void handleLookup(ResourceLookupsList resourceLookupsList);
    void setSearchCriteria(ResourceLookupSearchCriteria criteria);
}
