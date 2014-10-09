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
import com.google.android.apps.common.testing.ui.espresso.IdlingPolicies;
import com.google.android.apps.common.testing.ui.espresso.contrib.CountingIdlingResource;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.jaspersoft.android.jaspermobile.util.JsSpiceManager;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class SmartMockedSpiceManager extends JsSpiceManager {

    private final ArrayDeque<Object> responsesForCacheRequestQueue = Queues.newArrayDeque();
    private final ArrayDeque<Object> responsesForNetworkRequestQueue = Queues.newArrayDeque();
    private final SmartSpiceServiceListener customSpiceServerListener;
    private final LifeCycleListener lifeCycleListener;
    private boolean mOnlyMockBehavior;
    private boolean mDebugable;
    private boolean spiceListenerAdded;

    public static Builder builder() {
        return new Builder();
    }

    public static SmartMockedSpiceManager getInstance() {
        return new Builder().build();
    }

    private SmartMockedSpiceManager(boolean onlyMockBehavior, boolean debugable, Class<?>... responseChain) {
        mOnlyMockBehavior = onlyMockBehavior;
        mDebugable = debugable;
        if (onlyMockBehavior) {
            customSpiceServerListener = null;
            lifeCycleListener = null;
        } else {
            if (responseChain.length > 0) {
                customSpiceServerListener = new ChainSpiceServerListener(responseChain);
            } else {
                customSpiceServerListener = new LinearSpiceServerListener();
            }
            lifeCycleListener = new LifeCycleListener();
            ActivityLifecycleMonitorRegistry.getInstance()
                    .addLifecycleCallback(lifeCycleListener);
        }
    }

    public void removeLifeCycleListener() {
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
        if (!spiceListenerAdded) {
            spiceListenerAdded = true;
            addSpiceServiceListener(customSpiceServerListener);
        }
        if (mOnlyMockBehavior) {
            Object response = responsesForCacheRequestQueue.pollFirst();
            if (response instanceof RequestExecutionAssertion) {
                requestListener.onRequestSuccess((T) ((RequestExecutionAssertion) response).getResponse());
            } else {
                requestListener.onRequestSuccess((T) response);
            }
        } else {
            super.execute(request, requestCacheKey, cacheExpiryDuration, requestListener);
        }
    }

    @Override
    public <T> void execute(final SpiceRequest<T> request, final RequestListener<T> requestListener) {
        if (!spiceListenerAdded) {
            spiceListenerAdded = true;
            addSpiceServiceListener(customSpiceServerListener);
        }
        if (mOnlyMockBehavior) {
            Object response = responsesForNetworkRequestQueue.pollFirst();
            if (response instanceof RequestExecutionAssertion) {
                requestListener.onRequestSuccess((T) ((RequestExecutionAssertion) response).getResponse());
            } else {
                requestListener.onRequestSuccess((T) response);
            }
        } else {
            super.execute(request, requestListener);
        }
    }

    @Override
    public synchronized void shouldStop() {
        removeSpiceServiceListener(customSpiceServerListener);
        spiceListenerAdded = false;
        super.shouldStop();
    }


    public void behaveInRealMode() {
        setBehaveInMockedState(false);
    }

    public void behaveInMockedMode() {
        setBehaveInMockedState(true);
    }

    public void setBehaveInMockedState(boolean value) {
        mOnlyMockBehavior = value;
    }

    private class LifeCycleListener implements ActivityLifecycleCallback {
        @Override
        public void onActivityLifecycleChanged(Activity activity, Stage stage) {
            Log.d("CountingIdlingResource", activity.getClass().getSimpleName() + " Stage stage " + stage + " isStarted()" + isStarted());
        }
    }

    private class ChainSpiceServerListener extends SmartSpiceServiceListener {
        private final HashMap<Class<?>, CountingIdlingResource> idlingMap = Maps.newHashMap();
        private final ArrayList<Class<?>> requestChain;
        private final CountingIdlingResource chainIdlingResource;

        private ChainSpiceServerListener(Class<?>... responseChain) {
            super(mDebugable);
            Preconditions.checkState((responseChain.length != 0));

            requestChain = Lists.newArrayList(responseChain);
            chainIdlingResource = new CountingIdlingResource(String.format(" for response chain %s",
                    ArrayUtils.toString(requestChain)), mDebugable);
            Espresso.registerIdlingResources(chainIdlingResource);
            for (int i = 0; i < responseChain.length; i++) {
                chainIdlingResource.increment();
            }
        }

        @Override
        protected void incrementIdleResource(CachedSpiceRequest<?> request) {
            if (!idlingMap.keySet().contains(request.getResultType())) {
                Preconditions.checkState(requestChain.contains(request.getResultType()));
                idlingMap.put(request.getResultType(), chainIdlingResource);
            }

            if (chainIdlingResource.isIdleNow()) chainIdlingResource.increment();
        }

        @Override
        protected void decrementIdleResource(CachedSpiceRequest<?> request) {
            if (idlingMap.keySet().contains(request.getResultType())) {
                if (requestChain.contains(request.getResultType())) {
                    if (!chainIdlingResource.isIdleNow()) {
                        requestChain.remove(request.getResultType());
                        chainIdlingResource.decrement();
                    }
                }
            } else {
                logd(TAG, String.format("Could not decrement Idle resource for this request %s with #{%d}",
                        request.getResultType().getSimpleName(), request.hashCode()));
            }
        }
    }

    private class LinearSpiceServerListener extends SmartSpiceServiceListener {
        private final TreeMap<CachedSpiceRequest<?>, CountingIdlingResource> idlingMap = Maps.newTreeMap();

        private LinearSpiceServerListener() {
            super(mDebugable);
        }

        @Override
        protected void incrementIdleResource(CachedSpiceRequest<?> request) {
            CountingIdlingResource idlingResource;
            if (idlingMap.keySet().contains(request)) {
                idlingResource = idlingMap.get(request);
            } else {
                idlingResource = new CountingIdlingResource(
                        String.format(" for request %s with #{%d}",
                                request.getResultType().getSimpleName(), request.hashCode()), mDebugable);
                Espresso.registerIdlingResources(idlingResource);
                idlingMap.put(request, idlingResource);
            }

            if (idlingResource.isIdleNow()) idlingResource.increment();
        }

        @Override
        protected void decrementIdleResource(CachedSpiceRequest<?> request) {
            CountingIdlingResource idlingResource;
            if (idlingMap.keySet().contains(request)) {
                idlingResource = idlingMap.get(request);
                if (!idlingResource.isIdleNow()) idlingResource.decrement();
            } else {
                logd(TAG, String.format("Could not decrement Idle resource for this request %s with #{%d}",
                        request.getResultType().getSimpleName(), request.hashCode()));
            }
        }
    }

    public static class Builder {
        private boolean mocked;
        private boolean debugable;
        private Class<?>[] responseChain;

        public Builder() {
            this.mocked = true;
            this.responseChain = new Class<?>[0];
        }

        public Builder setIdlingResourceTimeout(long timeout, TimeUnit timeUnit) {
            IdlingPolicies.setIdlingResourceTimeout(timeout, timeUnit);
            return this;
        }

        public Builder setMocked(boolean mocked) {
            this.mocked = mocked;
            return this;
        }

        public Builder setResponseChain(Class<?>... responseChain) {
            this.responseChain = responseChain;
            this.mocked = false;
            return this;
        }

        public Builder setDebugable(boolean debugable) {
            this.debugable = debugable;
            return this;
        }

        public SmartMockedSpiceManager build() {
            return new SmartMockedSpiceManager(mocked, debugable, responseChain);
        }
    }

}
