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

package com.jaspersoft.android.jaspermobile.test.acceptance.home;

import android.database.Cursor;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.TestResponses;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper;

import org.apache.http.fake.FakeHttpLayerManager;
import org.hamcrest.Matchers;
import org.mockito.MockitoAnnotations;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.longClick;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.createOnlyDefaultProfile;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.deleteAllProfiles;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class AskPasswordHomePageTest extends ProtoActivityInstrumentation<HomeActivity_> {

    public AskPasswordHomePageTest() {
        super(HomeActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        registerTestModule(new SpiceAwareModule());
        createOnlyDefaultProfile(getContentResolver());
    }

    @Override
    protected void tearDown() throws Exception {
        deleteAllProfiles(getContentResolver());
        unregisterTestModule();
        super.tearDown();
    }

    public void testCorrectPasswordSetup() throws Throwable {
        startActivityUnderTest();
        setAskForPasswordOption();

        // Check whether our dialog is shown with Appropriate info
        onOverflowView(getActivity(), withId(R.id.dialogUsernameText)).check(matches(withText(ProfileHelper.DEFAULT_USERNAME)));
        onOverflowView(getActivity(), withId(R.id.dialogOrganizationText)).check(matches(withText(ProfileHelper.DEFAULT_ORGANIZATION)));
        onOverflowView(getActivity(), withId(R.id.dialogOrganizationTableRow)).check(matches(isDisplayed()));

        // Lets type some password and check if it set
        onOverflowView(getActivity(), withId(R.id.dialogPasswordEdit)).perform(typeText(PASSWORD));
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());

        assertThat(getServerProfile().getPassword(), is(PASSWORD));
    }

    public void testPasswordValidationCase() throws Throwable {
        startActivityUnderTest();
        setAskForPasswordOption();

        // Lets type some invalid password and check validation
        onOverflowView(getActivity(), withId(R.id.dialogPasswordEdit)).perform(clearText());
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());
        onOverflowView(getActivity(), withId(R.id.dialogPasswordEdit))
                .check(matches(hasErrorText(getActivity().getString(R.string.sp_error_field_required))));
    }

    public void testPasswordPersistedAfterRotation() throws Throwable {
        startActivityUnderTest();
        setAskForPasswordOption();

        // Lets type some password and check if it set
        onOverflowView(getActivity(), withId(R.id.dialogPasswordEdit)).perform(typeText(PASSWORD));
        rotate();
        onOverflowView(getActivity(), withId(R.id.dialogPasswordEdit)).check(matches(withText(PASSWORD)));
    }

    private void setAskForPasswordOption() throws Throwable {
        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.ROOT_FOLDER_CONTENT,
                TestResponses.get().notAuthorized());
        onView(withId(R.id.home_item_library)).perform(click());

        onOverflowView(getCurrentActivity(), withText(android.R.string.ok)).perform(click());
        onView(withId(getActionBarTitleId())).check(matches(withText(R.string.sp_bc_edit_profile)));
        onView(withId(getActionBarSubTitleId())).check(matches(withText(ProfileHelper.DEFAULT_ALIAS)));

        onView(withId(R.id.askPasswordCheckBox)).perform(click());
        onView(withId(R.id.saveAction)).perform(click());
    }


    public void testAlwaysAskForPasswordShouldBeActiveOnActivityCancelState() {
        setDefaultCurrentProfile();
        startActivityUnderTest();

        onView(withId(R.id.home_item_servers)).perform(click());

        onData(Matchers.is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.editItem)).perform(click());

        onView(withId(R.id.askPasswordCheckBox)).perform(click());
        onView(withId(R.id.saveAction)).perform(click());

        pressBack();

        // Check whether our dialog is shown with Appropriate info
        onOverflowView(getActivity(), withId(R.id.dialogUsernameText)).check(matches(withText(ProfileHelper.DEFAULT_USERNAME)));
        onOverflowView(getActivity(), withId(R.id.dialogOrganizationText)).check(matches(withText(ProfileHelper.DEFAULT_ORGANIZATION)));
        onOverflowView(getActivity(), withId(R.id.dialogOrganizationTableRow)).check(matches(isDisplayed()));
    }

}
