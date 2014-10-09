/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
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

import com.jaspersoft.android.jaspermobile.util.JsSpiceManager;
import com.jaspersoft.android.sdk.client.async.request.RunReportExecutionRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetServerInfoRequest;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@Deprecated
public class MockedSpiceManager extends JsSpiceManager {
    private Object responseForCacheRequest;
    private Object responseForNetworkRequest;

    public MockedSpiceManager() {
    }

    public void setResponseForCacheRequest(Object cachedResponse) {
        this.responseForCacheRequest = cachedResponse;
    }

    public void setResponseForNetworkRequest(Object responseForNetworkRequest) {
        this.responseForNetworkRequest = responseForNetworkRequest;
    }

    public <T> void execute(final SpiceRequest<T> request, final Object requestCacheKey,
                            final long cacheExpiryDuration, final RequestListener<T> requestListener) {
        if (request instanceof GetResourceLookupsRequest && responseForCacheRequest instanceof ResourceLookupsList) {
            requestListener.onRequestSuccess((T) responseForCacheRequest);
        }
        if (request instanceof GetServerInfoRequest && responseForCacheRequest instanceof ServerInfo) {
            requestListener.onRequestSuccess((T) responseForCacheRequest);
        }
    }

    public <T> void execute(final SpiceRequest<T> request, final RequestListener<T> requestListener) {
        if (request instanceof GetInputControlsRequest && responseForNetworkRequest instanceof InputControlsList) {
            requestListener.onRequestSuccess((T) responseForNetworkRequest);
        }
        if (request instanceof GetServerInfoRequest && responseForNetworkRequest instanceof ServerInfo) {
            requestListener.onRequestSuccess((T) responseForNetworkRequest);
        }
        if (request instanceof RunReportExecutionRequest && responseForNetworkRequest instanceof ReportExecutionResponse) {
            requestListener.onRequestSuccess((T) responseForNetworkRequest);
        }
    }
}