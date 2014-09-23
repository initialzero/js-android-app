/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.test.acceptance.library;

import com.google.common.collect.Queues;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.SmartMockedSpiceManager;
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
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.refreshing;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.swipeDown;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.LongListMatchers.withAdaptedData;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.LongListMatchers.withItemContent;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class LibraryPageRefreshingTest extends ProtoActivityInstrumentation<LibraryActivity_> {

    @Mock
    JsServerProfile mockServerProfile;
    @Mock
    JsRestClient mockRestClient;
    @Mock
    SpiceManager mockSpiceService;
    @Mock
    JsXmlSpiceServiceWrapper mockJsXmlSpiceServiceWrapper;

    private SmartMockedSpiceManager mMockedSpiceManager;
    private ResourceLookupsList smallLookUp;
    private ResourceLookupsList bigLookUp;
    private ResourceLookupsList emptyLookUp;

    public LibraryPageRefreshingTest() {
        super(LibraryActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        smallLookUp = TestResources.get().fromXML(ResourceLookupsList.class, "library_reports_small");
        bigLookUp = TestResources.get().fromXML(ResourceLookupsList.class, "library_0_40");
        emptyLookUp = new ResourceLookupsList();

        mMockedSpiceManager = SmartMockedSpiceManager.createMockedManager(JsXmlSpiceService.class);

        registerTestModule(new TestModule());
        when(mockRestClient.getServerProfile()).thenReturn(mockServerProfile);
        when(mockServerProfile.getUsernameWithOrgId()).thenReturn(USERNAME);
        when(mockServerProfile.getPassword()).thenReturn(PASSWORD);
        when(mockJsXmlSpiceServiceWrapper.getSpiceManager()).thenReturn(mMockedSpiceManager);
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        super.tearDown();
    }

    public void testPullToRefresh() throws InterruptedException {
        mMockedSpiceManager.addCachedResponse(bigLookUp);
        mMockedSpiceManager.addCachedResponse(smallLookUp);
        startActivityUnderTest();

        onView(allOf(withId(R.id.refreshLayout), is(not(refreshing()))));

        String lastResourceLabel = Queues.newArrayDeque(smallLookUp.getResourceLookups()).getLast().getLabel();
        onView(withId(android.R.id.list)).perform(swipeDown());
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(lastResourceLabel)))));
        onView(withId(android.R.id.empty)).check(matches(not(isDisplayed())));
    }

    public void testEmptyTextShouldBeInVisibleWhileContentExist() throws InterruptedException {
        mMockedSpiceManager.addCachedResponse(emptyLookUp);
        startActivityUnderTest();

        onView(allOf(withId(R.id.refreshLayout), is(not(refreshing()))));
        onView(withId(android.R.id.empty)).check(matches(isDisplayed()));
        onView(withId(android.R.id.empty)).check(matches(withText(R.string.r_browser_nothing_to_display)));
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).toInstance(mockRestClient);
            bind(JsXmlSpiceServiceWrapper.class).toInstance(mockJsXmlSpiceServiceWrapper);
        }
    }
}
