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

package com.jaspersoft.android.jaspermobile.test.acceptance;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;
import com.octo.android.robospice.persistence.DurationInMillis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest extends ProtoActivityInstrumentation<SettingsActivity_> {

    private DefaultPrefHelper_ prefHelper;

    public SettingsActivityTest() {
        super(SettingsActivity_.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        prefHelper = DefaultPrefHelper_.getInstance_(getInstrumentation().getContext());
        startActivityUnderTest();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testReadTimeOutShouldNotAcceptIncorrectInteger() {
        startActivityUnderTest();
        onView(withText(R.string.st_category_connection)).perform(click());
        onView(withText(R.string.st_title_read_timeout)).perform(click());
        onOverflowView(getActivity(), withId(android.R.id.edit)).perform(typeText("50000000000"));
        onOverflowView(getActivity(), withId(android.R.id.button1)).perform(click());
        onOverflowView(getActivity(), withText(R.string.st_invalid_number_format)).check(matches(isDisplayed()));

        int expectedTimeOut = (int) TimeUnit.SECONDS.toMillis(Integer.valueOf(DefaultPrefHelper.DEFAULT_READ_TIMEOUT));
        assertThat(prefHelper.getReadTimeoutValue(), is(expectedTimeOut));
    }

    @Test
    public void testConnectionTimeOutShouldNotAcceptIncorrectInteger() {
        startActivityUnderTest();
        onView(withText(R.string.st_category_connection)).perform(click());
        onView(withText(R.string.st_title_connect_timeout)).perform(click());
        onOverflowView(getActivity(), withId(android.R.id.edit)).perform(typeText("50000000000"));
        onOverflowView(getActivity(), withId(android.R.id.button1)).perform(click());
        onOverflowView(getActivity(), withText(R.string.st_invalid_number_format)).check(matches(isDisplayed()));

        int expectedTimeOut = (int) TimeUnit.SECONDS.toMillis(Integer.valueOf(DefaultPrefHelper.DEFAULT_CONNECT_TIMEOUT));
        assertThat(prefHelper.getConnectTimeoutValue(), is(expectedTimeOut));
    }

    @Test
    public void testConnectionCacheExpirationShouldNotAcceptIncorrectInteger() {
        prefHelper.setRepoCacheEnabled(true);
        startActivityUnderTest();

        onView(withText(R.string.st_category_repo_cache)).perform(click());
        onView(withText(R.string.st_title_cache_expiration)).perform(click());
        onOverflowView(getActivity(), withId(android.R.id.edit)).perform(typeText("214748364799"));
        onOverflowView(getActivity(), withId(android.R.id.button1)).perform(click());
        onOverflowView(getActivity(), withText(R.string.st_invalid_number_format)).check(matches(isDisplayed()));

        long defaultValue = Long.valueOf(DefaultPrefHelper.DEFAULT_REPO_CACHE_EXPIRATION) * DurationInMillis.ONE_HOUR;
        assertThat(prefHelper.getRepoCacheExpirationValue(), is(defaultValue));
    }

//    @Test
//    public void testShowAboutInfo() {
//        startActivityUnderTest();
//        onView(withId(R.id.showAbout)).perform(click());
//
//        onOverflowView(getActivity(), withText(R.string.sa_show_about)).check(matches(isDisplayed()));
//        onOverflowView(getActivity(), withId(android.R.id.message)).check(matches(isDisplayed()));
//    }

}
