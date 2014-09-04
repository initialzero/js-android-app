/*
* Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
* http://community.jaspersoft.com/project/jaspermobile-android
*
* Unless you have purchased a commercial license agreement from Jaspersoft,
* the following license terms apply:
*
* This program is part of Jaspersoft Mobile for Android.
*
* Jaspersoft Mobile is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Jaspersoft Mobile is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with Jaspersoft Mobile for Android. If not, see
* <http://www.gnu.org/licenses/lgpl>.
*/

package com.jaspersoft.android.jaspermobile.test.acceptance;

import android.app.Application;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.jaspersoft.android.jaspermobile.activities.repository.SearchActivity;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.R;
import com.jaspersoft.android.jaspermobile.test.TestResources;
import com.jaspersoft.android.jaspermobile.util.JsXmlSpiceServiceWrapper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetServerInfoRequest;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import roboguice.RoboGuice;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class LibraryPageTest extends ProtoActivityInstrumentation<SearchActivity> {
    @Mock
    JsRestClient mockRestClient;
    @Mock
    JsServerProfile mockServerProfile;
    @Mock
    JsXmlSpiceServiceWrapper mockJsXmlSpiceServiceWrapper;
    @Mock
    SpiceManager mockSpiceService;
    @Mock
    DatabaseProvider mockDatabaseProvider;
    @Mock
    ServerInfo mockServerInfo;

    final MockedSpiceManager mMockedSpiceManager = new MockedSpiceManager(JsXmlSpiceService.class);

    public LibraryPageTest() {
        super(SearchActivity.class);
    }

    @Override
    public String getPageName() {
        return "search";
    }

    @Override
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mockJsXmlSpiceServiceWrapper.getSpiceManager()).thenReturn(mMockedSpiceManager);
        when(mockRestClient.getServerProfile()).thenReturn(mockServerProfile);

        Application application = (Application) this.getInstrumentation()
                .getTargetContext().getApplicationContext();
        RoboGuice.setBaseApplicationInjector(application,
                RoboGuice.DEFAULT_STAGE,
                Modules.override(RoboGuice.newDefaultRoboModule(application))
                        .with(new TestModule()));

        super.setUp();
    }

    public void testInitialLoad() {
        makeTwoFirstListItemsAccessible();
        onView(withId(R.id.second_list_item)).perform(click());
    }

    public static class MockedSpiceManager extends SpiceManager {
        public MockedSpiceManager(Class<? extends SpiceService> spiceServiceClass) {
            super(spiceServiceClass);
        }

        public <T> void execute(final SpiceRequest<T> request, final Object requestCacheKey,
                                final long cacheExpiryDuration, final RequestListener<T> requestListener) {
            if (request instanceof GetServerInfoRequest) {
                requestListener.onRequestSuccess((T) TestResources.get().fromXML(ServerInfo.class, "server_info"));
            }
            if (request instanceof GetResourceLookupsRequest) {
                requestListener.onRequestSuccess((T) TestResources.get().fromXML(ResourceLookupsList.class, "library_reports"));
            }
            if (request instanceof GetInputControlsRequest) {
                requestListener.onRequestSuccess((T) new InputControlsList());
            }
        }
    }

    public class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bindConstant().annotatedWith(Names.named("animationSpeed")).to(0);
            bind(JsRestClient.class).toInstance(mockRestClient);
            bind(JsXmlSpiceServiceWrapper.class).toInstance(mockJsXmlSpiceServiceWrapper);
            bind(DatabaseProvider.class).toInstance(mockDatabaseProvider);

        }
    }

}
