package com.jaspersoft.android.jaspermobile.test.acceptance.save;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.storage.SavedReportsActivity_;
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
import org.hamcrest.Matchers;

import java.io.IOException;
import java.util.Arrays;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.longClick;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.pressImeActionButton;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isChecked;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasTotalCount;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
public class SaveReportTest extends ProtoActivityInstrumentation<ReportHtmlViewerActivity_> {

    public SaveReportTest() {
        super(ReportHtmlViewerActivity_.class);
    }

    private static ResourceLookup resource;

    private String[] reportsNames;

    private String[] reportsFormats;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registerTestModule(new HackedTestModule());
        setDefaultCurrentProfile();

        SavedFilesUtil.clear(getApplication());
        DatabaseUtils.deleteAllSavedItems(getApplication().getContentResolver());

        ResourceLookupsList resourceLookupsList = TestResources.get().fromXML(ResourceLookupsList.class, "only_report");
        resource = resourceLookupsList.getResourceLookups().get(0);

        reportsNames = new String[]{"First Test Report",
                "Second Test Report", "Some Report", "Test Report",
                "First Test Report", "First Test Report"};

        reportsFormats = new String[]{"PDF", "PDF", "PDF", "HTML", "HTML", "XLS"};
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        FakeHttpLayerManager.clearHttpResponseRules();

        SavedFilesUtil.clear(getApplication());
        DatabaseUtils.deleteAllSavedItems(getApplication().getContentResolver());

        super.tearDown();
    }


    //---------------------------------------------------------------------
    // Save test methods
    //---------------------------------------------------------------------
    public void testSaveWithDifferentNames() throws IOException {
        prepareSaveReportActivity(resource);

        String reportName = resource.getLabel();
        String differReportName = "Test name";
        long profileId = getServerProfile().getId();

        // Save reports with different names
        saveReport(reportName, "PDF");
        saveReport(differReportName, "PDF");

        //Check if files are saved proper in DB
        assertTrue(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), reportName, "PDF"));
        assertTrue(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), differReportName, "PDF"));

        //Check if files are saved proper in file system
        assertTrue(SavedFilesUtil.contains(getActivity(), reportName, "PDF", profileId));
        assertTrue(SavedFilesUtil.contains(getActivity(), differReportName, "PDF", profileId));

        //Check if files are properly displayed in list
        openSavePage();
        onView(withId(android.R.id.empty)).check(matches(not(isDisplayed())));

        onData(Matchers.is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0)
                .onChildView(withId(android.R.id.text1)).check(matches(withText(reportName)));

        onData(Matchers.is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(1)
                .onChildView(withId(android.R.id.text1)).check(matches(withText(differReportName)));
    }

    public void testSaveWithDifferentFormats() throws Throwable {
        prepareSaveReportActivity(resource);

        String reportName = resource.getLabel();
        long profileId = getServerProfile().getId();

        // Save reports in different format
        saveReport(reportName, "HTML");
        saveReport(reportName, "PDF");
        saveReport(reportName, "XLS");

        //Check if files are saved proper in DB
        assertTrue(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), reportName, "HTML"));
        assertTrue(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), reportName, "PDF"));
        assertTrue(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), reportName, "XLS"));

        //Check if files are saved proper in file system
        assertTrue(SavedFilesUtil.contains(getActivity(), reportName, "HTML", profileId));
        assertTrue(SavedFilesUtil.contains(getActivity(), reportName, "PDF", profileId));
        assertTrue(SavedFilesUtil.contains(getActivity(), reportName, "XLS", profileId));

        //Check if files are properly displayed in list
        openSavePage();
        onView(withId(android.R.id.empty)).check(matches(not(isDisplayed())));
        for (int i = 0; i < 3; i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(reportName)));
        }
    }

    public void testNotSaveWithSameName() throws Throwable {
        prepareSaveReportActivity(resource);
        saveReport(resource.getLabel(), "PDF");
        // Asserting file uniqueness, so that storing again
        saveReport(resource.getLabel(), "PDF");

        onView(withId(R.id.report_name_input)).check(matches(hasErrorText(getActivity().getString(R.string.sr_error_report_exists))));
    }

    //---------------------------------------------------------------------
    // Test rename methods
    //---------------------------------------------------------------------

    public void testRename() throws IOException {
        prepareSaveReportActivity(resource);

        String oldReportName = resource.getLabel();
        String newReportName = "Renamed - " + oldReportName;
        long profileId = getServerProfile().getId();

        saveReport(oldReportName, "PDF");
        openSavePage();

        onView(withText(oldReportName)).perform(longClick());
        onView(withId(R.id.renameItem)).perform(click());
        onView(withId(R.id.report_name_input)).perform(clearText());
        onView(withId(R.id.report_name_input)).perform(typeText(newReportName));
        onOverflowView(getActivity(), withText("OK")).perform(click());

        // Check if file is renamed in db and in file system
        assertTrue(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), newReportName, "PDF"));
        assertTrue(SavedFilesUtil.contains(getActivity(), newReportName, "PDF", profileId));

        // Check if file is renamed in UI
        onData(Matchers.is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0)
                .onChildView(withId(android.R.id.text1)).check(matches(withText(newReportName)));

        // Check if there is no file with old file name
        assertFalse(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), oldReportName, "PDF"));
        assertFalse(SavedFilesUtil.contains(getActivity(), oldReportName, "PDF", profileId));
    }

    public void testNotRenameWithExistingName() throws Throwable {
        prepareSaveReportActivity(resource);

        String oldReportName = resource.getLabel();
        String newReportName = "Renamed - " + oldReportName;

        saveReport(oldReportName, "PDF");
        saveReport(newReportName, "PDF");

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
    public void testDelete() throws IOException {
        prepareSaveReportActivity(resource);

        String reportName = resource.getLabel();
        long profileId = getServerProfile().getId();

        saveReport(reportName, "PDF");
        openSavePage();

        onView(withText(reportName)).perform(longClick());
        onView(withId(R.id.deleteItem)).perform(click());
        onOverflowView(getActivity(), withText("Delete")).perform(click());

        // Check if file is deleted from list UI
        onView(withId(android.R.id.empty)).check(matches(isDisplayed()));

        // Check if file is deleted from DB and file system
        assertFalse(AssertDatabaseUtil.containsSavedReport(getActivity().getContentResolver(), reportName, "PDF"));
        assertFalse(SavedFilesUtil.contains(getActivity(), reportName, "PDF", profileId));
    }

    //---------------------------------------------------------------------
    // Test filtering
    //---------------------------------------------------------------------
    public void testFilterOption() throws IOException, InterruptedException {
        prepareSaveReportActivity(resource);

        for (int i = 0; i < reportsNames.length; i++) {
            saveReport(reportsNames[i] + reportsFormats[i], reportsFormats[i]);
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

    public void testFilteringIsPersistentAfterRotate() throws IOException {
        prepareSaveReportActivity(resource);

        for (int i = 0; i < reportsNames.length; i++) {
            saveReport(reportsNames[i] + reportsFormats[i], reportsFormats[i]);
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

    public void testFilteringIsPersistentAfterSwitchViewType() throws IOException {
        prepareSaveReportActivity(resource);

        for (int i = 0; i < reportsNames.length; i++) {
            saveReport(reportsNames[i] + reportsFormats[i], reportsFormats[i]);
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
    public void testSortOption() throws IOException, InterruptedException {
        prepareSaveReportActivity(resource);

        String[] reportFullNames = new String[reportsNames.length];

        for (int i = 0; i < reportsNames.length; i++) {
            Thread.sleep(2000);
            reportFullNames[i] = reportsNames[i] + reportsFormats[i];
            saveReport(reportFullNames[i], reportsFormats[i]);
        }

        openSavePage();

        // Check if list by date is correct
        clickSortMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_sort_date)).perform(click());

        for (int i = 0; i < reportFullNames.length; i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(reportFullNames[reportFullNames.length - i - 1])));
        }

        // Check if list by label is correct
        clickSortMenuItem();
        Arrays.sort(reportFullNames);
        onOverflowView(getActivity(), withText(R.string.si_fd_sort_label)).perform(click());

        for (int i = 0; i < reportFullNames.length; i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(reportFullNames[i])));
        }
    }

    public void testSortingIsPersistentAfterRotate() throws IOException {
        prepareSaveReportActivity(resource);

        String[] reportFullNames = new String[reportsNames.length];

        for (int i = 0; i < reportsNames.length; i++) {
            reportFullNames[i] = reportsNames[i] + reportsFormats[i];
            saveReport(reportFullNames[i], reportsFormats[i]);
        }

        openSavePage();

        // Check if list by date is correct
        clickSortMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_sort_date)).perform(click());

        for (int i = 0; i < reportFullNames.length; i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(reportFullNames[reportFullNames.length - i - 1])));
        }

        rotate();

        // Check if list by date is correct after rotate

        for (int i = 0; i < reportFullNames.length; i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(reportFullNames[reportsNames.length - i - 1])));
        }
    }

    public void testSortingIsPersistentAfterSwitchViewType() throws IOException {
        prepareSaveReportActivity(resource);

        String[] reportFullNames = new String[reportsNames.length];

        for (int i = 0; i < reportsNames.length; i++) {
            reportFullNames[i] = reportsNames[i] + reportsFormats[i];
            saveReport(reportFullNames[i], reportsFormats[i]);
        }

        openSavePage();

        // Check if list by date is correct
        clickSortMenuItem();
        onOverflowView(getActivity(), withText(R.string.si_fd_sort_date)).perform(click());

        for (int i = 0; i < reportFullNames.length; i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(reportFullNames[reportsNames.length - i - 1])));
        }

        onView(withId(R.id.switchLayout)).perform(click());

        // Check if list by date is correct after rotate

        for (int i = 0; i < reportFullNames.length; i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
                    .inAdapterView(withId(android.R.id.list))
                    .atPosition(i)
                    .onChildView(withId(android.R.id.text1)).check(matches(withText(reportFullNames[reportsNames.length - i - 1])));
        }
    }

    //---------------------------------------------------------------------
    // Test search feature
    //---------------------------------------------------------------------
    public void testSearch() throws IOException {
        prepareSaveReportActivity(resource);

        String[] reportFullNames = new String[reportsNames.length];

        for (int i = 0; i < reportsNames.length; i++) {
            reportFullNames[i] = reportsNames[i] + reportsFormats[i];
            saveReport(reportFullNames[i], reportsFormats[i]);
        }

        openSavePage();

        onView(withId(R.id.search)).perform(click());
        onView(withId(getSearcFieldId())).perform(typeText("Fi"));
        onView(withId(getSearcFieldId())).perform(pressImeActionButton());

        // Check if list by date is correct after rotate

        for (int i = 0; i < 3; i++) {
            onData(Matchers.is(instanceOf(Cursor.class)))
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
        FakeHttpLayerManager.addHttpResponseRule(ApiMatcher.SERVER_INFO, TestResponses.SERVER_INFO);
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

    private void saveReport(String name, String format) {
        onView(withId(R.id.saveReport)).perform(click());

        onView(withId(R.id.report_name_input)).perform(clearText());
        onView(withId(R.id.report_name_input)).perform(typeText(name));
        onView(withId(R.id.output_format_spinner)).perform(click());
        onView(withText(format)).perform(click());
        onView(withId(R.id.saveAction)).perform(click());
    }

    private void openSavePage() {
        getInstrumentation().startActivitySync(
                SavedReportsActivity_.intent(getInstrumentation().getTargetContext())
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .get());
        getInstrumentation().waitForIdleSync();
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
}
