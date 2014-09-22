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

import android.app.Activity;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleCallback;
import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.testrunner.Stage;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.google.android.apps.common.testing.ui.espresso.contrib.CountingIdlingResource;
import com.google.common.collect.Queues;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.SpiceServiceListener;

import java.util.ArrayDeque;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class SmartMockedSpiceManager extends SpiceManager {

    private final ArrayDeque<Object> responseForCacheRequestMap = Queues.newArrayDeque();
    private final ArrayDeque<Object> responseForNetworkRequestMap = Queues.newArrayDeque();
    private final CustomSpiceServerListener customSpiceServerListener;
    private final LifeCycleListener lifeCycleListener;
    private boolean mBehaveInRealMode;

    public SmartMockedSpiceManager(Class<? extends SpiceService> spiceServiceClass) {
        super(spiceServiceClass);
        lifeCycleListener = new LifeCycleListener();
        customSpiceServerListener = new CustomSpiceServerListener();
        ActivityLifecycleMonitorRegistry.getInstance()
                .addLifecycleCallback(lifeCycleListener);
    }

    public void removeLifeCyclkeListener() {
        ActivityLifecycleMonitorRegistry.getInstance()
                .removeLifecycleCallback(lifeCycleListener);
    }

    public void addCachedResponse(Object cachedResponse) {
        responseForCacheRequestMap.add(cachedResponse);
    }

    public void addNetworkResponse(Object responseForNetworkRequest) {
        responseForNetworkRequestMap.add(responseForNetworkRequest);
    }

    @Override
    public <T> void execute(final SpiceRequest<T> request, final Object requestCacheKey,
                            final long cacheExpiryDuration, final RequestListener<T> requestListener) {
        if (mBehaveInRealMode) {
            addSpiceServiceListener(customSpiceServerListener);
            super.execute(request, requestCacheKey, cacheExpiryDuration, requestListener);
        } else {
            requestListener.onRequestSuccess((T) responseForCacheRequestMap.pollFirst());
        }
    }

    @Override
    public <T> void execute(final SpiceRequest<T> request, final RequestListener<T> requestListener) {
        if (mBehaveInRealMode) {
            addSpiceServiceListener(customSpiceServerListener);
            super.execute(request, requestListener);
        } else {
            requestListener.onRequestSuccess((T) responseForNetworkRequestMap.pollFirst());
        }
    }

    public void behaveInRealMode() {
        if (mBehaveInRealMode) {
            throw new RuntimeException("You are already in 'REAL MODE'");
        }
        setBehaveInRealMode(true);
    }

    public void behaveInMockedMode() {
        if (!mBehaveInRealMode) {
            throw new RuntimeException("You are already in 'MOCKED MODE'");
        }
        setBehaveInRealMode(false);
    }

    public void setBehaveInRealMode(boolean value) {
        mBehaveInRealMode = value;
    }

    private class LifeCycleListener implements ActivityLifecycleCallback {
        @Override
        public void onActivityLifecycleChanged(Activity activity, Stage stage) {
            switch (stage) {
                case RESUMED:
                    if (!isStarted()) {
                        start(activity);
                    }
                    break;
                case PAUSED:
                    if (isStarted()) {
                        shouldStop();
                    }
                    break;
                default: // NOP
            }
        }
    }

    private class CustomSpiceServerListener implements SpiceServiceListener {
        private final CountingIdlingResource idlingResource;

        private CustomSpiceServerListener() {
            idlingResource = new CountingIdlingResource("Spice server idle resource");
            Espresso.registerIdlingResources(idlingResource);
        }

        @Override
        public void onRequestSucceeded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            idlingResource.decrement();
            removeSpiceServiceListener(this);
        }

        @Override
        public void onRequestFailed(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            idlingResource.decrement();
            removeSpiceServiceListener(this);
        }

        @Override
        public void onRequestCancelled(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            idlingResource.decrement();
            removeSpiceServiceListener(this);
        }

        @Override
        public void onRequestProgressUpdated(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
        }

        @Override
        public void onRequestAdded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            idlingResource.increment();
        }

        @Override
        public void onRequestAggregated(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
        }

        @Override
        public void onRequestNotFound(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            idlingResource.decrement();
            removeSpiceServiceListener(this);
        }

        @Override
        public void onRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {
        }

        @Override
        public void onServiceStopped() {
        }
    }
}
