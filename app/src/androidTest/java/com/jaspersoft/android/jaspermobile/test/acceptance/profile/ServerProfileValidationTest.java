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

package com.jaspersoft.android.jaspermobile.test.acceptance.profile;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.profile.ServerProfileActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.TEST_ALIAS;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.TEST_ORGANIZATION;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.TEST_PASS;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.TEST_SERVER_URL;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.TEST_USERNAME;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.deleteTestProfile;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;

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
        startActivityUnderTest();
    }

    @Override
    protected void tearDown() throws Exception {
        deleteTestProfile(getInstrumentation().getContext().getContentResolver());
        super.tearDown();
    }

    public void testEmptyAliasNotAcceptable() {
        onView(withId(R.id.serverUrlEdit)).perform(typeText(TEST_SERVER_URL));
        onView(withId(R.id.organizationEdit)).perform(typeText(TEST_ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(typeText(TEST_USERNAME));
        onView(withId(R.id.passwordEdit)).perform(typeText(TEST_PASS));

        onView(withId(R.id.saveAction)).perform(click());
        onView(withId(R.id.aliasEdit)).check(matches(hasErrorText(getActivity().getString(R.string.sp_error_field_required))));
    }

    public void testEmptyPasswordNotAcceptable() {
        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(typeText(TEST_SERVER_URL));
        onView(withId(R.id.organizationEdit)).perform(typeText(TEST_ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(typeText(TEST_USERNAME));

        onView(withId(R.id.saveAction)).perform(click());
        onView(withId(R.id.passwordEdit)).check(matches(hasErrorText(getActivity().getString(R.string.sp_error_field_required))));
    }

    public void testEmptyServerUrlNotAcceptable() {
        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS));
        onView(withId(R.id.organizationEdit)).perform(typeText(TEST_ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(typeText(TEST_USERNAME));
        onView(withId(R.id.passwordEdit)).perform(typeText(TEST_PASS));

        onView(withId(R.id.saveAction)).perform(click());
        onView(withId(R.id.serverUrlEdit)).check(matches(hasErrorText(getActivity().getString(R.string.sp_error_field_required))));
    }

    public void testEmptyUsernameNotAcceptable() {
        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(typeText(TEST_SERVER_URL));
        onView(withId(R.id.organizationEdit)).perform(typeText(TEST_ORGANIZATION));
        onView(withId(R.id.passwordEdit)).perform(typeText(TEST_PASS));

        onView(withId(R.id.saveAction)).perform(click());
        onView(withId(R.id.usernameEdit)).check(matches(hasErrorText(getActivity().getString(R.string.sp_error_field_required))));
    }

    public void testServerUrlShouldBeValidUrl() {
        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(typeText("invalid url"));
        onView(withId(R.id.organizationEdit)).perform(typeText(TEST_ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(typeText(TEST_USERNAME));
        onView(withId(R.id.passwordEdit)).perform(typeText(TEST_PASS));

        onView(withId(R.id.saveAction)).perform(click());
        onView(withId(R.id.serverUrlEdit)).check(matches(hasErrorText(getActivity().getString(R.string.sp_error_url_not_valid))));
    }

}
