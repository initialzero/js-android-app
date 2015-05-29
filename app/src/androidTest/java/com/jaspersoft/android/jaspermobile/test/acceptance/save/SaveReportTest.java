/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.test.acceptance.save;

import android.content.Intent;
import android.database.Cursor;
import android.support.test.espresso.NoMatchingViewException;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.navigation.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.AssertDatabaseUtil;
import com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils;
import com.jaspersoft.android.jaspermobile.test.utils.HackedTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.SavedFilesUtil;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.test.utils.TestResponses;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.apache.http.fake.FakeHttpLayerManager;
import org.apache.http.fake.TestHttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.activities.report.fragment.SaveItemFragment.OutputFormat;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasTotalCount;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
public class SaveReportTest extends ProtoActivityInstrumentation<ReportHtmlViewerActivity_> {

    private SavedFilesUtil savedFilesUtil;

    public SaveReportTest() {
        super(ReportHtmlViewerActivity_.class);
    }

    private static ResourceLookup resource;

    private String[] reportsNames;

    private String[] reportsFormats;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        registerTestModule(new HackedTestModule());

        savedFilesUtil = SavedFilesUtil.builder()
                .context(getApplication())
                .setAccount(getActiveAccount())
                .build();
        SavedFilesUtil.deleteSavedItems(getApplication());

        DatabaseUtils.deleteAllSavedItems(getContentResolver());

        ResourceLookupsList resourceLookupsList = TestResources.get().fromXML(ResourceLookupsList.class, "only_report");
        resource = resourceLookupsList.getResourceLookups().get(0);

        reportsNames = new String[]{"First Test Report",
                "Second Test Report", "Some Report", "Test Report",
                "First Test Report", "First Test Report"};

        reportsFormats = new String[]{"PDF", "PDF", "PDF", "HTML", "HTML", "XLS"};
    }

    @Override
    public void tearDown() throws Exception {
        unregisterTestModule();
        FakeHttpLayerManager.clearHttpResponseRules();

        SavedFilesUtil.deleteSavedItems(getApplication());
        DatabaseUtils.deleteAllSavedItems(getContentResolver());

        super.tearDown();
    }

    //---------------------------------------------------------------------
    // Save test methods
    //---------------------------------------------------------------------

    @Test
    public void testSaveWithDifferentNames() throws IOException {
        prepareSaveReportActivity(resource);

        String reportName = resource.getLabel();
        String differReportName = "Test name";

        // Save reports with different names
        saveReport(reportName, OutputFormat.PDF);
        saveReport(differReportName, OutputFormat.PDF);

        //Check if files are saved proper in DB
        assertTrue(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), reportName, "PDF"));
        assertTrue(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), differReportName, "PDF"));

        //Check if files are saved proper in file system
        assertTrue(savedFilesUtil.hasSavedItem(reportName, "PDF"));
        assertTrue(savedFilesUtil.hasSavedItem(differReportName, "PDF"));

        //Check if files are properly displayed in list
        openSavePage();
        onView(withId(android.R.id.empty)).check(matches(not(isDisplayed())));

        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0)
                .onChildView(withId(android.R.id.text1)).check(matches(withText(reportName)));

        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(1)
                .onChildView(withId(android.R.id.text1)).check(matches(withText(differReportName)));
    }

    @Test
    public void testSaveWithDifferentFormats() throws Throwable {
        prepareSaveReportActivity(resource);
        String reportName = resource.getLabel();

        // Save reports in different format
        saveReport(reportName, OutputFormat.HTML);
        saveReport(reportName, OutputFormat.PDF);
        saveReport(reportName, OutputFormat.XLS);

        //Check if files are saved proper in DB
        assertTrue(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), reportName, "HTML"));
        assertTrue(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), reportName, "PDF"));
        assertTrue(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), reportName, "XLS"));

        //Check if files are saved proper in file system
        assertTrue(savedFilesUtil.hasSavedItem(reportName, "HTML"));
        assertTrue(savedFilesUtil.hasSavedItem(reportName, "PDF"));
        assertTrue(savedFilesUtil.hasSavedItem(reportName, "XLS"));

        //Check if files are properly displayed in list
        openSavePage();
        onView(withId(android.R.id.empty)).check(matches(not(isDisplayed())));
        for (int i = 0; i < 3; i++) {
            onData(is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(reportName)));
        }
    }

    @Test
    public void testNotSaveWithSameName() throws Throwable {
        prepareSaveReportActivity(resource);
        saveReport(resource.getLabel(), OutputFormat.PDF);
        // Asserting file uniqueness, so that storing again
        saveReport(resource.getLabel(), OutputFormat.PDF);

        onView(withId(R.id.report_name_input)).check(matches(hasErrorText(getActivity().getString(R.string.sr_error_report_exists))));
    }

    //---------------------------------------------------------------------
    // Test rename methods
    //---------------------------------------------------------------------

    @Test
    public void testRename() throws IOException {
        prepareSaveReportActivity(resource);

        String oldReportName = resource.getLabel();
        String newReportName = "Renamed - " + oldReportName;

        saveReport(oldReportName, OutputFormat.PDF);
        openSavePage();

        onView(withText(oldReportName)).perform(longClick());
        onView(withId(R.id.renameItem)).perform(click());
        onView(withId(R.id.report_name_input)).perform(clearText());
        onView(withId(R.id.report_name_input)).perform(typeText(newReportName));
        onOverflowView(getActivity(), withText("OK")).perform(click());

        // Check if file is renamed in db and in file system
        assertTrue(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), newReportName, "PDF"));
        assertTrue(savedFilesUtil.hasSavedItem(newReportName, "PDF"));

        // Check if file is renamed in UI
        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0)
                .onChildView(withId(android.R.id.text1)).check(matches(withText(newReportName)));

        // Check if there is no file with old file name
        assertFalse(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), oldReportName, "PDF"));
        assertFalse(savedFilesUtil.hasSavedItem(oldReportName, "PDF"));
    }

    @Test
    public void testNotRenameWithExistingName() throws Throwable {
        prepareSaveReportActivity(resource);

        String oldReportName = resource.getLabel();
        String newReportName = "Renamed - " + oldReportName;

        saveReport(oldReportName, OutputFormat.PDF);
        saveReport(newReportName, OutputFormat.PDF);

        openSavePage();

        onView(withText(oldReportName)).perform(longClick());
        onView(withId(R.id.renameItem)).perform(click());
        onView(withId(R.id.report_name_input)).perform(clearText());
        onView(withId(R.id.report_name_input)).perform(typeText(newReportName));
        onOverflowView(getActivity(), withText("OK")).perform(click());

        onView(withId(R.id.report_name_input)).check(matches(hasErrorText(getActivity().getString(R.string.sr_error_report_exists))));
    }


    //---------------------------------------------------------------------
    // Test delete methods
    //---------------------------------------------------------------------
    @Test
    public void testDelete() throws IOException {
        prepareSaveReportActivity(resource);
        String reportName = resource.getLabel();

        saveReport(reportName, OutputFormat.PDF);
        openSavePage();

        onView(withText(reportName)).perform(longClick());
        onView(withId(R.id.deleteItem)).perform(click());
        onOverflowView(getActivity(), withText("Delete")).perform(click());

        // Check if file is deleted from list UI
        onView(withId(android.R.id.empty)).check(matches(isDisplayed()));

        // Check if file is deleted from DB and file system
        assertFalse(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), reportName, "PDF"));
        assertFalse(savedFilesUtil.hasSavedItem(reportName, "PDF"));
    }

    //---------------------------------------------------------------------
    // Test filtering
    //---------------------------------------------------------------------
    @Test
    public void testFilterOption() throws IOException, InterruptedException {
        prepareSaveReportActivity(resource);

        for (int i = 0; i < reportsNames.length; i++) {
            saveReport(reportsNames[i] + reportsFormats[i], OutputFormat.valueOf(reportsFormats[i]));
        }

        openSavePage();
        onView(withId(android.R.id.list)).check(hasTotalCount(reportsNames.length));

        // Check if HTML list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_option_html)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(2));

        // Check if PDF list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_option_pdf)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(3));

        // Check if XLS list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_option_xls)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(1));

        // Check if whole list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_option_all)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(reportsNames.length));
    }

    @Test
    public void testFilteringIsPersistentAfterRotate() throws IOException {
        prepareSaveReportActivity(resource);

        for (int i = 0; i < reportsNames.length; i++) {
            saveReport(reportsNames[i] + reportsFormats[i], OutputFormat.valueOf(reportsFormats[i]));
        }

        openSavePage();
        onView(withId(android.R.id.list)).check(hasTotalCount(reportsNames.length));

        // Check if HTML list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_option_html)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(2));

        // Check if HTML list is correct after rotate
        rotate();
        onView(withId(android.R.id.list)).check(hasTotalCount(2));

        // Check if PDF list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_option_pdf)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(3));

        // Check if PDF list is correct after rotate
        rotate();
        onView(withId(android.R.id.list)).check(hasTotalCount(3));
    }

    @Test
    public void testFilteringIsPersistentAfterSwitchViewType() throws IOException {
        prepareSaveReportActivity(resource);

        for (int i = 0; i < reportsNames.length; i++) {
            saveReport(reportsNames[i] + reportsFormats[i], OutputFormat.valueOf(reportsFormats[i]));
        }

        openSavePage();
        onView(withId(android.R.id.list)).check(hasTotalCount(reportsNames.length));

        // Check if HTML list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_option_html)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(2));

        // Check if HTML list is correct after rotate
        onView(withId(R.id.switchLayout)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(2));

        // Check if PDF list is correct
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_option_pdf)).perform(click());

        onView(withId(android.R.id.list)).check(hasTotalCount(3));

        // Check if HTML list is correct after rotate
        onView(withId(R.id.switchLayout)).perform(click());
        onView(withId(android.R.id.list)).check(hasTotalCount(3));
    }

    //---------------------------------------------------------------------
    // Test sorting
    //---------------------------------------------------------------------
    @Test
    public void testSortOption() throws IOException, InterruptedException {
        prepareSaveReportActivity(resource);

        String[] reportFullNames = new String[reportsNames.length];

        for (int i = 0; i < reportsNames.length; i++) {
            reportFullNames[i] = reportsNames[i] + reportsFormats[i];
            saveReport(reportFullNames[i], OutputFormat.valueOf(reportsFormats[i]));
        }

        openSavePage();

        // Check if list by date is correct
        clickSortMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_sort_date)).perform(click());

        for (int i = 0; i < reportFullNames.length; i++) {
            onData(is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(reportFullNames[reportFullNames.length - i - 1])));
        }

        // Check if list by label is correct
        clickSortMenuItem();
        Arrays.sort(reportFullNames);
        onOverflowView(getActivity(), withText(R.string.s_fd_sort_label)).perform(click());

        for (int i = 0; i < reportFullNames.length; i++) {
            onData(is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(reportFullNames[i])));
        }
    }

    @Test
    public void testSortingIsPersistentAfterRotate() throws IOException {
        prepareSaveReportActivity(resource);

        String[] reportFullNames = new String[reportsNames.length];

        for (int i = 0; i < reportsNames.length; i++) {
            reportFullNames[i] = reportsNames[i] + reportsFormats[i];
            saveReport(reportFullNames[i], OutputFormat.valueOf(reportsFormats[i]));
        }

        openSavePage();

        // Check if list by date is correct
        clickSortMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_sort_date)).perform(click());

        for (int i = 0; i < reportFullNames.length; i++) {
            onData(is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(reportFullNames[reportFullNames.length - i - 1])));
        }

        rotate();

        // Check if list by date is correct after rotate

        for (int i = 0; i < reportFullNames.length; i++) {
            onData(is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(reportFullNames[reportsNames.length - i - 1])));
        }
    }

    @Test
    public void testSortingIsPersistentAfterSwitchViewType() throws IOException {
        prepareSaveReportActivity(resource);

        String[] reportFullNames = new String[reportsNames.length];

        for (int i = 0; i < reportsNames.length; i++) {
            reportFullNames[i] = reportsNames[i] + reportsFormats[i];
            saveReport(reportFullNames[i], OutputFormat.valueOf(reportsFormats[i]));
        }

        openSavePage();

        // Check if list by date is correct
        clickSortMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_sort_date)).perform(click());

        for (int i = 0; i < reportFullNames.length; i++) {
            onData(is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(reportFullNames[reportsNames.length - i - 1])));
        }

        onView(withId(R.id.switchLayout)).perform(click());

        // Check if list by date is correct after rotate

        for (int i = 0; i < reportFullNames.length; i++) {
            onData(is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(reportFullNames[reportsNames.length - i - 1])));
        }
    }

    //---------------------------------------------------------------------
    // Test search feature
    //---------------------------------------------------------------------
    @Test
    public void testSearch() throws IOException {
        prepareSaveReportActivity(resource);

        String[] reportFullNames = new String[reportsNames.length];

        for (int i = 0; i < reportsNames.length; i++) {
            reportFullNames[i] = reportsNames[i] + reportsFormats[i];
            saveReport(reportFullNames[i], OutputFormat.valueOf(reportsFormats[i]));
        }

        openSavePage();

        onView(withId(R.id.search)).perform(click());
        onView(withId(R.id.search_src_text)).perform(typeText("Fi"));
        onView(withId(R.id.search_src_text)).perform(pressImeActionButton());

        // Check if list by date is correct after rotate

        for (int i = 0; i < 3; i++) {
            onData(is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(containsString("Fi"))));
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------
    private void prepareSaveReportActivity(ResourceLookup otherResource) throws IOException {
        FakeHttpLayerManager.setDefaultHttpResponse(TestResponses.get().noContent());
        prepareIntent(otherResource);
        startActivityUnderTest();

        // Preparing HTPP call mocks
        TestHttpResponse fileResponse = new TestHttpResponse(200, TestResources.get().getBytes(TestResources.ONLY_REPORT));
        FakeHttpLayerManager.addHttpResponseRule(ApiMatcher.REPORT_EXECUTIONS, TestResponses.REPORT_EXECUTION);
        FakeHttpLayerManager.addHttpResponseRule(ApiMatcher.OUTPUT_RESOURCE, fileResponse);
    }

    private void prepareIntent(ResourceLookup otherResource) {
        Intent htmlViewer = new Intent();
        htmlViewer.putExtra(ReportHtmlViewerActivity_.RESOURCE_EXTRA, otherResource);
        setActivityIntent(htmlViewer);
    }

    private void saveReport(String name, OutputFormat format) {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.rv_ab_save_report)).perform(click());

        onView(withId(R.id.report_name_input)).perform(clearText());
        onView(withId(R.id.report_name_input)).perform(typeText(name));

        onView(withId(R.id.output_format_spinner)).perform(click());
        onData(allOf(is(instanceOf(OutputFormat.class)), is(format))).perform(click());

        onView(withId(R.id.saveAction)).perform(click());
    }

    private void openSavePage() {
        getInstrumentation().startActivitySync(
                NavigationActivity_.intent(getInstrumentation().getTargetContext())
                        .currentSelection(R.id.vg_saved_items)
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .get());
        getInstrumentation().waitForIdleSync();
    }

    private void clickFilterMenuItem() {
        try {
            onView(withId(R.id.filter)).perform(click());
        } catch (NoMatchingViewException ex) {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
            onView(withText(R.string.s_ab_filter_by)).perform(click());
        }
    }

    private void clickSortMenuItem() {
        try {
            onView(withId(R.id.sort)).perform(click());
        } catch (NoMatchingViewException ex) {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
            try {
                onView(withText(R.string.s_ab_sort_by)).perform(click());
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }
}
