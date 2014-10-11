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

import android.widget.GridView;
import android.widget.ListView;

import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
import com.google.inject.Singleton;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.RepositoryPref_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.SmartMockedSpiceManager;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.util.JsSpiceManager;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.longClick;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.pressImeActionButton;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class LibraryPageTest extends ProtoActivityInstrumentation<LibraryActivity_> {
    private static final int REPORT_ITEM_POSITION = 0;
    private static final int DASHBOARD_ITEM_POSITION = 1;

    private static final String GEO_QUERY = "Geo";

    private SmartMockedSpiceManager mMockedSpiceManager;
    private RepositoryPref_ repositoryPref;
    private ResourceLookupsList smallLookUp;
    private ResourceLookup reportResource;
    private ResourceLookup dashboardResource;
    private InputControlsList emptyInputControlsList;
    private InputControlsList fullInputControlsList;

    public LibraryPageTest() {
        super(LibraryActivity_.class);
    }

    protected ReportExecutionResponse reportExecution;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        repositoryPref = new RepositoryPref_(getInstrumentation().getContext());
        smallLookUp = TestResources.get().fromXML(ResourceLookupsList.class, "library_reports_small");
        reportExecution = TestResources.get().fromXML(ReportExecutionResponse.class, "report_execution_geographic_result");

        reportResource = smallLookUp.getResourceLookups().get(REPORT_ITEM_POSITION);
        dashboardResource = smallLookUp.getResourceLookups().get(DASHBOARD_ITEM_POSITION);

        emptyInputControlsList = new InputControlsList();
        emptyInputControlsList.setInputControls(new ArrayList<InputControl>());

        fullInputControlsList = TestResources.get().fromXML(InputControlsList.class, "input_contols_list");

        mMockedSpiceManager = SmartMockedSpiceManager.getInstance();
        mMockedSpiceManager.addCachedResponse(smallLookUp);

        registerTestModule(new TestModule());
        setDefaultCurrentProfile();

    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        repositoryPref = null;
        super.tearDown();
    }

    public void testDashboardItemClick() {
        forcePreview(ViewType.LIST);
        startActivityUnderTest();

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(DASHBOARD_ITEM_POSITION).perform(click());
        onView(withText(dashboardResource.getLabel())).check(matches(isDisplayed()));
        pressBack();
    }

    public void testInitialLoadOfGrid() {
        forcePreview(ViewType.GRID);
        startActivityUnderTest();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));
    }

    public void testInitialLoadOfList() {
        forcePreview(ViewType.LIST);
        startActivityUnderTest();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(ListView.class)));
    }

    public void testReportWithICItemClicked() {
        mMockedSpiceManager.addNetworkResponse(fullInputControlsList);
        clickOnReportItem();
    }

    public void testReportWithoutICItemClicked() {
        mMockedSpiceManager.addNetworkResponse(emptyInputControlsList);
        mMockedSpiceManager.addNetworkResponse(reportExecution);
        clickOnReportItem();
    }

    public void testSwitcher() {
        mMockedSpiceManager.addCachedResponse(smallLookUp);
        mMockedSpiceManager.addCachedResponse(smallLookUp);
        mMockedSpiceManager.addCachedResponse(smallLookUp);
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

    public void testHomeAsUp() {
        startActivityUnderTest();
        onView(withId(android.R.id.home)).perform(click());
        onView(withText(R.string.app_label)).check(matches(isDisplayed()));
    }

    private void clickOnReportItem() {
        forcePreview(ViewType.LIST);
        startActivityUnderTest();
        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(REPORT_ITEM_POSITION).perform(click());
// TODO: needs further ivestigation of reasons it failing
//        onViewDialogText(getActivity(), R.string.r_pd_running_report_msg)
//                .check(matches(isDisplayed()));

        onView(withText(reportResource.getLabel())).check(matches(isDisplayed()));
        pressBack();
    }

    public void testActionModeAboutIcon() {
        ResourceLookup resource = smallLookUp.getResourceLookups().get(0);
        mMockedSpiceManager.addCachedResponse(smallLookUp);
        startActivityUnderTest();

        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());

        onView(withId(R.id.showAction)).perform(click());

        onOverflowView(getActivity(), withId(R.id.sdl__title)).check(matches(withText(resource.getLabel())));
        onOverflowView(getActivity(), withId(R.id.sdl__message)).check(matches(withText(resource.getDescription())));
    }

    public void testSearchInRepository() {
        mMockedSpiceManager.addCachedResponse(smallLookUp);
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
    }

    private void forcePreview(ViewType viewType) {
        repositoryPref.viewType().put(viewType.toString());
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).in(Singleton.class);
            bind(JsSpiceManager.class).toInstance(mMockedSpiceManager);
        }
    }
}
