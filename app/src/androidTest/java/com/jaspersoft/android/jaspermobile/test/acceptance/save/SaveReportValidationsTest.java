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

import com.google.common.base.Preconditions;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.report.SaveReportActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.test.utils.TestResponses;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.apache.commons.io.FileUtils;
import org.apache.http.fake.FakeHttpLayerManager;
import org.apache.http.fake.TestHttpResponse;

import java.io.File;
import java.io.IOException;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class SaveReportValidationsTest extends ProtoActivityInstrumentation<SaveReportActivity_> {

    private ResourceLookup report;

    public SaveReportValidationsTest() {
        super(SaveReportActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ResourceLookupsList reports = TestResources.get()
                .fromXML(ResourceLookupsList.class, TestResources.ONLY_REPORT);
        report = reports.getResourceLookups().get(0);

        registerTestModule(new HackedTestModule());
        setDefaultCurrentProfile();
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        FakeHttpLayerManager.clearHttpResponseRules();
        super.tearDown();
    }

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

    public void testValidateFieldShouldNotBeEmpty() throws IOException {
        prepareIntent();
        startActivityUnderTest();
        onView(withId(getActionBarTitleId())).check(matches(withText(R.string.sr_ab_title)));

        onView(withId(R.id.report_name_input)).perform(clearText());
        onView(withId(R.id.saveAction)).perform(click());

        onView(withId(R.id.report_name_input)).check(matches(hasErrorText(getActivity().getString(R.string.sr_error_field_is_empty))));
    }

    private void prepareIntent() {
        Intent metaIntent = new Intent();
        metaIntent.putExtra(SaveReportActivity_.RESOURCE_EXTRA, report);
        setActivityIntent(metaIntent);
    }

}
