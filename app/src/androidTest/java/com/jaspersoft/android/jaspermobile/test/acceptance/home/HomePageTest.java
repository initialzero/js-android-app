/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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

import android.app.Application;

import com.google.inject.util.Modules;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity_;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.MockedSpiceManager;
import com.jaspersoft.android.jaspermobile.util.ConnectivityUtil;
import com.jaspersoft.android.jaspermobile.util.JsXmlSpiceServiceWrapper;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.SpiceManager;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import roboguice.RoboGuice;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onViewDialogId;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class HomePageTest extends ProtoActivityInstrumentation<HomeActivity_> {

    private static final String PASSWORD = "SOME_PASSWORD";
    private static final String ALIAS = "Mobile Demo";
    private static final String USERNAME = "Joe";
    private static final String ORGANIZATION = "Jasper";

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
    ConnectivityUtil mockConectivityUtil;
    @Mock
    ServerInfo mockServerInfo;

    final MockedSpiceManager mMockedSpiceManager = new MockedSpiceManager(JsXmlSpiceService.class);

    public HomePageTest() {
        super(HomeActivity_.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        when(mockConectivityUtil.isConnected()).thenReturn(true);

        Application application = (Application) this.getInstrumentation()
                .getTargetContext().getApplicationContext();
        RoboGuice.setBaseApplicationInjector(application,
                RoboGuice.DEFAULT_STAGE,
                Modules.override(RoboGuice.newDefaultRoboModule(application))
                        .with(new TestModule()));
        mMockedSpiceManager.setResponseForCacheRequest(mockServerInfo);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        RoboGuice.util.reset();
    }

    public void testMissingNetworkConnectionCase() {
        // Given simulation of connection loss
        when(mockConectivityUtil.isConnected()).thenReturn(false);
        when(mockServerProfile.getPassword()).thenReturn(PASSWORD);
        when(mockRestClient.getServerProfile()).thenReturn(mockServerProfile);

        startActivityUnderTest();
        // Click on any dashboard item
        onView(withId(R.id.home_item_servers)).perform(click());

        onViewDialogId(getActivity(), R.id.sdl__title).check(matches(withText(R.string.h_ad_title_no_connection)));
        onViewDialogId(getActivity(), R.id.sdl__message).check(matches(withText(R.string.h_ad_msg_no_connection)));
    }

    public void testMissingServerProfile() {
        when(mockRestClient.getServerProfile()).thenReturn(null);

        startActivityUnderTest();

        // As soon as we have mocked DbProvider we can not test real Server Profile
        when(mockServerProfile.getAlias()).thenReturn(ALIAS);
        when(mockServerProfile.getPassword()).thenReturn(PASSWORD);
        when(mockRestClient.getServerProfile()).thenReturn(mockServerProfile);

        onView(withText(ProfileHelper.DEFAULT_ALIAS)).perform(click());
        onView(withId(R.id.profile_name)).check(matches(withText(ALIAS)));
    }

    public void testOverAllNavigationForDashboard() {
        // Providing necessary mock for ServerProfile
        when(mockServerProfile.getAlias()).thenReturn(ALIAS);
        when(mockServerProfile.getPassword()).thenReturn(PASSWORD);
        when(mockRestClient.getServerProfile()).thenReturn(mockServerProfile);

        // Cut off SpiceManager
        when(mockJsXmlSpiceServiceWrapper.getSpiceManager()).thenReturn(mMockedSpiceManager);

        startActivityUnderTest();

        // Check ActionBar server name
        onView(withId(R.id.profile_name)).check(matches(withText(ALIAS)));

        onView(withId(R.id.home_item_library)).perform(click());
        pressBack();
        onView(withId(R.id.home_item_repository)).perform(click());
        pressBack();
        onView(withId(R.id.home_item_favorites)).perform(click());
        pressBack();
        onView(withId(R.id.home_item_saved_reports)).perform(click());
        pressBack();
        onView(withId(R.id.home_item_settings)).perform(click());
        pressBack();
        onView(withId(R.id.home_item_servers)).perform(click());
    }

    public void testProfileSwitchAction() {
        when(mockServerProfile.getPassword()).thenReturn(PASSWORD);
        when(mockRestClient.getServerProfile()).thenReturn(mockServerProfile);

        startActivityUnderTest();
        // Given we have no ALIAS set up
        onView(withId(R.id.profile_name)).check(matches(withText("")));

        // When we navigate to Servers page
        onView(withId(R.id.home_item_servers)).perform(click());
        onView(withText(ProfileHelper.DEFAULT_ALIAS)).perform(click());

        // Then we should check for appropriate method call
        verify(mockRestClient, times(3)).getServerProfile();
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).toInstance(mockRestClient);
            bind(JsXmlSpiceServiceWrapper.class).toInstance(mockJsXmlSpiceServiceWrapper);
            bind(DatabaseProvider.class).toInstance(mockDatabaseProvider);
            bind(ConnectivityUtil.class).toInstance(mockConectivityUtil);
        }
    }
}
