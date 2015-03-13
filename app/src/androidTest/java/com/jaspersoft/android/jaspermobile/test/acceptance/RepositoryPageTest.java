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

import android.support.test.espresso.NoMatchingViewException;
import android.widget.GridView;
import android.widget.ListView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.navigation.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ControllerPref;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.test.utils.TestResponses;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.report.FolderDataResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.apache.http.fake.FakeHttpLayerManager;
import org.apache.http.hacked.HackedJsRestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.LongListMatchers.withAdaptedData;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.LongListMatchers.withItemContent;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class RepositoryPageTest extends ProtoActivityInstrumentation<NavigationActivity_> {

    private static final String REPORTS_QUERY = "Reports";
    private static final String CLASS_NAME = "activities.DrawerActivity_";

    public RepositoryPageTest() {
        super(NavigationActivity_.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setActivityIntent(NavigationActivity_.intent(getApplication())
                .currentSelection(R.id.vg_repository).get());

        registerTestModule(new TestModule());
        setDefaultCurrentProfile();

        FakeHttpLayerManager.addHttpResponseRule(ApiMatcher.GET_ROOT_FOLDER, TestResponses.ROOT_FOLDER);
    }

    @After
    public void tearDown() throws Exception {
        unregisterTestModule();
        getActivity().finish();
        super.tearDown();
    }

    @Test
    public void testInitialLoadOfGrid() {
        forcePreview(ViewType.GRID);
        startActivityUnderTest();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));
    }

    @Test
    public void testSwitcher() {
        forcePreview(ViewType.LIST);
        startActivityUnderTest();

        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(ListView.class)));
        rotate();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(ListView.class)));

        onView(withId(R.id.switchLayout)).perform(click());

        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));
        rotate();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));
    }

    @Test
    public void testInitialLoadOfList() {
        forcePreview(ViewType.LIST);
        startActivityUnderTest();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(ListView.class)));
    }

    @Test
    public void testRepoClickCase() throws InterruptedException {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.ROOT_FOLDER_CONTENT,
                TestResponses.ROOT_REPOSITORIES);
        forcePreview(ViewType.LIST);
        startActivityUnderTest();

        FolderDataResponse rootFolder = TestResources.get().fromXML(FolderDataResponse.class, TestResources.ROOT_FOLDER);
        String firstRootRepoLabel = rootFolder.getLabel();

        // Force to wait so that test won`t be fluky
        Thread.sleep(500);

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());
        onView(withId(getActionBarTitleId())).check(matches(withText(firstRootRepoLabel)));
        pressBack();
        onView(withId(getActionBarTitleId())).check(matches(withText(R.string.h_repository_label)));
    }

    @Test
    public void testRepoBackstackPersistance() throws InterruptedException {
        FolderDataResponse rootFolder = TestResources.get().fromXML(FolderDataResponse.class, TestResources.ROOT_FOLDER);
        String rootLevelRepoLabel = rootFolder.getLabel();
        ResourceLookupsList levelRepositories = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.ONLY_FOLDER);
        String firstLevelRepoLabel = levelRepositories.getResourceLookups().get(0).getLabel();

        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.ROOT_FOLDER_CONTENT,
                TestResponses.ONLY_FOLDER);

        forcePreview(ViewType.LIST);
        startActivityUnderTest();

        // Force this test not to be fluky
        Thread.sleep(200);

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(firstLevelRepoLabel)))));
        rotate();
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(firstLevelRepoLabel)))));
        pressBack();
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(rootLevelRepoLabel)))));
        rotate();
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(rootLevelRepoLabel)))));
    }

    @Test
    public void testSearchInRepository() {
        forcePreview(ViewType.LIST);
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.REPORTS_QUERY,
                TestResponses.ONLY_FOLDER);
        startActivityUnderTest();

        try {
            onView(withId(R.id.search)).perform(click());
        } catch (NoMatchingViewException ex) {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
            onView(withText(android.R.string.search_go)).perform(click());
        }
        onView(withId(getSearcFieldId())).perform(typeText(REPORTS_QUERY));
        onView(withId(getSearcFieldId())).perform(pressImeActionButton());

        onView(withText(getActivity().getString(R.string.search_result_format, REPORTS_QUERY)))
                .check(matches(isDisplayed()));
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void forcePreview(ViewType viewType) {
        ControllerPref controllerPref = new ControllerPref(getInstrumentation().getContext(), CLASS_NAME);
        controllerPref.viewType().put(viewType.toString());
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    public class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).toInstance(HackedJsRestClient.get());
        }
    }
}
