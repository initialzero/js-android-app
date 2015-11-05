/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util.resource.pagination;

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
