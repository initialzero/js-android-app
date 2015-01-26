/*
* Copyright © 2014 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.test.acceptance.library;

import android.support.test.espresso.NoMatchingViewException;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.DrawerActivity;
import com.jaspersoft.android.jaspermobile.activities.DrawerActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.test.utils.TestResponses;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.apache.http.fake.FakeHttpLayerManager;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasTotalCount;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class LibraryPageFilterTest extends ProtoActivityInstrumentation<DrawerActivity_> {

    public LibraryPageFilterTest() {
        super(DrawerActivity_.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setActivityIntent(DrawerActivity_.intent(getApplication())
                .position(DrawerActivity.Position.LIBRARY.ordinal()).get());

        registerTestModule(new HackedTestModule());
        setDefaultCurrentProfile();

        FakeHttpLayerManager.clearHttpResponseRules();
    }

    @After
    public void tearDown() throws Exception {
        unregisterTestModule();
        super.tearDown();
    }

    @Test
    public void testDashboardAndAllFilterOption() throws InterruptedException {
        ResourceLookupsList onlyDashboardLookUp = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.ONLY_DASHBOARD);
        ResourceLookupsList allLookUp = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.SMALL_LOOKUP);

        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.get().noContent());
        startActivityUnderTest();

        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.ONLY_DASHBOARD);
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_dashboards)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(onlyDashboardLookUp.getResourceLookups().size()));

        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.SMALL_LOOKUP);
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_all)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(allLookUp.getResourceLookups().size()));
    }

    @Test
    public void testReportFilterOption() {
        ResourceLookupsList onlyReportLookUp = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.ONLY_REPORT);

        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.get().noContent());
        startActivityUnderTest();

        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.ONLY_REPORT);
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_reports)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(onlyReportLookUp.getResourceLookups().size()));
    }

    @Test
    public void testFilteringIsPersistent() {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.ONLY_REPORT);

        startActivityUnderTest();
        rotateToPortrait();

        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_reports)).perform(click());

        rotate();
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_reports)).check(matches(isChecked()));

        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.ONLY_DASHBOARD);
        onOverflowView(getActivity(), withText(R.string.s_fd_option_dashboards)).perform(click());
        rotate();
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_dashboards)).check(matches(isChecked()));
    }

    // Bug related to the custom single choice implementation.
    // We need long click on the item within big data list.
    // Then switch to the list with few items. As soon as we
    // kept reference to incorrect index position we received crash.
    // Test asserts that adapter clear() method resets old reference
    @Test
    public void testCurrentPositionResetAfterNewFilterSelected() {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.BIG_LOOKUP);
        startActivityUnderTest();

        onData(Matchers.is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(5).perform(longClick());
        pressBack();

        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.ONLY_DASHBOARD);
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_dashboards)).perform(click());
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void clickFilterMenuItem() {
        try {
            onView(withId(R.id.filter)).perform(click());
        } catch (NoMatchingViewException ex) {
            openActionBarOverflowOrOptionsMenu(getApplication());
            onView(withText(R.string.s_ab_filter_by)).perform(click());
        }
    }

}
