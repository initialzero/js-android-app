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
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.FilterOptions;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.util.JsXmlSpiceServiceWrapper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

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
    private static final int LIMIT = 40;

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

        registerTestModule(new TestModule());
        when(mockRestClient.getServerProfile()).thenReturn(mockServerProfile);
        when(mockServerProfile.getUsernameWithOrgId()).thenReturn(USERNAME);
        when(mockServerProfile.getPassword()).thenReturn(PASSWORD);
        when(mockJsXmlSpiceServiceWrapper.getSpiceManager()).thenReturn(mMockedSpiceManager);
    }

    public void testLibraryFilterDialog() {
        startActivityUnderTest();
        clickOnDialogText(android.R.string.cancel);
        clickOnDialogText(android.R.string.ok);
    }

    public void testDashboardAndAllFilterOption() throws InterruptedException {
        startActivityUnderTest();

        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_dashboards)).perform(click());
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(onlyDashboardLookUp.getResourceLookups().size()));

        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_all)).perform(click());
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(allLookUp.getResourceLookups().size()));
    }

    public void testReportFilterOption() {
        startActivityUnderTest();

        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_reports)).perform(click());
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(onlyReportLookUp.getResourceLookups().size()));
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

    public void testFilteringIsPersistent() {
        startActivityUnderTest();
        rotateToPortrait();

        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_reports)).perform(click());
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());

        rotate();
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_reports)).check(matches(isChecked()));
        onOverflowView(getActivity(), withText(R.string.s_fd_option_dashboards)).perform(click());
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());

        rotate();
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_dashboards)).check(matches(isChecked()));
    }

    private void clickFilterMenuItem() {
        try {
            onView(withId(R.id.filter)).perform(click());
        } catch (NoMatchingViewException ex) {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
            onOverflowView(getActivity(), withText(R.string.s_ab_filter_by)).perform(click());
        }
    }

    @Override
    public String getPageName() {
        return "library";
    }

    public class MockedSpiceManager extends SpiceManager {
        public MockedSpiceManager(Class<? extends SpiceService> spiceServiceClass) {
            super(spiceServiceClass);
        }

        public <T> void execute(final SpiceRequest<T> request, final Object requestCacheKey,
                                final long cacheExpiryDuration, final RequestListener<T> requestListener) {
            if (request instanceof GetResourceLookupsRequest) {
                GetResourceLookupsRequest lookupsRequest = (GetResourceLookupsRequest) request;
                List<String> types = lookupsRequest.getSearchCriteria().getTypes();
                if (types.equals(FilterOptions.ALL_TYPES) ) {
                    requestListener.onRequestSuccess((T) allLookUp);
                }
                if (types.equals(FilterOptions.ONLY_DASHBOARD)) {
                    requestListener.onRequestSuccess((T) onlyDashboardLookUp);
                }
                if (types.equals(FilterOptions.ONLY_REPORT)) {
                    requestListener.onRequestSuccess((T) onlyReportLookUp);
                }
            }
        }
    }

    public class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(JsRestClient.class).toInstance(mockRestClient);
            bind(DatabaseProvider.class).toInstance(mockDbProvider);
            bind(JsXmlSpiceServiceWrapper.class).toInstance(mockJsXmlSpiceServiceWrapper);
            bindConstant().annotatedWith(Names.named("LIMIT")).to(LIMIT);
            bindConstant().annotatedWith(Names.named("THRESHOLD")).to(5);
        }
    }
}
