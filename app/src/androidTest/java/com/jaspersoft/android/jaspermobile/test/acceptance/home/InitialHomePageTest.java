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

package com.jaspersoft.android.jaspermobile.test.acceptance.home;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.util.JsXmlSpiceServiceWrapper;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.exception.NetworkException;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import roboguice.RoboGuice;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.acceptance.profile.TestServerProfileUtils.createDefaultProfile;
import static com.jaspersoft.android.jaspermobile.test.acceptance.profile.TestServerProfileUtils.deleteAll;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class InitialHomePageTest extends ProtoActivityInstrumentation<HomeActivity_> {
    @Mock
    JsXmlSpiceServiceWrapper mockJsXmlSpiceServiceWrapper;

    private JsRestClient jsRestClient;
    final MockedSpiceManager mMockedSpiceManager = new MockedSpiceManager(JsXmlSpiceService.class);

    public InitialHomePageTest() {
        super(HomeActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        when(mockJsXmlSpiceServiceWrapper.getSpiceManager()).thenReturn(mMockedSpiceManager);

        Application application = (Application) this.getInstrumentation()
                .getTargetContext().getApplicationContext();
        RoboGuice.setBaseApplicationInjector(application,
                RoboGuice.DEFAULT_STAGE,
                Modules.override(RoboGuice.newDefaultRoboModule(application))
                        .with(new TestModule()));
        Injector injector = RoboGuice.getBaseApplicationInjector(application);
        jsRestClient = injector.getInstance(JsRestClient.class);

        ContentResolver contentResolver = getInstrumentation()
                .getContext().getContentResolver();
        deleteAll(contentResolver);
        createDefaultProfile(contentResolver);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        RoboGuice.util.reset();
    }

    public void testUserSelectsDefaultProfile() {
        startActivityUnderTest();

        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());
        onOverflowView(getActivity(), withText(getActivity().getString(R.string.h_server_switched_toast, ProfileHelper.DEFAULT_ALIAS)))
                .check(matches(isDisplayed()));

        JsServerProfile serverProfile = jsRestClient.getServerProfile();
        assertThat(serverProfile.getAlias(), is(ProfileHelper.DEFAULT_ALIAS));
        assertThat(serverProfile.getOrganization(), is(ProfileHelper.DEFAULT_ORGANIZATION));
        assertThat(serverProfile.getServerUrl(), is(ProfileHelper.DEFAULT_SERVER_URL));
        assertThat(serverProfile.getUsername(), is(ProfileHelper.DEFAULT_USERNAME));
        assertThat(serverProfile.getPassword(), is(ProfileHelper.DEFAULT_PASS));
    }

    public void testUsersRotateScreen() {
        startActivityUnderTest();

        rotate();

        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());
    }

    public void testProfileIncorrectSetup() throws Throwable {
        startActivityUnderTest();

        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        onView(withId(R.id.home_item_library)).perform(click());
        onOverflowView(getCurrentActivity(), withText(android.R.string.ok)).perform(click());
        onView(withId(getActionBarTitleId())).check(matches(withText(R.string.sp_bc_edit_profile)));
        onView(withId(getActionBarSubTitleId())).check(matches(withText(ProfileHelper.DEFAULT_ALIAS)));

        // We are forcing thread to hold execution, because of another toast message in process of showing
        Thread.sleep(500);
        onView(withId(R.id.saveAction)).perform(click());

        String updateToastMessage = getActivity().getString(R.string.spm_profile_updated_toast, ProfileHelper.DEFAULT_ALIAS);
        onOverflowView(getActivity(), withText(updateToastMessage)).check(matches(isDisplayed()));
    }

    public void testProfileIncorrectSetupWithNoPassword() throws Throwable {
        startActivityUnderTest();

        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        onView(withId(R.id.home_item_library)).perform(click());
        onOverflowView(getCurrentActivity(), withText(android.R.string.ok)).perform(click());
        onView(withId(getActionBarTitleId())).check(matches(withText(R.string.sp_bc_edit_profile)));
        onView(withId(getActionBarSubTitleId())).check(matches(withText(ProfileHelper.DEFAULT_ALIAS)));

        onView(withId(R.id.askPasswordCheckBox)).perform(click());
        onView(withId(R.id.saveAction)).perform(click());

        // Check whether our dialog is shown with Appropriate info
        onOverflowView(getActivity(), withId(R.id.dialogUsernameText)).check(matches(withText(ProfileHelper.DEFAULT_USERNAME)));
        onOverflowView(getActivity(), withId(R.id.dialogOrganizationText)).check(matches(withText(ProfileHelper.DEFAULT_ORGANIZATION)));
        onOverflowView(getActivity(), withId(R.id.dialogOrganizationTableRow)).check(matches(isDisplayed()));

        // Lets type some password and check if it set
        onOverflowView(getActivity(), withId(R.id.dialogPasswordEdit)).perform(typeText(PASSWORD));
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());

        JsServerProfile profile = jsRestClient.getServerProfile();
        assertThat(profile.getPassword(), is(PASSWORD));
    }

    private class MockedSpiceManager extends SpiceManager {
        public MockedSpiceManager(Class<? extends SpiceService> spiceServiceClass) {
            super(spiceServiceClass);
        }

        public <T> void execute(final SpiceRequest<T> request, final Object requestCacheKey,
                                final long cacheExpiryDuration, final RequestListener<T> requestListener) {
            if (request instanceof GetResourceLookupsRequest) {
                HttpStatusCodeException statusCodeException = new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
                NetworkException networkException = new NetworkException(statusCodeException);
                requestListener.onRequestFailure(networkException);
            }
        }

        public <T> void execute(final SpiceRequest<T> request, final RequestListener<T> requestListener) {
        }
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsXmlSpiceServiceWrapper.class).toInstance(mockJsXmlSpiceServiceWrapper);
            bind(JsRestClient.class).in(Singleton.class);
        }
    }
}
