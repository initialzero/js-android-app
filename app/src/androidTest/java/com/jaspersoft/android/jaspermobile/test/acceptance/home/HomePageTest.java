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

package com.jaspersoft.android.jaspermobile.test.acceptance.home;

import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.TestResponses;
import com.jaspersoft.android.jaspermobile.util.ConnectivityUtil;

import org.apache.http.fake.FakeHttpLayerManager;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.0
 */
    public class HomePageTest extends ProtoActivityInstrumentation<HomeActivity_> {

    @Mock
    ConnectivityUtil mockConectivityUtil;

    public HomePageTest() {
        super(HomeActivity_.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        when(mockConectivityUtil.isConnected()).thenReturn(true);

        registerTestModule(new TestModule());
        setDefaultCurrentProfile();
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        super.tearDown();
    }

    public void testMissingNetworkConnectionCase() {
        // Given simulation of connection loss
        when(mockConectivityUtil.isConnected()).thenReturn(false);

        startActivityUnderTest();
        int[] ids = {R.id.home_item_library, R.id.home_item_repository, R.id.home_item_favorites};

        for (int id : ids) {
            // Click on dashboard item
            onView(withId(id)).perform(click());

            onOverflowView(getActivity(), withId(R.id.sdl__title)).check(matches(withText(R.string.h_ad_title_no_connection)));
            onOverflowView(getActivity(), withId(R.id.sdl__message)).check(matches(withText(R.string.h_ad_msg_no_connection)));
            onOverflowView(getActivity(), withId(R.id.sdl__positive_button)).perform(click());
        }
    }

    public void testOverAllNavigationForDashboard() {
        startActivityUnderTest();

        // Check ActionBar server name
        onView(withId(R.id.profile_name)).check(matches(withText("Mobile Demo")));

        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.ONLY_FOLDER);
        onView(withId(R.id.home_item_library)).perform(click());
        Espresso.pressBack();

        FakeHttpLayerManager.clearHttpResponseRules();
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.GET_ROOT_FOLDER,
                TestResponses.ROOT_FOLDER);
        onView(withId(R.id.home_item_repository)).perform(click());
        Espresso.pressBack();

        onView(withId(R.id.home_item_favorites)).perform(click());
        Espresso.pressBack();
        onView(withId(R.id.home_item_saved_reports)).perform(click());
        Espresso.pressBack();
        onView(withId(R.id.home_item_settings)).perform(click());
        Espresso.pressBack();
        onView(withId(R.id.home_item_servers)).perform(click());
    }

    private class TestModule extends HackedTestModule {
        @Override
        protected void semanticConfigure() {
           super.semanticConfigure();
            bind(ConnectivityUtil.class).toInstance(mockConectivityUtil);
        }
    }
}
