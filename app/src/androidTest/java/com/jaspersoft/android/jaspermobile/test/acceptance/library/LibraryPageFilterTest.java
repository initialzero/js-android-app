/*
* Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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

import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.MockedSpiceManager;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.util.JsXmlSpiceServiceWrapper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.octo.android.robospice.SpiceManager;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isChecked;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasTotalCount;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.withNotDecorView;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class LibraryPageFilterTest extends ProtoActivityInstrumentation<LibraryActivity_> {
    @Mock
    JsServerProfile mockServerProfile;
    @Mock
    DatabaseProvider mockDbProvider;
    @Mock
    JsRestClient mockRestClient;
    @Mock
    SpiceManager mockSpiceService;
    @Mock
    JsXmlSpiceServiceWrapper mockJsXmlSpiceServiceWrapper;

    final MockedSpiceManager mMockedSpiceManager = new MockedSpiceManager(JsXmlSpiceService.class);
    private ResourceLookupsList allLookUp;
    private ResourceLookupsList onlyDashboardLookUp;
    private ResourceLookupsList onlyReportLookUp;

    public LibraryPageFilterTest() {
        super(LibraryActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        allLookUp = TestResources.get().fromXML(ResourceLookupsList.class, "library_reports_small");
        onlyDashboardLookUp = TestResources.get().fromXML(ResourceLookupsList.class, "only_dashboard");
        onlyReportLookUp = TestResources.get().fromXML(ResourceLookupsList.class, "only_report");

        when(mockRestClient.getServerProfile()).thenReturn(mockServerProfile);
        when(mockServerProfile.getUsernameWithOrgId()).thenReturn(USERNAME);
        when(mockServerProfile.getPassword()).thenReturn(PASSWORD);
        when(mockJsXmlSpiceServiceWrapper.getSpiceManager()).thenReturn(mMockedSpiceManager);
        registerTestModule(new TestModule());
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        super.tearDown();
    }

    public void testLibraryFilterDialog() {
        mMockedSpiceManager.setResponseForCacheRequest(onlyDashboardLookUp);
        startActivityUnderTest();

        clickOnDialogText(android.R.string.cancel);
        clickOnDialogText(android.R.string.ok);
    }

    public void testDashboardAndAllFilterOption() throws InterruptedException {
        mMockedSpiceManager.setResponseForCacheRequest(onlyDashboardLookUp);
        startActivityUnderTest();

        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_dashboards)).perform(click());
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(onlyDashboardLookUp.getResourceLookups().size()));

        mMockedSpiceManager.setResponseForCacheRequest(allLookUp);
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_all)).perform(click());
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(allLookUp.getResourceLookups().size()));
    }

    public void testReportFilterOption() {
        mMockedSpiceManager.setResponseForCacheRequest(onlyReportLookUp);
        startActivityUnderTest();

        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_reports)).perform(click());
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(onlyReportLookUp.getResourceLookups().size()));
    }

    public void testFilteringIsPersistent() {
        mMockedSpiceManager.setResponseForCacheRequest(onlyReportLookUp);
        startActivityUnderTest();
        rotateToPortrait();

        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_reports)).perform(click());
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());

        mMockedSpiceManager.setResponseForCacheRequest(onlyDashboardLookUp);
        rotate();
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_reports)).check(matches(isChecked()));
        onOverflowView(getActivity(), withText(R.string.s_fd_option_dashboards)).perform(click());
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());

        rotate();
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_dashboards)).check(matches(isChecked()));
    }

    private void clickOnDialogText(int resId) {
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_filter_by))
                .check(matches(isDisplayed()));
        onOverflowView(getActivity(), withText(resId))
                .perform(click());

        withNotDecorView(
                is(not(getActivity().getWindow().getDecorView()))
        ).matches(matches(not(isDisplayed())));
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

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).toInstance(mockRestClient);
            bind(JsXmlSpiceServiceWrapper.class).toInstance(mockJsXmlSpiceServiceWrapper);
        }
    }
}
