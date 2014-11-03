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

package com.jaspersoft.android.jaspermobile.test.real;

import android.app.Application;
import android.content.Intent;
import android.test.suitebuilder.annotation.Suppress;

import com.google.inject.Singleton;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.storage.SavedReportsActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.emerald2.ReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.acceptance.viewer.WebViewInjector;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.DummyResourceUtils;
import com.jaspersoft.android.jaspermobile.test.utils.IdleInjector;
import com.jaspersoft.android.jaspermobile.test.utils.SmartMockedSpiceManager;
import com.jaspersoft.android.jaspermobile.util.JsSpiceManager;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.longClick;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.DummyResourceUtils.RESOURCE_DEFAULT_LABEL;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasTotalCount;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.LongListMatchers.withAdaptedData;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.LongListMatchers.withItemContent;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@Suppress
public class RealSaveReportPageTest extends ProtoActivityInstrumentation<ReportHtmlViewerActivity_> {

    private static final String NEW_FILE_NAME = "Renamed";

    @Mock
    File mockFile;

    private SmartMockedSpiceManager mockedSpiceManager;
    private Application mApplication;
    private IdleInjector idleInjector;

    public RealSaveReportPageTest() {
        super(ReportHtmlViewerActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);
        mApplication = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        mockedSpiceManager = SmartMockedSpiceManager.builder()
                .setIdlingResourceTimeout(3, TimeUnit.MINUTES)
                .setMocked(false).build();
        registerTestModule(new TestModule());

        // Force default profile
        setDefaultCurrentProfile();

        // Clean all items in directory
        File appFilesDir = mApplication.getExternalFilesDir(null);
        File savedReportsDir = new File(appFilesDir, JasperMobileApplication.SAVED_REPORTS_DIR_NAME);
        FileUtils.deleteFilesInDirectory(savedReportsDir);

        idleInjector = WebViewInjector.registerFor(ReportHtmlViewerActivity_.class);
        configureIntent();
        startActivityUnderTest();
    }

    @Override
    protected void tearDown() throws Exception {
        idleInjector.unregister();
        mockedSpiceManager.removeLifeCycleListener();
        unregisterTestModule();
        super.tearDown();
    }

    public void testValidateFieldShouldNotAcceptSameName() {
        onView(withId(R.id.saveReport)).perform(click());
        onView(withId(R.id.output_format_spinner)).perform(click());
        onView(withText("PDF")).perform(click());
        onView(withId(R.id.saveAction)).perform(click());

        onView(withId(android.R.id.content)).check(matches(isDisplayed()));
        onView(withId(R.id.output_format_spinner)).perform(click());
        onView(withText("PDF")).perform(click());
        onView(withId(R.id.saveReport)).perform(click());

        onView(withId(R.id.saveAction)).perform(click());
        onView(withId(R.id.report_name_input)).check(matches(hasErrorText(getActivity().getString(R.string.sr_error_report_exists))));
    }

    public void testHtmlSavedItemInteractions() throws InterruptedException {
        onView(withId(R.id.saveReport)).perform(click());
        onView(withId(R.id.output_format_spinner)).perform(click());
        onView(withText("PDF")).perform(click());
        onView(withId(R.id.saveAction)).perform(click());

        onView(withId(android.R.id.content)).check(matches(isDisplayed()));

        openSavePage();

        // We are on the list page
        onView(withId(android.R.id.empty)).check(matches(not(isDisplayed())));
        onView(withId(android.R.id.list)).check(hasTotalCount(1));

        onData(is(instanceOf(File.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.renameItem)).perform(click());

        onOverflowView(getActivity(), withId(R.id.report_name_input)).perform(clearText());
        onOverflowView(getActivity(), withId(R.id.report_name_input)).perform(typeText(NEW_FILE_NAME));
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(NEW_FILE_NAME)))));

        onData(is(instanceOf(File.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.deleteItem)).perform(click());

        onOverflowView(getActivity(), withText(getActivity().getString(R.string.sdr_drd_msg, NEW_FILE_NAME))).check(matches(isDisplayed()));
        onOverflowView(getActivity(), withText(R.string.spm_delete_btn)).perform(click());

        onView(withId(android.R.id.empty)).check(matches(allOf(withText(R.string.r_browser_nothing_to_display), isDisplayed())));
        onView(withId(android.R.id.list)).check(hasTotalCount(0));
    }

    public void testDeleteSavedHtmlReportFromViewer() throws Throwable {
        onView(withId(R.id.saveReport)).perform(click());
        onView(withId(R.id.output_format_spinner)).perform(click());
        onView(withText("HTML")).perform(click());
        onView(withId(R.id.saveAction)).perform(click());

        onView(withId(android.R.id.content)).check(matches(isDisplayed()));

        openSavePage();

        // We are on the list page
        onView(withId(android.R.id.empty)).check(matches(not(isDisplayed())));
        onView(withId(android.R.id.list)).check(hasTotalCount(1));

        onData(is(instanceOf(File.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());

        onView(withId(R.id.deleteItem)).perform(click());

        onOverflowView(getActivity(), withText(getActivity().getString(R.string.sdr_drd_msg, RESOURCE_DEFAULT_LABEL))).check(matches(isDisplayed()));
        onOverflowView(getActivity(), withText(R.string.spm_delete_btn)).perform(click());

        onView(withId(android.R.id.empty)).check(matches(allOf(withText(R.string.r_browser_nothing_to_display), isDisplayed())));
        onView(withId(android.R.id.list)).check(hasTotalCount(0));
    }

    private void openSavePage() {
        getInstrumentation().startActivitySync(
                SavedReportsActivity_.intent(getInstrumentation().getTargetContext())
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .get());
        getInstrumentation().waitForIdleSync();
    }

    private void configureIntent() {
        setActivityIntent(ReportHtmlViewerActivity_.intent(mApplication)
                .flags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .resource(DummyResourceUtils.createDefaultLookup()).get());
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).in(Singleton.class);
            bind(JsSpiceManager.class).toInstance(mockedSpiceManager);
        }
    }

}
