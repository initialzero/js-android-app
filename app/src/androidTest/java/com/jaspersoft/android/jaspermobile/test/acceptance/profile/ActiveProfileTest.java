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

package com.jaspersoft.android.jaspermobile.test.acceptance.profile;

import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.profile.ServersManagerActivity_;
import com.jaspersoft.android.jaspermobile.network.ExceptionRule;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.TestResponses;
import com.jaspersoft.android.sdk.client.JsServerProfile;

import org.apache.http.fake.FakeHttpLayerManager;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Tom Koptel
 * @since 1.9
 * <p/>
 * Bug related: When user changes data of currently selected profile.
 * For instance from one user to another. Then he should be automatically signed.
 * One of constraints to this trait is that update of Server profile data will
 * undergo check on the server side by simple call to ServerInfo.
 */
@Ignore
@RunWith(AndroidJUnit4.class)
public class ActiveProfileTest extends ProtoActivityInstrumentation<ServersManagerActivity_> {

    public ActiveProfileTest() {
        super(ServersManagerActivity_.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        registerTestModule(new HackedTestModule());
        setDefaultCurrentProfile();
        FakeHttpLayerManager.clearHttpResponseRules();
        startActivityUnderTest();
    }

    @After
    public void tearDown() throws Exception {
        unregisterTestModule();
        super.tearDown();
    }

    @Test
    public void testValidChangesToProfileWillBePersisted() {
        onData(Matchers.is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.editItem)).perform(click());

        onView(withId(R.id.organizationEdit)).perform(clearText());
//        onView(withId(R.id.usernameEdit)).perform(clearText());
//        onView(withId(R.id.usernameEdit)).perform(typeText("another_user"));
//        onView(withId(R.id.passwordEdit)).perform(clearText());
//        onView(withId(R.id.passwordEdit)).perform(typeText("1234"));

        // We test valid creation case.
        FakeHttpLayerManager.addHttpResponseRule(ApiMatcher.SERVER_INFO, TestResponses.SERVER_INFO);
        onView(withId(R.id.saveAction)).perform(click());

        JsServerProfile jsServerProfile = getJsRestClient().getServerProfile();
        assertThat(jsServerProfile.getOrganization(), is(""));
        assertThat(jsServerProfile.getUsername(), is("another_user"));
        assertThat(jsServerProfile.getPassword(), is("1234"));
    }

    @Test
    public void testInValidChangesToProfileWillBeIgnored() {
        onData(Matchers.is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.editItem)).perform(click());

        onView(withId(R.id.organizationEdit)).perform(clearText());
//        onView(withId(R.id.usernameEdit)).perform(clearText());
//        onView(withId(R.id.usernameEdit)).perform(typeText("invalid_user"));
//        onView(withId(R.id.passwordEdit)).perform(clearText());
//        onView(withId(R.id.passwordEdit)).perform(typeText("1234"));

        // We test invalid creation case.
        FakeHttpLayerManager.addHttpResponseRule(ApiMatcher.SERVER_INFO, TestResponses.get().notAuthorized());
        onView(withId(R.id.saveAction)).perform(click());

        // Assert out profile still the same
        JsServerProfile jsServerProfile = getJsRestClient().getServerProfile();
        assertThat(jsServerProfile.getOrganization(), is(not("")));
        assertThat(jsServerProfile.getUsername(), is(not("invalid_user")));
        assertThat(jsServerProfile.getPassword(), is(not("1234")));

        // We also should see 401 error dialog
        onOverflowView(getActivity(), withId(R.id.sdl__title)).check(matches(withText(R.string.error_msg)));
        onOverflowView(getActivity(), withId(R.id.sdl__message)).check(matches(withText(ExceptionRule.UNAUTHORIZED.getMessage())));
        onOverflowView(getActivity(), withId(R.id.sdl__negative_button)).check(matches(withText(android.R.string.ok)));
        onOverflowView(getActivity(), withId(R.id.sdl__negative_button)).perform(click());
    }

    @Test
    public void testOldServerInstanceShouldBeIgnored() {
        onData(Matchers.is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.editItem)).perform(click());

        onView(withId(R.id.organizationEdit)).perform(clearText());
//        onView(withId(R.id.usernameEdit)).perform(clearText());
//        onView(withId(R.id.usernameEdit)).perform(typeText("another_user"));
//        onView(withId(R.id.passwordEdit)).perform(clearText());
//        onView(withId(R.id.passwordEdit)).perform(typeText("1234"));

        // We will send server info with unsupported server version
        FakeHttpLayerManager.addHttpResponseRule(ApiMatcher.SERVER_INFO, TestResponses.EMERALD_MR1_SERVER_INFO);
        onView(withId(R.id.saveAction)).perform(click());

        // Assert out profile still the same
        JsServerProfile jsServerProfile = getJsRestClient().getServerProfile();
        assertThat(jsServerProfile.getOrganization(), is(not("")));
        assertThat(jsServerProfile.getUsername(), is(not("invalid_user")));
        assertThat(jsServerProfile.getPassword(), is(not("1234")));

        // We also should see info dialog about old JRS usage
        onOverflowView(getActivity(), withId(R.id.sdl__title)).check(matches(withText(R.string.error_msg)));
        onOverflowView(getActivity(), withId(R.id.sdl__message)).check(matches(withText(R.string.r_error_server_not_supported)));
    }

    @Test
    public void testSelectionOfAlreadyActiveProfileChangeNothing() {
        onData(Matchers.is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());
        assertThat(FakeHttpLayerManager.getUnHandledRequestsCount(), is(0));
    }

    // Bug related. When user selects active profile and apply change it should be
    // persisted properly. There was a case when app lost reference to the profile
    // because of wrong saved data.
    @Test
    public void testProfileShouldBeActiveAfterUpdate() {
        onData(Matchers.is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.editItem)).perform(click());

        onView(withId(R.id.organizationEdit)).perform(clearText());
//        onView(withId(R.id.usernameEdit)).perform(clearText());
//        onView(withId(R.id.usernameEdit)).perform(typeText("another_user"));
//        onView(withId(R.id.passwordEdit)).perform(clearText());
//        onView(withId(R.id.passwordEdit)).perform(typeText("1234"));
        onView(withId(R.id.saveAction)).perform(click());

        JsServerProfile profile = getJsRestClient().getServerProfile();
        assertThat(profile.getId(), is(not(0l)));
        assertThat(profile.getPassword(), is("1234"));
        assertThat(profile.getUsername(), is("another_user"));
        assertThat(profile.getOrganization(), is(""));
    }

    // Bug related. All alterations around password should be persisted properly.
    // There was a case when user set ask for password and then altered password data again.
    // This behavior was buggy and user saw previously saved state, not the one he currently applied.
    @Test
    public void testProfilePasswordResetShouldBePersistent() {
        FakeHttpLayerManager.addHttpResponseRule(ApiMatcher.SERVER_INFO, TestResponses.SERVER_INFO);
        onData(Matchers.is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.editItem)).perform(click());

        onView(withId(R.id.organizationEdit)).perform(clearText());
//        onView(withId(R.id.askPasswordCheckBox)).perform(click());
        onView(withId(R.id.saveAction)).perform(click());

        onData(Matchers.is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.editItem)).perform(click());
//        onView(withId(R.id.askPasswordCheckBox)).perform(click());
//        onView(withId(R.id.passwordEdit)).perform(typeText("1234"));
        onView(withId(R.id.saveAction)).perform(click());


        onData(Matchers.is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.editItem)).perform(click());
//        onView(withId(R.id.passwordEdit)).check(matches(isEnabled()));
//        onView(withId(R.id.passwordEdit)).check(matches(hasText("1234")));
    }

}
