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
import android.content.ContentResolver;
import android.content.Intent;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.google.android.apps.common.testing.ui.espresso.IdlingPolicies;
import com.google.inject.Singleton;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.ReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils;
import com.jaspersoft.android.jaspermobile.util.JsXmlSpiceServiceWrapper;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper_;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsRequest;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.doubleClick;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.firstChildOf;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ReportViewPageTest extends ProtoActivityInstrumentation<ReportHtmlViewerActivity_> {
    private static final String RESOURCE_URI = "/Reports/2_Sales_Mix_by_Demographic_Report";
    private static final String RESOURCE_LABEL = "02. Sales Mix by Demographic Report";

    @Mock
    JsXmlSpiceServiceWrapper mockJsXmlSpiceServiceWrapper;
    @Mock
    SpiceManager mockSpiceService;

    private ProfileHelper_ profileHelper;
    private ReportWebViewInjector injector;
    final MockedSpiceManager mMockedSpiceManager = new MockedSpiceManager(JsXmlSpiceService.class);

    public ReportViewPageTest() {
        super(ReportHtmlViewerActivity_.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        when(mockJsXmlSpiceServiceWrapper.getSpiceManager())
                .thenReturn(mMockedSpiceManager);
        registerTestModule(new TestModule());
        setDefaultCurrentProfile();
        registerIdleResources();
    }

    private void setDefaultCurrentProfile() {
        Application application = (Application) this.getInstrumentation()
                .getTargetContext().getApplicationContext();

        ContentResolver cr = application.getContentResolver();
        ServerProfiles profile = TestServerProfileUtils.queryProfileByAlias(
                cr, ProfileHelper.DEFAULT_ALIAS);
        if (profile == null) {
            TestServerProfileUtils.createDefaultProfile(cr);
            profile = TestServerProfileUtils.queryProfileByAlias(
                    cr, ProfileHelper.DEFAULT_ALIAS);
        }
        profileHelper = ProfileHelper_.getInstance_(application);
        profileHelper.setCurrentServerProfile(profile.getRowId());
    }

    private void registerIdleResources() {
        IdlingPolicies.setIdlingResourceTimeout(150, TimeUnit.SECONDS);
        WebViewIdlingResource webViewIdlingResource = new WebViewIdlingResource();
        Espresso.registerIdlingResources(webViewIdlingResource);
        injector = new ReportWebViewInjector(webViewIdlingResource);
        ActivityLifecycleMonitorRegistry.getInstance()
                .addLifecycleCallback(injector);
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        ActivityLifecycleMonitorRegistry.getInstance()
                .removeLifecycleCallback(injector);
        super.tearDown();
    }

    public void testInitialLoad() {
        createReportIntent();
        startActivityUnderTest();

        onView(withText(RESOURCE_LABEL)).check(matches(isDisplayed()));

        for (int i = 0; i < 10; i++) {
            onView(firstChildOf(withId(R.id.webViewPlaceholder))).perform(doubleClick());
        }
    }

    private void createReportIntent() {
        Intent htmlViewer = new Intent();
        htmlViewer.putExtra(ReportHtmlViewerActivity_.RESOURCE_URI_EXTRA, RESOURCE_URI);
        htmlViewer.putExtra(ReportHtmlViewerActivity_.RESOURCE_LABEL_EXTRA, RESOURCE_LABEL);
        setActivityIntent(htmlViewer);
    }

    private static class MockedSpiceManager extends SpiceManager {
        public MockedSpiceManager(Class<? extends SpiceService> spiceServiceClass) {
            super(spiceServiceClass);
        }

        public <T> void execute(final SpiceRequest<T> request, final RequestListener<T> requestListener) {
            if (request instanceof GetInputControlsRequest) {
                requestListener.onRequestSuccess((T) new InputControlsList());
            }
        }
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).in(Singleton.class);
            bind(JsXmlSpiceServiceWrapper.class).toInstance(mockJsXmlSpiceServiceWrapper);
        }
    }

}
