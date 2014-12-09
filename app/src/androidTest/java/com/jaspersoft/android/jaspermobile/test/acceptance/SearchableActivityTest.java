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

package com.jaspersoft.android.jaspermobile.test.acceptance;

import android.content.Intent;
import android.os.Bundle;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.SearchableActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.test.utils.TestResponses;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.apache.http.fake.FakeHttpLayerManager;
import org.apache.http.fake.RequestMatcher;
import org.apache.http.hacked.GetUriRegexMatcher;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.LongListMatchers.withAdaptedData;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.LongListMatchers.withItemContent;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class SearchableActivityTest extends ProtoActivityInstrumentation<SearchableActivity_> {
    private static final String SEARCH_QUERY = "Reports";

    public SearchableActivityTest() {
        super(SearchableActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registerTestModule(new HackedTestModule());
        setDefaultCurrentProfile();
        configureSearchIntent();
        FakeHttpLayerManager.clearHttpResponseRules();
    }

    @Override
    protected void tearDown() throws Exception {
        getActivity().finish();
        unregisterTestModule();
        super.tearDown();
    }

    public void testDashboardClick() {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.REPORTS_QUERY,
                TestResponses.ONLY_DASHBOARD);
        startActivityUnderTest();

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());
        pressBack();
    }

    public void testFolderClick() {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.REPORTS_QUERY,
                TestResponses.ONLY_FOLDER);
        RequestMatcher uriRegexMatcher =
                new GetUriRegexMatcher(".*(folderUri=/organizations/org_template).*");
        FakeHttpLayerManager.addHttpResponseRule(
                uriRegexMatcher,
                TestResponses.ROOT_REPOSITORIES);
        uriRegexMatcher =
                new GetUriRegexMatcher(".*(folderUri=/public).*");
        FakeHttpLayerManager.addHttpResponseRule(
                uriRegexMatcher,
                TestResponses.get().noContent());

        startActivityUnderTest();

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        ResourceLookupsList levelRepositories = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.ONLY_FOLDER);
        String firstLevelRepoLabel = levelRepositories.getResourceLookups().get(0).getLabel();
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(firstLevelRepoLabel)))));

        // Bug related: To check whether we have only one switcher. Otherwise it will rise 'matches multiple views in the hierarchy.'
        onView(withId(R.id.switchLayout)).check(matches(isDisplayed()));

        onView(withText("Public")).perform(click());

        // Bug related: Check whether empty test displays correct message.
        onView(withId(android.R.id.empty)).check(matches(withText(R.string.r_browser_nothing_to_display)));
    }

    public void testSearchResultsPersistedOnRotation() {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.REPORTS_QUERY,
                TestResponses.ONLY_FOLDER);
        ResourceLookupsList levelRepositories = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.ONLY_FOLDER);
        String firstLevelRepoLabel = levelRepositories.getResourceLookups().get(0).getLabel();

        startActivityUnderTest();

        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(firstLevelRepoLabel)))));
        rotate();
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(firstLevelRepoLabel)))));
    }

    public void testSearchResultsWithNoResults() {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.REPORTS_QUERY,
                TestResponses.get().noContent());
        startActivityUnderTest();

        onView(withId(android.R.id.empty)).check(matches(withText(R.string.r_search_nothing_to_display)));
        rotate();
        onView(withId(android.R.id.empty)).check(matches(withText(R.string.r_search_nothing_to_display)));
    }

    private void configureSearchIntent() {
        Intent launchIntent = new Intent();
        launchIntent.setAction(Intent.ACTION_SEARCH);
        Bundle extras = new Bundle();
        extras.putString(SearchableActivity_.QUERY_EXTRA, SEARCH_QUERY);
        launchIntent.putExtras(extras);
        setActivityIntent(launchIntent);
    }

}
