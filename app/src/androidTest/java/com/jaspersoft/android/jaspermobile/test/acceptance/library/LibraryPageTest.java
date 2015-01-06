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

package com.jaspersoft.android.jaspermobile.test.acceptance.library;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.runner.AndroidJUnit4;
import android.widget.GridView;
import android.widget.ListView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ControllerPref;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
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
import org.junit.runner.RunWith;

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
@RunWith(AndroidJUnit4.class)
public class LibraryPageTest extends ProtoActivityInstrumentation<LibraryActivity_> {
    private static final int DASHBOARD_ITEM_POSITION = 1;

    private static final String GEO_QUERY = "Geo";
    private static final String CLASS_NAME = "activities.repository.LibraryActivity_";

    private ResourceLookupsList smallLookUp;
    private ResourceLookup dashboardResource;

    public LibraryPageTest() {
        super(LibraryActivity_.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

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
        forcePreview(ViewType.LIST);
        startActivityUnderTest();

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(DASHBOARD_ITEM_POSITION).perform(click());
        onView(withText(dashboardResource.getLabel())).check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testInitialLoadOfGrid() {
        forcePreview(ViewType.GRID);
        startActivityUnderTest();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));
    }

    @Test
    public void testInitialLoadOfList() {
        forcePreview(ViewType.LIST);
        startActivityUnderTest();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(ListView.class)));
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
    public void testHomeAsUp() {
        startActivityUnderTest();
        onView(withId(android.R.id.home)).perform(click());
        onView(withText(R.string.app_label)).check(matches(isDisplayed()));
    }

    @Test
    public void testActionModeAboutIcon() {
        ResourceLookup resource = smallLookUp.getResourceLookups().get(0);
        startActivityUnderTest();

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());

        onView(withId(R.id.showAction)).perform(click());

        onOverflowView(getActivity(), withId(R.id.sdl__title)).check(matches(withText(resource.getLabel())));
        onOverflowView(getActivity(), withId(R.id.sdl__message)).check(matches(withText(resource.getDescription())));
    }

    @Test
    public void testSearchInRepository() {
        forcePreview(ViewType.LIST);
        startActivityUnderTest();

        try {
            onView(withId(R.id.search)).perform(click());
        } catch (NoMatchingViewException ex) {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
            onOverflowView(getActivity(), withText(android.R.string.search_go)).perform(click());
        }
        onView(withId(getSearcFieldId())).perform(typeText(GEO_QUERY));
        onView(withId(getSearcFieldId())).perform(pressImeActionButton());

        onView(withText(getActivity().getString(R.string.search_result_format, GEO_QUERY)))
                .check(matches(isDisplayed()));

        onView(withId(R.id.switchLayout)).perform(click());
        pressBack();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void forcePreview(ViewType viewType) {
        ControllerPref controllerPref = new ControllerPref(getInstrumentation().getContext(), CLASS_NAME);
        controllerPref.viewType().put(viewType.toString());
    }

}
