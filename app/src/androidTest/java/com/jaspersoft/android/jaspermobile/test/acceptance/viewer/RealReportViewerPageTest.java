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

package com.jaspersoft.android.jaspermobile.test.acceptance.viewer;

import com.google.inject.Singleton;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.ReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.SmartMockedSpiceManager;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.util.JsXmlSpiceServiceWrapper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.firstChildOf;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class RealReportViewerPageTest extends ProtoActivityInstrumentation<ReportHtmlViewerActivity_> {
    private static final String RESOURCE_URI = "/Reports/2_Sales_Mix_by_Demographic_Report";
    private static final String RESOURCE_LABEL = "02. Sales Mix by Demographic Report";

    @Mock
    JsXmlSpiceServiceWrapper mockJsXmlSpiceServiceWrapper;
    @Mock
    DatabaseProvider mockDbProvider;

    private SmartMockedSpiceManager mMockedSpiceManager;
    private InputControlsList inputControlList;
    private ResourceLookup mResource;

    public RealReportViewerPageTest() {
        super(ReportHtmlViewerActivity_.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);
        mMockedSpiceManager = SmartMockedSpiceManager.createHybridManager(JsXmlSpiceService.class);
        when(mockJsXmlSpiceServiceWrapper.getSpiceManager())
                .thenReturn(mMockedSpiceManager);

        registerTestModule(new TestModule());
        setDefaultCurrentProfile();

        WebViewInjector.registerFor(ReportHtmlViewerActivity_.class);

        inputControlList = TestResources.get().fromXML(InputControlsList.class, "input_contols_list");
        if (mResource == null) {
            ResourceLookupsList resourceLookupsList =
                    TestResources.get().fromXML(ResourceLookupsList.class, "only_report");
            mResource = resourceLookupsList.getResourceLookups().get(0);
            mResource.setUri(RESOURCE_URI);
            mResource.setLabel(RESOURCE_LABEL);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        mMockedSpiceManager.removeLifeCycleListener();
        WebViewInjector.unregister();
        super.tearDown();
    }

    public void testReportWithInputControls() {
        mMockedSpiceManager.addNetworkResponse(inputControlList);
        setActivityIntent(ReportHtmlViewerActivity_
                .intent(getInstrumentation().getTargetContext())
                .resource(mResource).get());
        startActivityUnderTest();

        mMockedSpiceManager.behaveInRealMode();
        onView(withId(R.id.runReportButton)).perform(click());
        onView(withText(RESOURCE_LABEL)).check(matches(isDisplayed()));
        onView(withId(R.id.showFilters)).check(matches(isDisplayed()));

        rotate();
        onView(firstChildOf(withId(R.id.webViewPlaceholder))).check(matches(isDisplayed()));
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).in(Singleton.class);
            bind(JsXmlSpiceServiceWrapper.class).toInstance(mockJsXmlSpiceServiceWrapper);
            bind(DatabaseProvider.class).toInstance(mockDbProvider);
        }
    }
}
