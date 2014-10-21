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

import android.app.Application;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;

import com.google.inject.Singleton;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.SearchableActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils;
import com.jaspersoft.android.jaspermobile.test.utils.SmartMockedSpiceManager;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.util.JsSpiceManager;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper_;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.SpiceManager;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.TestResources.get;
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

    @Mock
    JsRestClient mockRestClient;
    @Mock
    SpiceManager mockSpiceService;

    private SmartMockedSpiceManager mMockedSpiceManager;
    private ResourceLookupsList reportsQueryResult;
    private ResourceLookupsList levelRepositories;
    private ResourceLookupsList rootRepositories;
    private ReportExecutionResponse reportExecution;
    private ServerInfo mockServerInfo;

    public SearchableActivityTest() {
        super(SearchableActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        mMockedSpiceManager = SmartMockedSpiceManager.getInstance();
        reportsQueryResult = get().fromXML(ResourceLookupsList.class, "reports_query_result");
        levelRepositories = get().fromXML(ResourceLookupsList.class, "level_repositories");
        rootRepositories = get().fromXML(ResourceLookupsList.class, "root_repositories");
        reportExecution = get().fromXML(ReportExecutionResponse.class, "report_execution_geographic_result");
        mockServerInfo = TestResources.get().fromXML(ServerInfo.class, "server_info");

        registerTestModule(new TestModule());
        ContentResolver cr = getInstrumentation().getTargetContext().getContentResolver();
        DatabaseUtils.deleteAllProfiles(cr);

        Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        ProfileHelper profileHelper = ProfileHelper_.getInstance_(application);
        profileHelper.setCurrentServerProfile(DatabaseUtils.createDefaultProfile(cr));

        configureSearchIntent();
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        super.tearDown();
    }

    public void testReportClick() {
        mMockedSpiceManager.addNetworkResponse(mockServerInfo);
        mMockedSpiceManager.addCachedResponse(reportsQueryResult);

        mMockedSpiceManager.addNetworkResponse(new InputControlsList());
        mMockedSpiceManager.addNetworkResponse(reportExecution);

        mMockedSpiceManager.addNetworkResponse(mockServerInfo);
        mMockedSpiceManager.addCachedResponse(reportsQueryResult);
        startActivityUnderTest();


        onView(withText("Employees")).perform(click());

        pressBack();
    }

    public void testDashboardClick() {
        mMockedSpiceManager.addNetworkResponse(mockServerInfo);
        mMockedSpiceManager.addCachedResponse(reportsQueryResult);
        startActivityUnderTest();

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(2).perform(click());
        pressBack();
    }

    public void testFolderClick() {
        mMockedSpiceManager.addNetworkResponse(mockServerInfo);
        mMockedSpiceManager.addCachedResponse(levelRepositories);
        mMockedSpiceManager.addNetworkResponse(mockServerInfo);
        mMockedSpiceManager.addCachedResponse(rootRepositories);
        mMockedSpiceManager.addNetworkResponse(mockServerInfo);
        mMockedSpiceManager.addCachedResponse(new ResourceLookupsList());
        startActivityUnderTest();

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        String firstLevelRepoLabel = levelRepositories.getResourceLookups().get(0).getLabel();
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(firstLevelRepoLabel)))));

        // Bug related: To check whether we have only one switcher. Otherwise it will rise 'matches multiple views in the hierarchy.'
        onView(withId(R.id.switchLayout)).check(matches(isDisplayed()));

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        // Bug related: Check whether empty test displays correct message.
        onView(withId(android.R.id.empty)).check(matches(withText(R.string.r_browser_nothing_to_display)));
    }

    public void testSearchResultsPersistedOnRotation() {
        mMockedSpiceManager.addNetworkResponse(mockServerInfo);
        mMockedSpiceManager.addCachedResponse(levelRepositories);
        mMockedSpiceManager.addNetworkResponse(mockServerInfo);
        mMockedSpiceManager.addCachedResponse(levelRepositories);
        String firstLevelRepoLabel = levelRepositories.getResourceLookups().get(0).getLabel();

        startActivityUnderTest();

        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(firstLevelRepoLabel)))));
        rotate();
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(firstLevelRepoLabel)))));
    }

    public void testSearchResultsWithNoResults() {
        mMockedSpiceManager.addNetworkResponse(mockServerInfo);
        mMockedSpiceManager.addCachedResponse(new ResourceLookupsList());
        mMockedSpiceManager.addNetworkResponse(mockServerInfo);
        mMockedSpiceManager.addCachedResponse(new ResourceLookupsList());
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

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).in(Singleton.class);
            bind(JsSpiceManager.class).toInstance(mMockedSpiceManager);
        }
    }
}
