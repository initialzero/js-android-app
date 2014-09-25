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
import android.util.Log;

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

    private final ArrayDeque<Object> responsesForCacheRequestQueue = Queues.newArrayDeque();
    private final ArrayDeque<Object> responsesForNetworkRequestQueue = Queues.newArrayDeque();
    private final CustomSpiceServerListener customSpiceServerListener;
    private final LifeCycleListener lifeCycleListener;
    private final boolean mOnlyMockBehavior;
    private boolean mBehaveInRealMode;

    public static SmartMockedSpiceManager createMockedManager(Class<? extends SpiceService> spiceServiceClass) {
        return new SmartMockedSpiceManager(spiceServiceClass, false);
    }

    public static SmartMockedSpiceManager createHybridManager(Class<? extends SpiceService> spiceServiceClass) {
        return new SmartMockedSpiceManager(spiceServiceClass, true);
    }

    private SmartMockedSpiceManager(Class<? extends SpiceService> spiceServiceClass, boolean onlyMockBehavior) {
        super(spiceServiceClass);
        mOnlyMockBehavior = onlyMockBehavior;
        if (onlyMockBehavior) {
            lifeCycleListener = new LifeCycleListener();
            customSpiceServerListener = new CustomSpiceServerListener();
            ActivityLifecycleMonitorRegistry.getInstance()
                    .addLifecycleCallback(lifeCycleListener);
        } else {
            customSpiceServerListener = null;
            lifeCycleListener = null;
        }
    }

    public void removeLifeCycleListener() {
        removeSpiceServiceListener(customSpiceServerListener);
        ActivityLifecycleMonitorRegistry.getInstance()
                .removeLifecycleCallback(lifeCycleListener);
    }

    public void addCachedResponse(Object cachedResponse) {
        responsesForCacheRequestQueue.add(cachedResponse);
    }

    public void addNetworkResponse(Object responseForNetworkRequest) {
        responsesForNetworkRequestQueue.add(responseForNetworkRequest);
    }

    public void clearNetworkResponses() {
        responsesForNetworkRequestQueue.clear();
    }

    public void clearCachedResponses() {
        responsesForCacheRequestQueue.clear();
    }

    @Override
    public <T> void execute(final SpiceRequest<T> request, final Object requestCacheKey,
                            final long cacheExpiryDuration, final RequestListener<T> requestListener) {
        if (mBehaveInRealMode) {

            addSpiceServiceListener(customSpiceServerListener);
            super.execute(request, requestCacheKey, cacheExpiryDuration, requestListener);
        } else {
            requestListener.onRequestSuccess((T) responsesForCacheRequestQueue.pollFirst());
        }
    }

    @Override
    public <T> void execute(final SpiceRequest<T> request, final RequestListener<T> requestListener) {
        if (mBehaveInRealMode) {
            addSpiceServiceListener(customSpiceServerListener);
            super.execute(request, requestListener);
        } else {
            requestListener.onRequestSuccess((T) responsesForNetworkRequestQueue.pollFirst());
        }
    }

    public void behaveInRealMode() {
        setBehaveInRealMode(true);
    }

    public void behaveInMockedMode() {
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
        private static final String TAG = "CountingIdlingResource";

        private final CountingIdlingResource idlingResource;
        private final boolean mDebug;

        private CustomSpiceServerListener() {
            this(false);
        }

        private CustomSpiceServerListener(boolean debug) {
            mDebug = debug;
            idlingResource = new CountingIdlingResource(
                    String.format("CustomSpiceServerListener #{%d} idle resource", this.hashCode()), true);
            Espresso.registerIdlingResources(idlingResource);
        }

        @Override
        public void onRequestFailed(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            idlingResource.decrement();
        }

        @Override
        public void onRequestCancelled(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            idlingResource.decrement();
        }

        @Override
        public void onRequestProgressUpdated(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
        }

        @Override
        public void onRequestSucceeded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            dumpLog("onRequestSucceeded", request.getResultType().getSimpleName(), request.hashCode());
            if (!idlingResource.isIdleNow()) idlingResource.decrement();
        }

        @Override
        public void onRequestAdded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            dumpLog("onRequestAdded", request.getResultType().getSimpleName(), request.hashCode());
            idlingResource.increment();
        }

        @Override
        public void onRequestAggregated(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
        }

        @Override
        public void onRequestNotFound(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            idlingResource.decrement();
        }

        @Override
        public void onRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {
        }

        @Override
        public void onServiceStopped() {
        }

        private void dumpLog(String tag, String what, long whatHashCode) {
            dumpLog(tag, what, whatHashCode, "");
        }

        private void dumpLog(String tag, String what, long whatHashCode, String extraMsg) {
            if (mDebug) {
                Log.i(TAG, String.format("CustomSpiceServerListener %s for: %s %d %s", tag, what, whatHashCode, extraMsg));
            }
        }
    }
}
