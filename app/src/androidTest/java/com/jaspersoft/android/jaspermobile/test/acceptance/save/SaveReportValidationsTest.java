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

package com.jaspersoft.android.jaspermobile.test.acceptance.save;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.report.SaveReportActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@RunWith(AndroidJUnit4.class)
public class SaveReportValidationsTest extends ProtoActivityInstrumentation<SaveReportActivity_> {

    protected static final String RESOURCE_URI = "/Reports/2_Sales_Mix_by_Demographic_Report";
    protected static final String RESOURCE_LABEL = "02. Sales Mix by Demographic Report";

    public SaveReportValidationsTest() {
        super(SaveReportActivity_.class);
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
        super.tearDown();
    }

    @Test
    public void testValidateFieldShouldNotAcceptReservedSymbols() {
        prepareIntent();
        startActivityUnderTest();

        char[] chars = {'*', '\\', '/', '"', '\'', ':', '?', '|', '<', '>', '+', '[', ']'};

        for (char symbol : chars) {
            onView(withId(R.id.report_name_input)).perform(clearText());
            onView(withId(R.id.report_name_input)).perform(typeText(String.valueOf(symbol)));
            onView(withId(R.id.saveAction)).perform(click());
            onView(withId(R.id.report_name_input)).check(matches(hasErrorText(getActivity().getString(R.string.sr_error_characters_not_allowed))));
        }
    }

    @Test
    public void testValidateFieldShouldNotAcceptOnlySpaces() {
        prepareIntent();
        startActivityUnderTest();

        onView(withId(R.id.report_name_input)).perform(clearText());
        onView(withId(R.id.report_name_input)).perform(typeText("      "));
        onView(withId(R.id.saveAction)).perform(click());

        onView(withId(R.id.report_name_input)).check(matches(hasErrorText(getActivity().getString(R.string.sr_error_field_is_empty))));
    }

    @Test
    public void testValidateFieldShouldNotBeEmpty() {
        prepareIntent();
        startActivityUnderTest();
        onView(withId(getActionBarTitleId())).check(matches(withText(R.string.sr_ab_title)));

        onView(withId(R.id.report_name_input)).perform(clearText());
        onView(withId(R.id.saveAction)).perform(click());

        onView(withId(R.id.report_name_input)).check(matches(hasErrorText(getActivity().getString(R.string.sr_error_field_is_empty))));
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void prepareIntent() {
        Intent metaIntent = new Intent();
        metaIntent.putExtra(SaveReportActivity_.RESOURCE_LABEL_EXTRA, RESOURCE_LABEL);
        metaIntent.putExtra(SaveReportActivity_.RESOURCE_URI_EXTRA, RESOURCE_URI);
        setActivityIntent(metaIntent);
    }

}
