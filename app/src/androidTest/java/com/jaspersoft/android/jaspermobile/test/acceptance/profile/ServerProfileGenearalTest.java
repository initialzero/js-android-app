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

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.profile.ServerProfileActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.TEST_ALIAS;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.TEST_ORGANIZATION;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.TEST_SERVER_URL;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.createTestProfile;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.deleteTestProfiles;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@Ignore
@RunWith(AndroidJUnit4.class)
public class ServerProfileGenearalTest extends ProtoActivityInstrumentation<ServerProfileActivity_> {

    public ServerProfileGenearalTest() {
        super(ServerProfileActivity_.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        registerTestModule(new HackedTestModule());
        setDefaultCurrentProfile();
    }

    @After
    public void tearDown() throws Exception {
        unregisterTestModule();
        deleteTestProfiles(getInstrumentation().getContext().getContentResolver());
        super.tearDown();
    }

    @Test
    public void testFormIsPersistentWhileRotation() {
        startActivityUnderTest();

        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(typeText(TEST_SERVER_URL));
        onView(withId(R.id.organizationEdit)).perform(typeText(TEST_ORGANIZATION));
//        onView(withId(R.id.usernameEdit)).perform(typeText(TEST_USERNAME));
//        onView(withId(R.id.passwordEdit)).perform(typeText(TEST_PASS));

        rotate();
        onView(withId(R.id.aliasEdit)).check(matches(withText(TEST_ALIAS)));
        onView(withId(R.id.serverUrlEdit)).check(matches(withText(TEST_SERVER_URL)));
        onView(withId(R.id.organizationEdit)).check(matches(withText(TEST_ORGANIZATION)));
//        onView(withId(R.id.usernameEdit)).check(matches(withText(TEST_USERNAME)));
//        onView(withId(R.id.passwordEdit)).check(matches(withText(TEST_PASS)));

        rotate();
        onView(withId(R.id.aliasEdit)).check(matches(withText(TEST_ALIAS)));
        onView(withId(R.id.serverUrlEdit)).check(matches(withText(TEST_SERVER_URL)));
        onView(withId(R.id.organizationEdit)).check(matches(withText(TEST_ORGANIZATION)));
//        onView(withId(R.id.usernameEdit)).check(matches(withText(TEST_USERNAME)));
//        onView(withId(R.id.passwordEdit)).check(matches(withText(TEST_PASS)));
    }

    @Test
    public void testFormPopulatedWithDataForExactServerProfile() {
        Intent launchIntent = new Intent();
        launchIntent.putExtra(ServerProfileActivity_.PROFILE_ID_EXTRA,
        createTestProfile(getContentResolver()));
        launchIntent.putExtra(ServerProfileActivity_.IN_EDIT_MODE_EXTRA, true);
        setActivityIntent(launchIntent);
        startActivityUnderTest();

        onView(withId(getActionBarTitleId())).check(matches(withText(R.string.sp_bc_edit_profile)));
        onView(withId(getActionBarSubTitleId())).check(matches(withText(TEST_ALIAS)));
        onView(withId(R.id.aliasEdit)).check(matches(withText(TEST_ALIAS)));
        onView(withId(R.id.serverUrlEdit)).check(matches(withText(TEST_SERVER_URL)));
        onView(withId(R.id.organizationEdit)).check(matches(withText(TEST_ORGANIZATION)));
//        onView(withId(R.id.usernameEdit)).check(matches(withText(TEST_USERNAME)));
//        onView(withId(R.id.passwordEdit)).check(matches(withText(TEST_PASS)));

        int[] fields = {R.id.aliasEdit, R.id.serverUrlEdit,
                R.id.organizationEdit,};
//                R.id.usernameEdit, R.id.passwordEdit};
        for (int field : fields) {
            onView(withId(field)).perform(clearText());
            onView(withId(field)).perform(typeText("_suffix"));
        }

        rotate();

        for (int field : fields) {
            onView(withId(field)).check(matches(withText("_suffix")));
        }
    }

    @Test
    public void testAskForPasswordCheckedForProfileWithoutPassword() {
        Intent launchIntent = new Intent();
        long profileId = createTestProfile(getContentResolver());
        launchIntent.putExtra(ServerProfileActivity_.PROFILE_ID_EXTRA, profileId);
//        updateProfile(getContentResolver(), profileId, ServerProfilesTable.PASSWORD, "");
        setActivityIntent(launchIntent);
        startActivityUnderTest();

//        onView(withId(R.id.passwordEdit)).check(matches(withText("")));
//        onView(withId(R.id.passwordEdit)).check(matches(not(isEnabled())));
//        onView(withId(R.id.passwordEdit)).check(matches(not(isFocusable())));
//        onView(withId(R.id.askPasswordCheckBox)).check(matches(isChecked()));
    }

}
