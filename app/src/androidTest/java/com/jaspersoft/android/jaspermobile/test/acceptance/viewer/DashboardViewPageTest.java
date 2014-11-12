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

package com.jaspersoft.android.jaspermobile.test.acceptance.viewer;

import android.content.Intent;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.DashboardHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.IdleInjector;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.firstChildOf;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;

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
        registerTestModule(new HackedTestModule());
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

    public void testAboutAction() {
        createReportIntent();
        startActivityUnderTest();

        onView(withId(R.id.aboutAction)).perform(click());

        onOverflowView(getActivity(), withId(R.id.sdl__title)).check(matches(withText(mResource.getLabel())));
        onOverflowView(getActivity(), withId(R.id.sdl__message)).check(matches(withText(mResource.getDescription())));
    }

    private void createReportIntent() {
        Intent htmlViewer = new Intent();
        htmlViewer.putExtra(DashboardHtmlViewerActivity_.RESOURCE_EXTRA, mResource);
        setActivityIntent(htmlViewer);
    }

}
