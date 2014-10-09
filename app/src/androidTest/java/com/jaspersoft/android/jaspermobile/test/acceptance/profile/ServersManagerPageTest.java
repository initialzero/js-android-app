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

import android.database.Cursor;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.profile.ServersManagerActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils;
import com.jaspersoft.android.jaspermobile.test.utils.SmartMockedSpiceManager;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.util.JsSpiceManager;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;

import org.mockito.MockitoAnnotations;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.longClick;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.assertThat;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.TEST_ALIAS;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.TEST_ORGANIZATION;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.TEST_PASS;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.TEST_SERVER_URL;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.TEST_USERNAME;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.createTestProfile;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.deleteTestProfiles;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.queryTestProfile;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasTotalCount;
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

    private SmartMockedSpiceManager mMockedSpiceManager;
    private ServerInfo serverInfo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteTestProfiles(getInstrumentation().getContext().getContentResolver());
        serverInfo = TestResources.get().fromXML(ServerInfo.class, "server_info");

        MockitoAnnotations.initMocks(this);
        mMockedSpiceManager = SmartMockedSpiceManager.getInstance();
        mMockedSpiceManager.addNetworkResponse(serverInfo);
        registerTestModule(new TestModule());
        startActivityUnderTest();
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        deleteTestProfiles(getInstrumentation().getContext().getContentResolver());
        super.tearDown();
    }

    public void testValidFormCreation() {
        startActivityUnderTest();

        onView(withId(R.id.addProfile)).perform(click());
        onView(withText(R.string.label_add_profile)).check(matches(isDisplayed()));

        onView(withId(R.id.aliasEdit)).perform(typeText(DatabaseUtils.TEST_ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(typeText(DatabaseUtils.TEST_SERVER_URL));
        onView(withId(R.id.organizationEdit)).perform(typeText(DatabaseUtils.TEST_ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(typeText(DatabaseUtils.TEST_USERNAME));
        onView(withId(R.id.passwordEdit)).perform(typeText(DatabaseUtils.TEST_PASS));

        onView(withId(R.id.saveAction)).perform(click());

        Cursor cursor = queryTestProfile(getActivity().getContentResolver());
        try {
            assertThat(cursor.getCount(), is(1));
        } finally {
            cursor.close();
        }
    }

    public void testServerLowerThanEmeraldNotAcceptable() {
        mMockedSpiceManager.clearNetworkResponses();
        serverInfo.setVersionCode(ServerInfo.VERSION_CODES.EMERALD_MR1);
        mMockedSpiceManager.addNetworkResponse(serverInfo);

        onView(withId(R.id.addProfile)).perform(click());

        onView(withId(R.id.aliasEdit)).perform(typeText(DatabaseUtils.TEST_ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(typeText(DatabaseUtils.TEST_SERVER_URL));
        onView(withId(R.id.organizationEdit)).perform(typeText(DatabaseUtils.TEST_ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(typeText(DatabaseUtils.TEST_USERNAME));
        onView(withId(R.id.passwordEdit)).perform(typeText(DatabaseUtils.TEST_PASS));

        onView(withId(R.id.saveAction)).perform(click());

        onOverflowView(getActivity(), withId(R.id.sdl__title)).check(matches(withText(R.string.error_msg)));
        onOverflowView(getActivity(), withId(R.id.sdl__message)).check(matches(withText(R.string.r_error_server_not_supported)));
    }

    public void testServerAliasShouldBeUniqueDuringCreation() {
        createTestProfile(getActivity().getContentResolver());
        startActivityUnderTest();

        onView(withId(R.id.addProfile)).perform(click());
        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(typeText(TEST_SERVER_URL));
        onView(withId(R.id.organizationEdit)).perform(typeText(TEST_ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(typeText(TEST_USERNAME));
        onView(withId(R.id.passwordEdit)).perform(typeText(TEST_PASS));

        onView(withId(R.id.saveAction)).perform(click());
        onView(withId(R.id.aliasEdit)).check(matches(hasErrorText(getActivity().getString(R.string.sp_error_duplicate_alias))));

        onView(withId(R.id.aliasEdit)).perform(clearText());
        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS + "_suffix"));
        onView(withId(R.id.saveAction)).perform(click());

        onView(withText(TEST_ALIAS + "_suffix")).check(matches(isDisplayed()));
    }

    public void testServerAliasShouldBeUniqueDuringUpdate() {
        createTestProfile(getActivity().getContentResolver());
        startActivityUnderTest();

        onView(withId(R.id.addProfile)).perform(click());
        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS + "_suffix"));
        onView(withId(R.id.serverUrlEdit)).perform(typeText(TEST_SERVER_URL));
        onView(withId(R.id.organizationEdit)).perform(typeText(TEST_ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(typeText(TEST_USERNAME));
        onView(withId(R.id.passwordEdit)).perform(typeText(TEST_PASS));
        onView(withId(R.id.saveAction)).perform(click());

        onView(withText(TEST_ALIAS + "_suffix")).perform(longClick());
        onView(withId(R.id.editItem)).perform(click());

        onView(withId(R.id.aliasEdit)).perform(clearText());
        onView(withId(R.id.aliasEdit)).perform(typeText(TEST_ALIAS));

        onView(withId(R.id.saveAction)).perform(click());
        onView(withId(R.id.aliasEdit)).check(matches(hasErrorText(getActivity().getString(R.string.sp_error_duplicate_alias))));
    }

    public void testNotActiveServerProfileCanBeDeleted() {
        createTestProfile(getActivity().getContentResolver());
        startActivityUnderTest();

        onView(withText(TEST_ALIAS)).perform(longClick());
        onView(withId(R.id.deleteItem)).perform(click());
        onOverflowView(getActivity(), withText(R.string.spm_delete_btn)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(1));
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsSpiceManager.class).toInstance(mMockedSpiceManager);
        }
    }

}
