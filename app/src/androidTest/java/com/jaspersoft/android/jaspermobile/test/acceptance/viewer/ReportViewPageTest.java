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

package com.jaspersoft.android.jaspermobile.test.acceptance.viewer;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.support.test.espresso.NoMatchingViewException;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.test.utils.TestResponses;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper_;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.apache.http.fake.FakeHttpLayerManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.deleteAllFavorites;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.getAllFavorites;
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

    @Before
    public void setUp() throws Exception {
        super.setUp();
        registerTestModule(new HackedTestModule());
        setDefaultCurrentProfile();

        favoritesHelper = FavoritesHelper_.getInstance_(getApplication());

        ResourceLookupsList resourceLookupsList = TestResources.get().fromXML(ResourceLookupsList.class, "only_report");
        mResource = resourceLookupsList.getResourceLookups().get(0);
        mResource.setLabel(RESOURCE_LABEL);
        mResource.setUri(RESOURCE_URI);

        FakeHttpLayerManager.setDefaultHttpResponse(TestResponses.get().noContent());
    }

    @After
    public void tearDown() throws Exception {
        unregisterTestModule();
        super.tearDown();
    }

    @Test
    public void testAboutAction() {
        createReportIntent();
        startActivityUnderTest();

        clickAboutMenuItem();

        onOverflowView(getActivity(), withText(mResource.getLabel())).check(matches(isDisplayed()));
        onOverflowView(getActivity(), withId(android.R.id.message)).check(matches(withText(mResource.getDescription())));
    }

    @Test
    public void testRemoveFromFavorites() {
        ContentResolver contentResolver = getInstrumentation().getContext().getContentResolver();
        deleteAllFavorites(contentResolver);
        favoritesHelper.addToFavorites(mResource);

        createReportIntent();
        startActivityUnderTest();

        onView(withId(R.id.favoriteAction)).perform(click());

        Cursor cursor = getAllFavorites(contentResolver);
        assertThat(cursor.getCount(), is(0));
        cursor.close();
    }


    @Test
    public void testToggleFavoritesState() {
        ContentResolver contentResolver = getInstrumentation().getContext().getContentResolver();
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

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void clickAboutMenuItem() {
        try {
            onView(withId(R.id.aboutAction)).perform(click());
        } catch (NoMatchingViewException ex) {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
            onView(withText(R.string.r_cm_view_details)).perform(click());
        }
    }

    protected void createReportIntent() {
        Intent htmlViewer = new Intent();
        htmlViewer.putExtra(ReportHtmlViewerActivity_.RESOURCE_EXTRA, mResource);
        setActivityIntent(htmlViewer);
    }

}
