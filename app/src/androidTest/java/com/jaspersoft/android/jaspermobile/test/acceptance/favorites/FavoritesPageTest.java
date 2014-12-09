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

package com.jaspersoft.android.jaspermobile.test.acceptance.favorites;

import android.app.Application;
import android.content.Intent;
import android.database.Cursor;

import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.FavoritesActivity_;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.test.utils.TestResponses;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper_;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.apache.http.fake.FakeHttpLayerManager;
import org.hamcrest.Matchers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.longClick;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.pressImeActionButton;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.deleteAllFavorites;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasTotalCount;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class FavoritesPageTest extends ProtoActivityInstrumentation<FavoritesActivity_> {

    private Application mApplication;
    private FavoritesHelper_ favoritesHelper;

    public FavoritesPageTest() {
        super(FavoritesActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mApplication = (Application) this.getInstrumentation()
                .getTargetContext().getApplicationContext();
        registerTestModule(new HackedTestModule());
        setDefaultCurrentProfile();

        favoritesHelper = FavoritesHelper_.getInstance_(mApplication);
        deleteAllFavorites(mApplication.getContentResolver());
        FakeHttpLayerManager.clearHttpResponseRules();
    }

    @Override
    protected void tearDown() throws Exception {
        deleteAllFavorites(mApplication.getContentResolver());
        unregisterTestModule();
        super.tearDown();
    }

    public void testActionModeAboutIcon() {
        ResourceLookupsList onlyReport = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.ONLY_REPORT);
        ResourceLookup resource = onlyReport.getResourceLookups().get(0);
        favoritesHelper.addToFavorites(resource);
        startActivityUnderTest();

        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());

        onView(withId(R.id.showAction)).perform(click());
        onOverflowView(getActivity(), withId(R.id.sdl__title)).check(matches(withText(resource.getLabel())));
        onOverflowView(getActivity(), withId(R.id.sdl__message)).check(matches(withText(resource.getDescription())));
    }

    public void testAddDashboardToFavoriteFromContextMenu() throws Throwable {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.ONLY_DASHBOARD);

        deleteAllFavorites(mApplication.getContentResolver());
        startActivityUnderTest();
        startContextMenuInteractionTest();
    }

    public void testAddFolderToFavoriteFromContextMenu() throws Throwable {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.ONLY_FOLDER);

        deleteAllFavorites(mApplication.getContentResolver());
        startActivityUnderTest();
        startContextMenuInteractionTest();
    }

    public void testAddReportToFavoriteFromContextMenu() throws Throwable {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.ONLY_REPORT);

        deleteAllFavorites(mApplication.getContentResolver());
        startActivityUnderTest();
        startContextMenuInteractionTest();
    }

    public void testAddToFavoriteFromDashboardView() {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.ONLY_DASHBOARD);
        startActivityUnderTest();

        // Force only dashboards
        Intent intent = LibraryActivity_.intent(mApplication)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .get();
        getInstrumentation().startActivitySync(intent);
        getInstrumentation().waitForIdleSync();

        // Select dashboard
        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(1).perform(click());

        // Add to favorite
        onView(withId(R.id.favoriteAction)).perform(click());
        pressBack();
        pressBack();

        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        // Remove from favorite
        onView(withId(R.id.favoriteAction)).perform(click());
        pressBack();
        onView(withId(android.R.id.list)).check(hasTotalCount(0));
        onView(withId(android.R.id.empty)).check(matches(allOf(withText(R.string.f_empty_list_msg), isDisplayed())));
    }

    public void testAddToFavoriteFromReportView() {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.ONLY_REPORT);
        startActivityUnderTest();

        // Force only reports
        Intent intent = LibraryActivity_.intent(mApplication)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .get();
        getInstrumentation().startActivitySync(intent);
        getInstrumentation().waitForIdleSync();

        // Select report
        FakeHttpLayerManager.setDefaultHttpResponse(TestResponses.get().noContent());
        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        // Add to favorite
        onView(withId(R.id.favoriteAction)).perform(click());
        pressBack();
        pressBack();

        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        // Remove from favorite
        onView(withId(R.id.favoriteAction)).perform(click());
        pressBack();

        onView(withId(android.R.id.list)).check(hasTotalCount(0));
        onView(withId(android.R.id.empty)).check(matches(allOf(withText(R.string.f_empty_list_msg), isDisplayed())));
    }

    public void testPageShouldPreserveOriginalLabel() {
        ResourceLookupsList onlyFolder = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.ONLY_FOLDER);
        ResourceLookup resourceLookup = onlyFolder.getResourceLookups().get(0);
        favoritesHelper.addToFavorites(resourceLookup);

        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.ONLY_FOLDER);

        startActivityUnderTest();

        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        onView(withId(getActionBarTitleId())).check(matches(withText(resourceLookup.getLabel())));
        pressBack();
        onView(withId(getActionBarTitleId())).check(matches(withText(R.string.f_title)));
    }

    //---------------------------------------------------------------------
    // Test filtering
    //---------------------------------------------------------------------
    public void testFilterOption() throws IOException, InterruptedException {
        ResourceLookupsList allResources = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.ALL_RESOURCES);
        List<ResourceLookup> resourcesList = allResources.getResourceLookups();
        for (ResourceLookup resourceLookup : resourcesList) {
            favoritesHelper.addToFavorites(resourceLookup);
        }

        startActivityUnderTest();

        onView(withId(android.R.id.list)).check(hasTotalCount(resourcesList.size()));

        // Check if reports list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_reports)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(3));

        // Check if dashboards list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_dashboards)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(4));

        // Check if folders list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.f_fd_option_folders)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(5));

        // Check if whole list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_all)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(resourcesList.size()));
    }

    public void testFilteringIsPersistentAfterRotate() throws IOException {
        ResourceLookupsList allResources = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.ALL_RESOURCES);
        List<ResourceLookup> resourcesList = allResources.getResourceLookups();
        for (ResourceLookup resourceLookup : resourcesList) {
            favoritesHelper.addToFavorites(resourceLookup);
        }

        startActivityUnderTest();

        onView(withId(android.R.id.list)).check(hasTotalCount(resourcesList.size()));

        // Check if repository list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_reports)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(3));

        // Check if repository list is correct after rotate
        rotate();
        onView(withId(android.R.id.list)).check(hasTotalCount(3));

        // Check if dashboards list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_dashboards)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(4));

        // Check if dashboards list is correct after rotate
        rotate();
        onView(withId(android.R.id.list)).check(hasTotalCount(4));
    }

    public void testFilteringIsPersistentAfterSwitchViewType() throws IOException {
        ResourceLookupsList allResources = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.ALL_RESOURCES);
        List<ResourceLookup> resourcesList = allResources.getResourceLookups();
        for (ResourceLookup resourceLookup : resourcesList) {
            favoritesHelper.addToFavorites(resourceLookup);
        }

        startActivityUnderTest();

        onView(withId(android.R.id.list)).check(hasTotalCount(resourcesList.size()));

        // Check if repository list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_reports)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(3));

        // Check if repository list is correct after switch layout
        onView(withId(R.id.switchLayout)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(3));

        // Check if dashboards list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_dashboards)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(4));

        // Check if dashboards list is correct after switch layout
        onView(withId(R.id.switchLayout)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(4));
    }

    //---------------------------------------------------------------------
    // Test sorting
    //---------------------------------------------------------------------
    public void testSortOption() {
        ResourceLookupsList allResources = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.ONLY_REPORT);
        List<ResourceLookup> resourcesList = allResources.getResourceLookups();
        for (ResourceLookup resourceLookup : resourcesList) {
            favoritesHelper.addToFavorites(resourceLookup);
        }

        startActivityUnderTest();

        // Check if list by label is correct
        clickSortMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_sort_label)).perform(click());

        Collections.sort(resourcesList, new ResourseLookupComparatorByLabel());

        for (int i = 0; i < resourcesList.size(); i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(resourcesList.get(i).getLabel())));
        }

        // Check if list by date is correct
        clickSortMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_sort_label)).perform(click());

        Collections.sort(resourcesList, new ResourseLookupComparatorByDate());

        for (int i = 0; i < resourcesList.size(); i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(resourcesList.get(i).getLabel())));
        }
    }

    public void testSortingIsPersistentAfterRotate() throws IOException {
        ResourceLookupsList allResources = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.ONLY_REPORT);
        List<ResourceLookup> resourcesList = allResources.getResourceLookups();
        for (ResourceLookup resourceLookup : resourcesList) {
            favoritesHelper.addToFavorites(resourceLookup);
        }

        startActivityUnderTest();

        Collections.sort(resourcesList, new ResourseLookupComparatorByDate());

        // Check if list by date is correct
        clickSortMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_sort_date)).perform(click());

        for (int i = 0; i < resourcesList.size(); i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(resourcesList.get(i).getLabel())));
        }

        rotate();

        // Check if list by date is correct after rotate

        for (int i = 0; i < resourcesList.size(); i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(resourcesList.get(i).getLabel())));
        }
    }

    public void testSortingIsPersistentAfterSwitchViewType() throws IOException {
        ResourceLookupsList allResources = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.ONLY_REPORT);
        List<ResourceLookup> resourcesList = allResources.getResourceLookups();
        for (ResourceLookup resourceLookup : resourcesList) {
            favoritesHelper.addToFavorites(resourceLookup);
        }

        startActivityUnderTest();

        Collections.sort(resourcesList, new ResourseLookupComparatorByDate());

        // Check if list by date is correct
        clickSortMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_sort_date)).perform(click());

        for (int i = 0; i < resourcesList.size(); i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(resourcesList.get(i).getLabel())));
        }

        onView(withId(R.id.switchLayout)).perform(click());

        // Check if list by date is correct after rotate

        for (int i = 0; i < resourcesList.size(); i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(resourcesList.get(i).getLabel())));
        }
    }

    //---------------------------------------------------------------------
    // Test search feature
    //---------------------------------------------------------------------
    public void testSearch() throws IOException {
        ResourceLookupsList allResources = TestResources.get().fromXML(ResourceLookupsList.class, TestResources.ALL_RESOURCES);
        List<ResourceLookup> resourcesList = allResources.getResourceLookups();

        String searchQuery = "Re";
        int expectedSearchCount = 0;
        for (ResourceLookup resourceLookup : resourcesList) {
            favoritesHelper.addToFavorites(resourceLookup);
            if(resourceLookup.getLabel().contains(searchQuery)) {
                expectedSearchCount++;
            }
        }

        startActivityUnderTest();

        onView(withId(R.id.search)).perform(click());
        onView(withId(getSearcFieldId())).perform(typeText(searchQuery));
        onView(withId(getSearcFieldId())).perform(pressImeActionButton());

        // Check if list by date is correct after rotate

        for (int i = 0; i < expectedSearchCount; i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(containsString(searchQuery))));
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void startContextMenuInteractionTest() throws InterruptedException {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.INPUT_CONTROLS,
                TestResponses.get().noContent());
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.REPORT_EXECUTIONS,
                TestResponses.get().noContent());

        Intent intent = LibraryActivity_.intent(mApplication)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .get();
        getInstrumentation().startActivitySync(intent);
        getInstrumentation().waitForIdleSync();

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.favoriteAction)).perform(click());
        pressBack();
        pressBack();

        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());
        pressBack();

        intent = LibraryActivity_.intent(mApplication)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .get();
        getInstrumentation().startActivitySync(intent);
        getInstrumentation().waitForIdleSync();

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.favoriteAction)).perform(click());
        pressBack();
        pressBack();

        Thread.sleep(200);
        onView(withId(android.R.id.list)).check(hasTotalCount(0));
        onView(withId(android.R.id.empty)).check(matches(allOf(withText(R.string.f_empty_list_msg), isDisplayed())));

        intent = LibraryActivity_.intent(mApplication)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .get();
        getInstrumentation().startActivitySync(intent);
        getInstrumentation().waitForIdleSync();

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.favoriteAction)).perform(click());
        pressBack();
        pressBack();

        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.removeFromFavorites)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(0));
        onView(withId(android.R.id.empty)).check(matches(allOf(withText(R.string.f_empty_list_msg), isDisplayed())));
    }

    private void clickFilterMenuItem() {
        try {
            onView(withId(R.id.filter)).perform(click());
        } catch (NoMatchingViewException ex) {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
            try {
                onOverflowView(getCurrentActivity(), withText(R.string.s_ab_filter_by)).perform(click());
            } catch (Throwable throwable) {
                new RuntimeException(throwable);
            }
        }
    }

    private void clickSortMenuItem() {
        try {
            onView(withId(R.id.sort)).perform(click());
        } catch (NoMatchingViewException ex) {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
            try {
                onOverflowView(getCurrentActivity(), withText(R.string.s_ab_sort_by)).perform(click());
            } catch (Throwable throwable) {
                new RuntimeException(throwable);
            }
        }
    }

    //---------------------------------------------------------------------
    // nested classes
    //---------------------------------------------------------------------

    private class ResourseLookupComparatorByLabel implements Comparator<ResourceLookup> {
        public int compare(ResourceLookup resA, ResourceLookup resB) {
            return resA.getLabel().compareToIgnoreCase(resB.getLabel());
        }
    }

    private class ResourseLookupComparatorByDate implements Comparator<ResourceLookup> {
        public int compare(ResourceLookup resA, ResourceLookup resB) {
            return resA.getCreationDate().compareToIgnoreCase(resB.getCreationDate());
        }
    }

}
