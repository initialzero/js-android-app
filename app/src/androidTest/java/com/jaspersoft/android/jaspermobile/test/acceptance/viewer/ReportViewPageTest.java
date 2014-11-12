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

package com.jaspersoft.android.jaspermobile.test.acceptance.viewer;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;

import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.emerald2.ReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.test.utils.TestResponses;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper_;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.apache.http.fake.FakeHttpLayerManager;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.assertThat;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.deleteAllFavorites;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.getAllFavorites;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.firstChildOf;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ReportViewPageTest extends ProtoActivityInstrumentation<ReportHtmlViewerActivity_> {
    protected static final String RESOURCE_URI = "/Reports/2_Sales_Mix_by_Demographic_Report";
    protected static final String RESOURCE_LABEL = "02. Sales Mix by Demographic Report";

    private ResourceLookup mResource;
    private FavoritesHelper_ favoritesHelper;

    public ReportViewPageTest() {
        super(ReportHtmlViewerActivity_.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Application application = (Application) this.getInstrumentation()
                .getTargetContext().getApplicationContext();
        registerTestModule(new HackedTestModule());
        setDefaultCurrentProfile();

        favoritesHelper = FavoritesHelper_.getInstance_(application);

        ResourceLookupsList resourceLookupsList = TestResources.get().fromXML(ResourceLookupsList.class, "only_report");
        mResource = resourceLookupsList.getResourceLookups().get(0);
        mResource.setLabel(RESOURCE_LABEL);
        mResource.setUri(RESOURCE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        super.tearDown();
    }

    public void testAboutAction() {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.INPUT_CONTROLS,
                TestResponses.get().noContent());
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.REPORT_EXECUTIONS,
                TestResponses.REPORT_EXECUTION);

        createReportIntent();
        startActivityUnderTest();

        clickAboutMenuItem();

        onOverflowView(getActivity(), withId(R.id.sdl__title)).check(matches(withText(mResource.getLabel())));
        onOverflowView(getActivity(), withId(R.id.sdl__message)).check(matches(withText(mResource.getDescription())));
    }

    public void testEmptyReport() {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.INPUT_CONTROLS,
                TestResponses.INPUT_CONTROLS);
        createReportIntent();
        startActivityUnderTest();

        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.REPORTS,
                TestResponses.get().xml("empty_inputcontrol_state"));
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.REPORT_EXECUTIONS,
                TestResponses.get().xml("empty_report_execution"));
        onView(withId(R.id.saveAction)).perform(click());

        onOverflowView(getActivity(), withId(R.id.sdl__title)).check(matches(withText(R.string.warning_msg)));
        onOverflowView(getActivity(), withId(R.id.sdl__message)).check(matches(withText(R.string.rv_error_empty_report)));
    }

    public void testRemoveFromFavorites() {
        ContentResolver contentResolver = getInstrumentation().getContext().getContentResolver();
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.INPUT_CONTROLS,
                TestResponses.get().noContent());
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.REPORT_EXECUTIONS,
                TestResponses.REPORT_EXECUTION);

        deleteAllFavorites(contentResolver);
        favoritesHelper.addToFavorites(mResource);

        createReportIntent();
        startActivityUnderTest();

        onView(withId(R.id.favoriteAction)).perform(click());

        Cursor cursor = getAllFavorites(contentResolver);
        assertThat(cursor.getCount(), is(0));
        cursor.close();
    }

    public void testReportWithNoInputControls() {
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.INPUT_CONTROLS,
                TestResponses.get().noContent());
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.REPORT_EXECUTIONS,
                TestResponses.REPORT_EXECUTION);
        createReportIntent();
        startActivityUnderTest();

        onView(withText(RESOURCE_LABEL)).check(matches(isDisplayed()));
        onView(not(withId(R.id.showFilters)));

        rotate();
        onView(firstChildOf(withId(R.id.webViewPlaceholder))).check(matches(isDisplayed()));
    }

    public void testToggleFavoritesState() {
        ContentResolver contentResolver = getInstrumentation().getContext().getContentResolver();
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.INPUT_CONTROLS,
                TestResponses.get().noContent());
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.REPORT_EXECUTIONS,
                TestResponses.REPORT_EXECUTION);

        deleteAllFavorites(contentResolver);
        createReportIntent();
        startActivityUnderTest();

        for (int i = 0; i < 2; i++) {
            onView(withId(R.id.favoriteAction)).perform(click());
            Cursor cursor = getAllFavorites(contentResolver);
            assertThat(cursor.getCount(), is(not(0)));
            cursor.close();

            onView(withId(R.id.favoriteAction)).perform(click());
            cursor = getAllFavorites(contentResolver);
            assertThat(cursor.getCount(), is(0));
            cursor.close();
        }
    }

    private void clickAboutMenuItem() {
        try {
            onView(withId(R.id.aboutAction)).perform(click());
        } catch (NoMatchingViewException ex) {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
            try {
                onOverflowView(getCurrentActivity(), withText(R.string.r_cm_view_details)).perform(click());
            } catch (Throwable throwable) {
                new RuntimeException(throwable);
            }
        }
    }

    protected void createReportIntent() {
        Intent htmlViewer = new Intent();
        htmlViewer.putExtra(ReportHtmlViewerActivity_.RESOURCE_EXTRA, mResource);
        setActivityIntent(htmlViewer);
    }

}
