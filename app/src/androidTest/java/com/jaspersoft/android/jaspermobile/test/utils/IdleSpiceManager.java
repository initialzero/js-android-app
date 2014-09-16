/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.test.utils;

import android.os.Handler;

import com.google.android.apps.common.testing.ui.espresso.contrib.CountingIdlingResource;
import com.google.common.collect.Queues;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayDeque;


/**
 * @author Tom Koptel
 * @since 1.9
 */
public class IdleSpiceManager extends SpiceManager {
    public static final int IDLE_STATE = 0x1;
    public static final int BUSY_STATE = 0x2;
    public static final int SMALL_LOOKUP = 0x4;
    public static final int BIG_LOOKUP = 0x8;
    public static final int EMPTY_LOOKUP = 0x10;
    public static final int SLEEP_RATE = 1000;

    private final ResourceLookupsList smallLookUp;
    private final ResourceLookupsList bigLookUp;
    private final ResourceLookupsList emptyLookUp;
    private final CountingIdlingResource mCountingIdlingResource;

    private Handler mHandler;
    private int mState;
    private int sleepRate;

    public IdleSpiceManager(CountingIdlingResource countingIdlingResource,
                            Handler handler,
                            Class<? extends SpiceService> spiceServiceClass) {
        super(spiceServiceClass);
        mCountingIdlingResource = countingIdlingResource;
        mHandler = handler;
        smallLookUp = TestResources.get().fromXML(ResourceLookupsList.class, "library_reports_small");
        bigLookUp = TestResources.get().fromXML(ResourceLookupsList.class, "library_0_40");
        emptyLookUp = new ResourceLookupsList();

        mState = IDLE_STATE | SMALL_LOOKUP;
        sleepRate = SLEEP_RATE;
    }

    public void setSleepRate(int sleepRate) {
        this.sleepRate = sleepRate;
    }

    public void setState(int mState) {
        this.mState = mState;
    }

    public <T> void execute(final SpiceRequest<T> request, final Object requestCacheKey,
                            final long cacheExpiryDuration, final RequestListener<T> requestListener) {
        if (request instanceof GetResourceLookupsRequest) {
            if ( (mState & BUSY_STATE) == BUSY_STATE) {
                mCountingIdlingResource.increment();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            requestListener.onRequestSuccess((T) getResourceLookup());
                        } finally {
                            mCountingIdlingResource.decrement();
                        }
                    }
                }, sleepRate);
            } else {
                requestListener.onRequestSuccess((T) getResourceLookup());
            }
        }
    }

    public ResourceLookupsList getResourceLookup() {
        if ( (mState & SMALL_LOOKUP ) == SMALL_LOOKUP ) {
            return smallLookUp;
        }
        if ( (mState & BIG_LOOKUP ) == BIG_LOOKUP ) {
            return bigLookUp;
        }
        return emptyLookUp;
    }

    public int lookupSize() {
        return getResourceLookup().getResourceLookups().size();
    }

    public ArrayDeque<ResourceLookup> getResources() {
        return Queues.newArrayDeque(getResourceLookup().getResourceLookups());
    }
}