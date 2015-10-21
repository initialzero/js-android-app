/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.test.acceptance.save;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.widget.NumberPicker;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.SaveReportActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.apache.http.fake.FakeHttpLayerManager;
import org.hamcrest.core.AllOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasMinValue;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.selectCurrentNumber;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@RunWith(AndroidJUnit4.class)
public class SaveReportValidationsTest extends ProtoActivityInstrumentation<SaveReportActivity_> {

    private ResourceLookup report;

    public SaveReportValidationsTest() {
        super(SaveReportActivity_.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());


        ResourceLookupsList reports = TestResources.get()
                .fromXML(ResourceLookupsList.class, TestResources.ONLY_REPORT);
        report = reports.getResourceLookups().get(0);

        registerTestModule(new HackedTestModule());
        setDefaultCurrentProfile();
        prepareIntent();
        startActivityUnderTest();
    }

    @After
    public void tearDown() throws Exception {
        unregisterTestModule();
        FakeHttpLayerManager.clearHttpResponseRules();
        super.tearDown();
    }

    @Test
    public void testValidateFieldShouldNotAcceptReservedSymbols() {
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
        onView(withId(R.id.report_name_input)).perform(clearText());
        onView(withId(R.id.report_name_input)).perform(typeText("      "));
        onView(withId(R.id.saveAction)).perform(click());

        onView(withId(R.id.report_name_input)).check(matches(hasErrorText(getActivity().getString(R.string.sr_error_field_is_empty))));
    }

    @Test
    public void testValidateFieldShouldNotBeEmpty() throws IOException {
        onView(AllOf.allOf(
                withParent(withId(R.id.tb_navigation)),
                withText(R.string.sr_ab_title)
        )).check(matches(isDisplayed()));

        onView(withId(R.id.report_name_input)).perform(clearText());
        onView(withId(R.id.saveAction)).perform(click());

        onView(withId(R.id.report_name_input)).check(matches(hasErrorText(getActivity().getString(R.string.sr_error_field_is_empty))));
    }

    //---------------------------------------------------------------------
    // Range control
    //---------------------------------------------------------------------

    @Test
    public void testToRangeDependentOnFromPageControl() {
        onView(withId(R.id.fromPageControl)).perform(click());
        onView(withClassName(equalTo(NumberPicker.class.getName()))).perform(selectCurrentNumber(30));
        onView(withText(R.string.ok)).perform(click());

        onView(withId(R.id.toPageControl)).perform(click());
        onView(withClassName(equalTo(NumberPicker.class.getName()))).check(matches(hasMinValue(30)));
    }

    @Test
    public void testToRangeDisabledWhileFromPageHasMaxValue() {
        onView(withId(R.id.fromPageControl)).perform(click());
        onView(withClassName(equalTo(NumberPicker.class.getName()))).perform(selectCurrentNumber(45));
        onView(withText(R.string.ok)).perform(click());

        onView(withId(R.id.toPageControl)).check(matches(not(isEnabled())));
    }

    @Test
    public void testRightMarginAlwaysBiggerOrEqualThanLeftOne() {
        int numberPickerInputId = getActivity().getResources().getIdentifier("numberpicker_input", "id", "android");

        // Select 43
        onView(withId(R.id.fromPageControl)).perform(click());
        onView(withClassName(equalTo(NumberPicker.class.getName()))).perform(selectCurrentNumber(43));
        onView(withText(R.string.ok)).perform(click());

        // Enter in number picker edit field incorrect value
        onView(withId(R.id.toPageControl)).perform(click());
        onView(withId(numberPickerInputId)).perform(clearText());
        onView(withId(numberPickerInputId)).perform(typeText("15"));
        onView(withText(R.string.ok)).perform(click());

        onView(withId(R.id.toPageControl)).check(matches(withText("45")));
    }

    @Test
    public void testLeftMarginAlwaysLowerOrEqualThanRightOne() {
        // Select 10
        onView(withId(R.id.toPageControl)).perform(click());
        onView(withClassName(equalTo(NumberPicker.class.getName()))).perform(selectCurrentNumber(10));
        onView(withText(R.string.ok)).perform(click());

        // Enter in number picker edit field incorrect value
        onView(withId(R.id.fromPageControl)).perform(click());
        onView(withClassName(equalTo(NumberPicker.class.getName()))).perform(selectCurrentNumber(43));
        onView(withText(R.string.ok)).perform(click());

        onView(withId(R.id.fromPageControl)).check(matches(withText("1")));
    }

    @Test
    public void testRangeControlsDoesntAcceptZero() {
        int numberPickerInputId = getActivity().getResources().getIdentifier("numberpicker_input", "id", "android");

        onView(withId(R.id.fromPageControl)).perform(click());
        onView(withId(numberPickerInputId)).perform(clearText());
        onView(withId(numberPickerInputId)).perform(typeText("0"));
        onView(withText(R.string.ok)).perform(click());
        onView(withId(R.id.fromPageControl)).check(matches(withText("1")));

        onView(withId(R.id.toPageControl)).perform(click());
        onView(withId(numberPickerInputId)).perform(clearText());
        onView(withId(numberPickerInputId)).perform(typeText("0"));
        onView(withText(R.string.ok)).perform(click());
        onView(withId(R.id.toPageControl)).check(matches(withText("45")));
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void prepareIntent() {
        Intent metaIntent = new Intent();
        metaIntent.putExtra(SaveReportActivity_.RESOURCE_EXTRA, report);
        metaIntent.putExtra(SaveReportActivity_.PAGE_COUNT_EXTRA, 45);
        setActivityIntent(metaIntent);
    }

}
