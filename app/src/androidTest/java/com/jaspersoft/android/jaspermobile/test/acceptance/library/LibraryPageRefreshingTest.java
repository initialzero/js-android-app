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

package com.jaspersoft.android.jaspermobile.test.acceptance.library;

import com.google.common.collect.Queues;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.DrawerActivity;
import com.jaspersoft.android.jaspermobile.activities.DrawerActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.test.utils.TestResponses;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.apache.http.fake.FakeHttpLayerManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.refreshing;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.swipeDown;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.LongListMatchers.withAdaptedData;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.LongListMatchers.withItemContent;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class LibraryPageRefreshingTest extends ProtoActivityInstrumentation<DrawerActivity_> {

    public LibraryPageRefreshingTest() {
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
    public void testPullToRefresh() throws InterruptedException {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.BIG_LOOKUP);
        startActivityUnderTest();

        onView(allOf(withId(R.id.refreshLayout), is(not(refreshing()))));

        ResourceLookupsList smallLookUp = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.SMALL_LOOKUP);
        String lastResourceLabel = Queues.newArrayDeque(smallLookUp.getResourceLookups()).getLast().getLabel();

        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.SMALL_LOOKUP);
        onView(withId(android.R.id.list)).perform(swipeDown());
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(lastResourceLabel)))));
        onView(withId(android.R.id.empty)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testEmptyTextShouldBeInVisibleWhileContentExist() throws InterruptedException {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.get().noContent());
        startActivityUnderTest();

        onView(allOf(withId(R.id.refreshLayout), is(not(refreshing()))));
        onView(withId(android.R.id.empty)).check(matches(isDisplayed()));
        onView(withId(android.R.id.empty)).check(matches(withText(R.string.r_browser_nothing_to_display)));
    }

}
