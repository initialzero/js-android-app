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

import android.database.Cursor;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.profile.ServersManagerActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.assertThat;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.acceptance.profile.TestServerProfileUtils.deleteTestProfile;
import static com.jaspersoft.android.jaspermobile.test.acceptance.profile.TestServerProfileUtils.queryCreatedProfile;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.Matchers.is;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ServersManagerPageTest extends ProtoActivityInstrumentation<ServersManagerActivity_> {

    public ServersManagerPageTest() {
        super(ServersManagerActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        deleteTestProfile(getInstrumentation().getContext().getContentResolver());
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        deleteTestProfile(getInstrumentation().getContext().getContentResolver());
        super.tearDown();
    }

    public void testValidFormCreation() {
        startActivityUnderTest();

        onView(withId(R.id.addProfile)).perform(click());
        onView(withText(R.string.label_add_profile)).check(matches(isDisplayed()));

        onView(withId(R.id.aliasEdit)).perform(typeText(TestServerProfileUtils.TEST_ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(typeText(TestServerProfileUtils.TEST_SERVER_URL));
        onView(withId(R.id.organizationEdit)).perform(typeText(TestServerProfileUtils.TEST_ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(typeText(TestServerProfileUtils.TEST_USERNAME));
        onView(withId(R.id.passwordEdit)).perform(typeText(TestServerProfileUtils.TEST_PASS));

        onView(withId(R.id.saveAction)).perform(click());

        Cursor cursor = queryCreatedProfile(getActivity().getContentResolver());
        try {
            assertThat(cursor.getCount(), is(1));
        } finally {
            cursor.close();
        }

        onOverflowView(getActivity(), withText(getActivity()
                .getString(R.string.spm_profile_created_toast, TestServerProfileUtils.TEST_ALIAS)))
                .check(matches(isDisplayed()));
    }

}
