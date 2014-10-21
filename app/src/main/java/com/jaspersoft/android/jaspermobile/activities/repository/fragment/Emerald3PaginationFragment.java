package com.jaspersoft.android.jaspermobile.activities.repository.fragment;

import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class Emerald3PaginationFragment extends RoboSpiceFragment
        implements PaginationPolicy {


    @InstanceState
    int mNextOffset;
    @InstanceState
    boolean mHasNextPage;
    @InstanceState
    ResourceLookupSearchCriteria mSearchCriteria;

    @Override
    public boolean hasNextPage() {
        return mHasNextPage;
    }

    @Override
    public int calculateNextOffset() {
        return mNextOffset;
    }

    @Override
    public void setSearchCriteria(ResourceLookupSearchCriteria criteria) {
        this.mSearchCriteria = criteria;
    }

    @Override
    public void handleLookup(ResourceLookupsList resourceLookupsList) {
        int offset = resourceLookupsList.getNextOffset();
        mNextOffset = offset;
        mHasNextPage = offset != ResourceLookupsList.NO_OFFSET;
    }
}
