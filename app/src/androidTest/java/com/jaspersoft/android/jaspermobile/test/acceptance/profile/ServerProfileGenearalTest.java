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

import android.content.ContentResolver;
import android.content.Intent;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.profile.ServerProfileActivity_;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isChecked;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isEnabled;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isFocusable;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.TEST_ALIAS;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.TEST_ORGANIZATION;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.TEST_PASS;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.TEST_SERVER_URL;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.TEST_USERNAME;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.createTestProfile;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.deleteTestProfile;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.updateProfile;
import static org.hamcrest.Matchers.not;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ServerProfileGenearalTest extends ProtoActivityInstrumentation<ServerProfileActivity_> {

    public ServerProfileGenearalTest() {
        super(ServerProfileActivity_.class);
    }

    @Override
    protected void tearDown() throws Exception {
        deleteTestProfile(getInstrumentation().getContext().getContentResolver());
        super.tearDown();
    }

    public void testFormIsPersistentWhileRotation() {
        startActivityUnderTest();

        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(typeText(TEST_SERVER_URL));
        onView(withId(R.id.organizationEdit)).perform(typeText(TEST_ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(typeText(TEST_USERNAME));
        onView(withId(R.id.passwordEdit)).perform(typeText(TEST_PASS));

        rotate();
        onView(withId(R.id.aliasEdit)).check(matches(withText(TEST_ALIAS)));
        onView(withId(R.id.serverUrlEdit)).check(matches(withText(TEST_SERVER_URL)));
        onView(withId(R.id.organizationEdit)).check(matches(withText(TEST_ORGANIZATION)));
        onView(withId(R.id.usernameEdit)).check(matches(withText(TEST_USERNAME)));
        onView(withId(R.id.passwordEdit)).check(matches(withText(TEST_PASS)));

        rotate();
        onView(withId(R.id.aliasEdit)).check(matches(withText(TEST_ALIAS)));
        onView(withId(R.id.serverUrlEdit)).check(matches(withText(TEST_SERVER_URL)));
        onView(withId(R.id.organizationEdit)).check(matches(withText(TEST_ORGANIZATION)));
        onView(withId(R.id.usernameEdit)).check(matches(withText(TEST_USERNAME)));
        onView(withId(R.id.passwordEdit)).check(matches(withText(TEST_PASS)));
    }

    public void testFormPopulatedWithDataForExactServerProfile() {
        Intent launchIntent = new Intent();
        launchIntent.putExtra(ServerProfileActivity_.PROFILE_ID_EXTRA,
                createTestProfile(getInstrumentation().getContext().getContentResolver()));
        setActivityIntent(launchIntent);
        startActivityUnderTest();

        onView(withId(getActionBarTitleId())).check(matches(withText(R.string.sp_bc_edit_profile)));
        onView(withId(getActionBarSubTitleId())).check(matches(withText(TEST_ALIAS)));
        onView(withId(R.id.aliasEdit)).check(matches(withText(TEST_ALIAS)));
        onView(withId(R.id.serverUrlEdit)).check(matches(withText(TEST_SERVER_URL)));
        onView(withId(R.id.organizationEdit)).check(matches(withText(TEST_ORGANIZATION)));
        onView(withId(R.id.usernameEdit)).check(matches(withText(TEST_USERNAME)));
        onView(withId(R.id.passwordEdit)).check(matches(withText(TEST_PASS)));

        int[] fields = {R.id.aliasEdit, R.id.serverUrlEdit,
                R.id.organizationEdit, R.id.usernameEdit, R.id.passwordEdit};
        for (int field : fields) {
            onView(withId(field)).perform(clearText());
            onView(withId(field)).perform(typeText("_suffix"));
        }

        rotate();

        for (int field : fields) {
            onView(withId(field)).check(matches(withText("_suffix")));
        }
    }

    public void testAskForPasswordCheckedForProfileWithoutPassword() {
        Intent launchIntent = new Intent();
        ContentResolver contentResolver = getInstrumentation().getContext().getContentResolver();
        long profileId = createTestProfile(contentResolver);
        launchIntent.putExtra(ServerProfileActivity_.PROFILE_ID_EXTRA, profileId);
        updateProfile(contentResolver, profileId, ServerProfilesTable.PASSWORD, "");
        setActivityIntent(launchIntent);
        startActivityUnderTest();

        onView(withId(R.id.passwordEdit)).check(matches(withText("")));
        onView(withId(R.id.passwordEdit)).check(matches(not(isEnabled())));
        onView(withId(R.id.passwordEdit)).check(matches(not(isFocusable())));
        onView(withId(R.id.askPasswordCheckBox)).check(matches(isChecked()));
    }

}
