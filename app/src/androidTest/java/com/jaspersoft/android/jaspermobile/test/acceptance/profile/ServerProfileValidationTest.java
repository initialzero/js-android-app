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

package com.jaspersoft.android.jaspermobile.test.acceptance.profile;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.profile.ServerProfileActivity_;
import com.jaspersoft.android.jaspermobile.network.ExceptionRule;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.TestResponses;

import org.apache.http.fake.FakeHttpLayerManager;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.TEST_ALIAS;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.TEST_ORGANIZATION;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.TEST_PASS;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.TEST_SERVER_URL;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.TEST_USERNAME;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.deleteTestProfiles;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ServerProfileValidationTest extends ProtoActivityInstrumentation<ServerProfileActivity_> {

    public ServerProfileValidationTest() {
        super(ServerProfileActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteTestProfiles(getInstrumentation().getContext().getContentResolver());
        registerTestModule(new HackedTestModule());
        setDefaultCurrentProfile();
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        deleteTestProfiles(getInstrumentation().getContext().getContentResolver());
        super.tearDown();
    }

    public void testEmptyAliasNotAcceptable() {
        startActivityUnderTest();
        onView(withId(R.id.serverUrlEdit)).perform(typeText(TEST_SERVER_URL));
        onView(withId(R.id.organizationEdit)).perform(typeText(TEST_ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(typeText(TEST_USERNAME));
        onView(withId(R.id.passwordEdit)).perform(typeText(TEST_PASS));

        onView(withId(R.id.saveAction)).perform(click());
        onView(withId(R.id.aliasEdit)).check(matches(hasErrorText(getActivity().getString(R.string.sp_error_field_required))));
    }

    public void testEmptyPasswordNotAcceptable() {
        startActivityUnderTest();
        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(typeText(TEST_SERVER_URL));
        onView(withId(R.id.organizationEdit)).perform(typeText(TEST_ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(typeText(TEST_USERNAME));

        onView(withId(R.id.saveAction)).perform(click());
        onView(withId(R.id.passwordEdit)).check(matches(hasErrorText(getActivity().getString(R.string.sp_error_field_required))));
    }

    public void testEmptyServerUrlNotAcceptable() {
        startActivityUnderTest();
        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS));
        onView(withId(R.id.organizationEdit)).perform(typeText(TEST_ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(typeText(TEST_USERNAME));
        onView(withId(R.id.passwordEdit)).perform(typeText(TEST_PASS));

        onView(withId(R.id.saveAction)).perform(click());
        onView(withId(R.id.serverUrlEdit)).check(matches(hasErrorText(getActivity().getString(R.string.sp_error_field_required))));
    }

    public void testEmptyUsernameNotAcceptable() {
        startActivityUnderTest();
        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(typeText(TEST_SERVER_URL));
        onView(withId(R.id.organizationEdit)).perform(typeText(TEST_ORGANIZATION));
        onView(withId(R.id.passwordEdit)).perform(typeText(TEST_PASS));

        onView(withId(R.id.saveAction)).perform(click());
        onView(withId(R.id.usernameEdit)).check(matches(hasErrorText(getActivity().getString(R.string.sp_error_field_required))));
    }

    public void testServerUrlShouldBeValidUrl() {
        startActivityUnderTest();
        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(typeText("invalid url"));
        onView(withId(R.id.organizationEdit)).perform(typeText(TEST_ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(typeText(TEST_USERNAME));
        onView(withId(R.id.passwordEdit)).perform(typeText(TEST_PASS));

        onView(withId(R.id.saveAction)).perform(click());
        onView(withId(R.id.serverUrlEdit)).check(matches(hasErrorText(getActivity().getString(R.string.sp_error_url_not_valid))));
    }

    // Bug related: When user enter invalid data REST will rise 401
    // As soon as we shared same RequestExceptionHandler for all
    // failure listeners we experienced flow which required customization
    public void testPageShouldProperlyHandleUnAthorized() {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.SERVER_INFO,
                TestResponses.get().notAuthorized());
        startActivityUnderTest();

        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(typeText(TEST_SERVER_URL));
        onView(withId(R.id.organizationEdit)).perform(typeText("some invalid organization"));
        onView(withId(R.id.usernameEdit)).perform(typeText("some invalid username"));
        onView(withId(R.id.passwordEdit)).perform(typeText("some invalid password"));
        onView(withId(R.id.saveAction)).perform(click());

        onOverflowView(getActivity(), withId(R.id.sdl__title)).check(matches(withText(R.string.error_msg)));
        onOverflowView(getActivity(), withId(R.id.sdl__message)).check(matches(withText(ExceptionRule.UNAUTHORIZED.getMessage())));
        onOverflowView(getActivity(), withId(R.id.sdl__negative_button)).check(matches(withText(android.R.string.ok)));
        onOverflowView(getActivity(), withId(R.id.sdl__negative_button)).perform(click());
    }

}
