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

package com.jaspersoft.android.jaspermobile.test.acceptance.library;

import android.support.test.espresso.NoMatchingViewException;
import android.widget.GridView;
import android.widget.ListView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.navigation.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.test.utils.TestResponses;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.apache.http.fake.FakeHttpLayerManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class LibraryPageTest extends ProtoActivityInstrumentation<NavigationActivity_> {
    private static final int DASHBOARD_ITEM_POSITION = 1;

    private static final String GEO_QUERY = "Geo";

    private ResourceLookupsList smallLookUp;
    private ResourceLookup dashboardResource;

    public LibraryPageTest() {
        super(NavigationActivity_.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        smallLookUp = TestResources.get().fromXML(ResourceLookupsList.class, "library_reports_small");
        dashboardResource = smallLookUp.getResourceLookups().get(DASHBOARD_ITEM_POSITION);

        registerTestModule(new HackedTestModule());
        setDefaultCurrentProfile();

        FakeHttpLayerManager.clearHttpResponseRules();
        FakeHttpLayerManager.addHttpResponseRule(
                ApiMatcher.RESOURCES,
                TestResponses.SMALL_LOOKUP);
    }

    @After
    public void tearDown() throws Exception {
        unregisterTestModule();
        super.tearDown();
    }

    @Test
    public void testDashboardItemClick() {
        startActivityUnderTest();
        forcePreview(ViewType.LIST);

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(DASHBOARD_ITEM_POSITION).perform(click());
        onView(withText(dashboardResource.getLabel())).check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testInitialLoadOfGrid() {
        startActivityUnderTest();
        forcePreview(ViewType.GRID);
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));
    }

    @Test
    public void testInitialLoadOfList() {
        startActivityUnderTest();
        forcePreview(ViewType.LIST);
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(ListView.class)));
    }

    @Test
    public void testSwitcher() {
        startActivityUnderTest();
        forcePreview(ViewType.LIST);

        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(ListView.class)));
        rotate();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(ListView.class)));

        onView(withId(R.id.switchLayout)).perform(click());

        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));
        rotate();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));
    }

    @Test
    public void testActionModeAboutIcon() {
        ResourceLookup resource = smallLookUp.getResourceLookups().get(0);
        startActivityUnderTest();

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());

        onView(withId(R.id.showAction)).perform(click());

        onOverflowView(getActivity(), withText(resource.getLabel())).check(matches(isDisplayed()));
        onOverflowView(getActivity(), withId(android.R.id.message)).check(matches(withText(resource.getDescription())));
    }

    @Test
    public void testSearchInRepository() {
        startActivityUnderTest();
        forcePreview(ViewType.LIST);

        try {
            onView(withId(R.id.search)).perform(click());
        } catch (NoMatchingViewException ex) {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
            onView(withText(android.R.string.search_go)).perform(click());
        }
        onView(withId(R.id.search_src_text)).perform(typeText(GEO_QUERY));
        onView(withId(R.id.search_src_text)).perform(pressImeActionButton());

        onView(withText(getActivity().getString(R.string.search_result_format, GEO_QUERY)))
                .check(matches(isDisplayed()));

        onView(withId(R.id.switchLayout)).perform(click());
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    // Yep, it is messy helper. Hopefully we will remove list/grid combination preview in future release.
    private void forcePreview(ViewType viewType) {
        Object list = findViewById(android.R.id.list);
        if (list instanceof ListView && viewType == ViewType.GRID) {
            onView(withId(R.id.switchLayout)).perform(click());
        }
        if (list instanceof GridView && viewType == ViewType.LIST) {
            onView(withId(R.id.switchLayout)).perform(click());
        }
    }

}
