package com.jaspersoft.android.jaspermobile.test.acceptance.save;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

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
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registerTestModule(new HackedTestModule());
        setDefaultCurrentProfile();

        SavedFilesUtil.clear(getApplication());
        DatabaseUtils.deleteAllSavedItems(getApplication().getContentResolver());

        ResourceLookupsList resourceLookupsList = TestResources.get().fromXML(ResourceLookupsList.class, "only_report");
        resource = resourceLookupsList.getResourceLookups().get(0);
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        FakeHttpLayerManager.clearHttpResponseRules();

        SavedFilesUtil.clear(getApplication());
        DatabaseUtils.deleteAllSavedItems(getApplication().getContentResolver());

        super.tearDown();
    }

    public void testSaveWithDifferentFormats() throws Throwable {
        prepareSaveReportActivity(resource);

        String reportName = resource.getLabel();
        long profileId = getServerProfile().getId();

        // Save reports in different format
        saveReport("HTML");
        saveReport("PDF");
        saveReport("XLS");

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
        saveReport("PDF");
        // Asserting file uniqueness, so that storing again
        saveReport("PDF");

        onView(withId(R.id.report_name_input)).check(matches(hasErrorText(getActivity().getString(R.string.sr_error_report_exists))));
    }

    public void testRename() throws IOException {
        prepareSaveReportActivity(resource);

        String oldReportName = resource.getLabel();
        String newReportName = "Renamed - " + oldReportName;
        long profileId = getServerProfile().getId();

        saveReport("PDF");
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

    public void testDelete() throws IOException {
        prepareSaveReportActivity(resource);

        String reportName = resource.getLabel();
        long profileId = getServerProfile().getId();

        saveReport("PDF");
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

    private void saveReport(String format) {
        onView(withId(R.id.saveReport)).perform(click());

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
}
