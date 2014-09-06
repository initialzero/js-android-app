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

package com.jaspersoft.android.jaspermobile.test.acceptance.viewer;

import android.app.Application;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.BaseHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.ReportHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.util.JsXmlSpiceServiceWrapper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.async.request.RunReportExecutionRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetServerInfoRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;

import roboguice.RoboGuice;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.doubleClick;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.firstChildOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ReportViewPageTest extends ActivityInstrumentationTestCase2<ReportHtmlViewerActivity> {
    private static final String REPORT_URI = "http://mobiledemo.jaspersoft.com/";

    private static final String USERNAME = "phoneuser|organization_1";
    private static final String PASSWORD = "phoneuser";
    private static final String RESOURCE_LABEL = "02. Sales Mix by Demographic Report";

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

    private ReportWebViewInjector injector;
    final MockedSpiceManager mMockedSpiceManager = new MockedSpiceManager(JsXmlSpiceService.class);

    public ReportViewPageTest() {
        super(ReportHtmlViewerActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mockJsXmlSpiceServiceWrapper.getSpiceManager()).thenReturn(mMockedSpiceManager);
        when(mockRestClient.getServerProfile()).thenReturn(mockServerProfile);
        when(mockRestClient.getExportOuptutResourceURI(anyString(), anyString())).thenReturn(URI.create(REPORT_URI));
        when(mockServerProfile.getUsernameWithOrgId()).thenReturn(USERNAME);
        when(mockServerProfile.getPassword()).thenReturn(PASSWORD);

        Application application = (Application) this.getInstrumentation()
                .getTargetContext().getApplicationContext();
        RoboGuice.setBaseApplicationInjector(application,
                RoboGuice.DEFAULT_STAGE,
                Modules.override(RoboGuice.newDefaultRoboModule(application))
                        .with(new TestModule()));

        super.setUp();

        Intent htmlViewer = new Intent();
        htmlViewer.putExtra(BaseHtmlViewerActivity.EXTRA_RESOURCE_URI, "/Reports/2_Sales_Mix_by_Demographic_Report");
        htmlViewer.putExtra(BaseHtmlViewerActivity.EXTRA_RESOURCE_LABEL, RESOURCE_LABEL);
        setActivityIntent(htmlViewer);

        WebViewIdlingResource webViewIdlingResource = new WebViewIdlingResource();
        Espresso.registerIdlingResources(webViewIdlingResource);
        injector = new ReportWebViewInjector(webViewIdlingResource);
        ActivityLifecycleMonitorRegistry.getInstance()
                .addLifecycleCallback(injector);

        getActivity();
    }

    @Override
    protected void tearDown() throws Exception {
        ActivityLifecycleMonitorRegistry.getInstance()
                .removeLifecycleCallback(injector);
        super.tearDown();
    }

    public void testInitialLoad() throws InterruptedException {
        onView(withText(RESOURCE_LABEL)).check(matches(isDisplayed()));;

        for (int i = 0; i < 10; i++) {
            onView(firstChildOf(withId(R.id.webViewPlaceholder))).perform(doubleClick());
        }
    }

    public static class MockedSpiceManager extends SpiceManager {
        public MockedSpiceManager(Class<? extends SpiceService> spiceServiceClass) {
            super(spiceServiceClass);
        }

        public <T> void execute(final SpiceRequest<T> request, final RequestListener<T> requestListener) {
            if (request instanceof GetServerInfoRequest) {
                requestListener.onRequestSuccess((T) TestResources.get().fromXML(ServerInfo.class, "server_info"));
            }
            if (request instanceof RunReportExecutionRequest) {
                requestListener.onRequestSuccess((T) TestResources.get().fromXML(ReportExecutionResponse.class, "report_execution"));
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
