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

package com.jaspersoft.android.jaspermobile.test.acceptance.viewer;

import android.content.Intent;

import com.google.inject.Singleton;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.DashboardHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.IdleInjector;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.firstChildOf;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class DashboardViewPageTest extends ProtoActivityInstrumentation<DashboardHtmlViewerActivity_> {
    private static final String RESOURCE_URI = "/Dashboards/Supermart_Dashboard";
    private static final String RESOURCE_LABEL = "1. Supermart Dashboard";
    private ResourceLookup mResource;
    private IdleInjector idleInjector;

    public DashboardViewPageTest() {
        super(DashboardHtmlViewerActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registerTestModule(new TestModule());
        setDefaultCurrentProfile();
        idleInjector = WebViewInjector.registerFor(DashboardHtmlViewerActivity_.class);

        ResourceLookupsList resourceLookupsList = TestResources.get().fromXML(ResourceLookupsList.class, "only_dashboard");
        mResource = resourceLookupsList.getResourceLookups().get(0);
        mResource.setLabel(RESOURCE_LABEL);
        mResource.setUri(RESOURCE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        idleInjector.unregister();
        super.tearDown();
    }

    public void testInitialLoad() {
        createReportIntent();
        startActivityUnderTest();
        onView(withText(RESOURCE_LABEL)).check(matches(isDisplayed()));
        rotate();
        onView(firstChildOf(withId(R.id.webViewPlaceholder))).check(matches(isDisplayed()));
    }

    private void createReportIntent() {
        Intent htmlViewer = new Intent();
        htmlViewer.putExtra(DashboardHtmlViewerActivity_.RESOURCE_EXTRA, mResource);
        setActivityIntent(htmlViewer);
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).in(Singleton.class);
        }
    }

}
