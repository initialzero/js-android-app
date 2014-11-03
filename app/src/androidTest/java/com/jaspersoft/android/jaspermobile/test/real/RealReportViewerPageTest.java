/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.test.real;

import android.test.suitebuilder.annotation.Suppress;

import com.google.inject.Singleton;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.emerald2.ReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.acceptance.viewer.WebViewInjector;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.DummyResourceUtils;
import com.jaspersoft.android.jaspermobile.test.utils.IdleInjector;
import com.jaspersoft.android.jaspermobile.test.utils.SmartMockedSpiceManager;
import com.jaspersoft.android.jaspermobile.util.JsSpiceManager;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.util.concurrent.TimeUnit;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.firstChildOf;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@Suppress
public class RealReportViewerPageTest extends ProtoActivityInstrumentation<ReportHtmlViewerActivity_> {

    private final ResourceLookup mResource = DummyResourceUtils.createLookupWithIC();
    private SmartMockedSpiceManager mMockedSpiceManager;
    private IdleInjector injector;

    public RealReportViewerPageTest() {
        super(ReportHtmlViewerActivity_.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        mMockedSpiceManager = SmartMockedSpiceManager.builder()
                .setIdlingResourceTimeout(3, TimeUnit.MINUTES)
                .setDebugable(true)
                .setResponseChain(InputControlsList.class, ReportExecutionResponse.class)
                .build();

        registerTestModule(new TestModule());
        setDefaultCurrentProfile();

        injector = WebViewInjector.registerFor(ReportHtmlViewerActivity_.class);
        setActivityIntent(ReportHtmlViewerActivity_
                .intent(getInstrumentation().getTargetContext())
                .resource(mResource).get());
        startActivityUnderTest();
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        mMockedSpiceManager.removeLifeCycleListener();
        injector.unregister();
        super.tearDown();
    }

    public void testReportWithInputControls() {
        onView(withId(getActionBarTitleId())).check(matches(withText(DummyResourceUtils.RESOURCE_DEFAULT_LABEL)));
        onView(withId(R.id.showFilters)).check(matches(isDisplayed()));

        rotate();

        onView(firstChildOf(withId(R.id.webViewPlaceholder))).check(matches(isDisplayed()));
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).in(Singleton.class);
            bind(JsSpiceManager.class).toInstance(mMockedSpiceManager);
        }
    }

}
