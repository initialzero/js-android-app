/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
 * http://community.jaspersoft.com/project/mobile-sdk-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile SDK for Android.
 *
 * Jaspersoft Mobile SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile SDK for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.test.acceptance.home;

import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.SyncSpiceManager;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.util.JsSpiceManager;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetServerInfoRequest;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.apache.http.hacked.HackedJsRestClient;

/**
 * This is not common way of handling injection. Because, of specific implementation we have
 * for profile setup we are forced to be responsible of decoupling state.
 * In usual situation we have JsRestClient properly bound to the JsSpiceManager, but this is not
 * an option when user tries to log in with new profile.
 *
 * <br/>
 * For more details take a look here {@link com.jaspersoft.android.jaspermobile.activities.profile.fragment.ServersFragment#onItemClick}
 *
 * @author Tom Koptel
 * @since 1.9
 */
class SpiceAwareModule extends CommonTestModule {
    @Override
    protected void semanticConfigure() {
        bind(JsRestClient.class).toInstance(HackedJsRestClient.get());
        bind(JsSpiceManager.class).toInstance(new JsSpiceManager() {
            @Override
            public <T> void execute(CachedSpiceRequest<T> cachedSpiceRequest, final RequestListener<T> requestListener) {
                if (cachedSpiceRequest.getSpiceRequest() instanceof GetServerInfoRequest) {
                    ServerInfo serverInfo = TestResources.get().fromXML(ServerInfo.class, TestResources.SERVER_INFO);
                    requestListener.onRequestSuccess((T) serverInfo);
                } else {
                    new SyncSpiceManager().execute(cachedSpiceRequest, requestListener);
                }
            }
        });
    }
}
