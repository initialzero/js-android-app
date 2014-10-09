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

import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
import com.google.inject.Singleton;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOrder;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.RequestExecutionAssertion;
import com.jaspersoft.android.jaspermobile.test.utils.SmartMockedSpiceManager;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.util.JsSpiceManager;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.mockito.ArgumentCaptor;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.assertThat;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class LibraryPageSortTest extends ProtoActivityInstrumentation<LibraryActivity_> {

    private final SmartMockedSpiceManager mMockedSpiceManager = spy(SmartMockedSpiceManager.getInstance());
    private ResourceLookupsList allLookUp = TestResources.get().fromXML(ResourceLookupsList.class, "library_reports_small");

    public LibraryPageSortTest() {
        super(LibraryActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registerTestModule(new TestModule());
        setDefaultCurrentProfile();
    }

    public void testSortByDate() {
        verifySortBy(SortOrder.CREATION_DATE, R.string.s_fd_sort_date);
    }

     public void testSortByLabel() {
        verifySortBy(SortOrder.LABEL, R.string.s_fd_sort_label);
    }

    private void verifySortBy(final SortOrder sortOrder, int menuLabelRes) {
        // We don`t need to listen initial load of page
        mMockedSpiceManager.addCachedResponse(allLookUp);
        // We will assert Search query whether it was setup correctly
        mMockedSpiceManager.addCachedResponse(new RequestExecutionAssertion(allLookUp) {
            @Override
            public <T> void assertExecution(SpiceRequest<T> request, RequestListener<T> requestListener) {
                GetResourceLookupsRequest resourceLookupsRequest = (GetResourceLookupsRequest) request;
                ResourceLookupSearchCriteria searchCriteria = resourceLookupsRequest.getSearchCriteria();
                assertThat(searchCriteria.getSortBy(), is(sortOrder.getValue()));
            }
        });
        startActivityUnderTest();

        clickSortMenuItem();
        onOverflowView(getActivity(), withText(menuLabelRes)).perform(click());

        // We should verify that network call has been started.
        // Otherwise test considered to be poorly designed.
        ArgumentCaptor<SpiceRequest> argument1 = ArgumentCaptor.forClass(SpiceRequest.class);
        ArgumentCaptor<Object> argument2 = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Long> argument3 = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<RequestListener> argument4 = ArgumentCaptor.forClass(RequestListener.class);
        verify(mMockedSpiceManager, atLeastOnce()).execute(argument1.capture(), argument2.capture(),
                argument3.capture(), argument4.capture());
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

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).in(Singleton.class);
            bind(JsSpiceManager.class).toInstance(mMockedSpiceManager);
        }
    }


}
