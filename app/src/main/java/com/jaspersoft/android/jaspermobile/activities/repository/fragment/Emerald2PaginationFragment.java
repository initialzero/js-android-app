package com.jaspersoft.android.jaspermobile.activities.repository.fragment;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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
public class Emerald2PaginationFragment extends RoboSpiceFragment
        implements PaginationPolicy {

    @InstanceState
    ResourceLookupSearchCriteria mSearchCriteria;

    @Inject
    @Named("LIMIT")
    int mLimit;
    @InstanceState
    int mTotal;

    @Override
    public boolean hasNextPage() {
        return mSearchCriteria.getOffset() + mLimit < mTotal;
    }

    @Override
    public int calculateNextOffset() {
        return mSearchCriteria.getOffset() + mLimit;
    }

    @Override
    public void setSearchCriteria(ResourceLookupSearchCriteria criteria) {
        this.mSearchCriteria = criteria;
    }

    @Override
    public void handleLookup(ResourceLookupsList resourceLookupsList) {
        boolean isFirstPage = mSearchCriteria.getOffset() == 0;
        if (isFirstPage) {
            mTotal = resourceLookupsList.getTotalCount();
        }
    }
}
