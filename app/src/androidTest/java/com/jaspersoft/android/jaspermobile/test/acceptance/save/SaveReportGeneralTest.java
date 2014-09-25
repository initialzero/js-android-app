package com.jaspersoft.android.jaspermobile.test.acceptance.save;

import android.app.Application;
import android.content.Intent;

import com.google.inject.Singleton;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.storage.SavedReportsActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.ReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.acceptance.viewer.WebViewInjector;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.SmartMockedSpiceManager;
import com.jaspersoft.android.jaspermobile.util.JsXmlSpiceServiceWrapper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.doubleClick;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.longClick;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasTotalCount;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.LongListMatchers.withAdaptedData;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.LongListMatchers.withItemContent;
import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class SaveReportGeneralTest extends ProtoActivityInstrumentation<ReportHtmlViewerActivity_> {

    protected static final String RESOURCE_URI = "/Reports/2_Sales_Mix_by_Demographic_Report";
    protected static final String RESOURCE_LABEL = "02. Sales Mix by Demographic Report";
    private static final String NEW_FILE_NAME = "Renamed";

    @Mock
    JsXmlSpiceServiceWrapper jsXmlSpiceServiceWrapper;
    @Mock
    File mockFile;

    private SmartMockedSpiceManager mockedSpiceManager;
    private Application mApplication;

    public SaveReportGeneralTest() {
        super(ReportHtmlViewerActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);
        mApplication = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        mockedSpiceManager = SmartMockedSpiceManager.createHybridManager(JsXmlSpiceService.class);
        mockedSpiceManager.behaveInRealMode();
        when(jsXmlSpiceServiceWrapper.getSpiceManager()).thenReturn(mockedSpiceManager);
        registerTestModule(new TestModule());

        // Force default profile
        setDefaultCurrentProfile();

        // Clean all items in directory
        File appFilesDir = mApplication.getExternalFilesDir(null);
        File savedReportsDir = new File(appFilesDir, JasperMobileApplication.SAVED_REPORTS_DIR_NAME);
        FileUtils.deleteFilesInDirectory(savedReportsDir);

        WebViewInjector.registerFor(ReportHtmlViewerActivity_.class);
    }

    @Override
    protected void tearDown() throws Exception {
        WebViewInjector.unregister();
        mockedSpiceManager.removeLifeCycleListener();
        unregisterTestModule();
        super.tearDown();
    }

    public void testValidateFieldShouldNotAcceptSameName() throws Throwable {
        ResourceLookup resource = new ResourceLookup();
        resource.setLabel(RESOURCE_LABEL);
        resource.setUri(RESOURCE_URI);
        resource.setResourceType(ResourceType.reportUnit.toString());

        setActivityIntent(ReportHtmlViewerActivity_.intent(mApplication)
                .resource(resource).get());
        startActivityUnderTest();

        onView(withId(R.id.saveReport)).perform(click());
        onView(withId(R.id.saveAction)).perform(click());

        getCurrentActivity();
        onView(withId(android.R.id.content)).check(matches(isDisplayed()));
        onView(withId(R.id.saveReport)).perform(click());

        onView(withId(R.id.saveAction)).perform(click());
        onView(withId(R.id.report_name_input)).check(matches(hasErrorText(getActivity().getString(R.string.sr_error_report_exists))));
    }

    public void testHtmlSavedItemInteractions() throws Throwable {
        ResourceLookup resource = new ResourceLookup();
        resource.setLabel(RESOURCE_LABEL);
        resource.setUri(RESOURCE_URI);
        resource.setResourceType(ResourceType.reportUnit.toString());

        setActivityIntent(ReportHtmlViewerActivity_.intent(mApplication)
                .resource(resource).get());
        startActivityUnderTest();

        onView(withId(R.id.saveReport)).perform(click());
        onView(withId(R.id.saveAction)).perform(click());

        onView(withId(android.R.id.content)).check(matches(isDisplayed()));

        getInstrumentation().startActivitySync(
                SavedReportsActivity_.intent(getInstrumentation().getTargetContext())
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK).get());
        getInstrumentation().waitForIdleSync();

        // We are on the list page
        onView(withId(android.R.id.empty)).check(matches(not(isDisplayed())));
        onView(withId(android.R.id.list)).check(hasTotalCount(1));

        onData(is(instanceOf(File.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withText(R.string.sdr_cm_open)).perform(click());
        pressBack();

        onData(is(instanceOf(File.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());
        pressBack();

        onData(is(instanceOf(File.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withText(R.string.sdr_cm_rename)).perform(click());

        onOverflowView(getActivity(), withId(R.id.report_name_input)).perform(clearText());
        onOverflowView(getActivity(), withId(R.id.report_name_input)).perform(typeText(NEW_FILE_NAME));
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(NEW_FILE_NAME)))));

        onData(is(instanceOf(File.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withText(R.string.sdr_cm_delete)).perform(click());

        onOverflowView(getActivity(), withText(getActivity().getString(R.string.sdr_drd_msg, NEW_FILE_NAME))).check(matches(isDisplayed()));
        onOverflowView(getActivity(), withText(R.string.spm_delete_btn)).perform(click());

        onView(withId(android.R.id.empty)).check(matches(allOf(withText(R.string.r_browser_nothing_to_display), isDisplayed())));
        onView(withId(android.R.id.list)).check(hasTotalCount(0));
    }

    public void testDeleteSavedHtmlReportFromViewer() throws Throwable {
        ResourceLookup resource = new ResourceLookup();
        resource.setLabel(RESOURCE_LABEL);
        resource.setUri(RESOURCE_URI);
        resource.setResourceType(ResourceType.reportUnit.toString());

        setActivityIntent(ReportHtmlViewerActivity_.intent(mApplication)
                .resource(resource).get());
        startActivityUnderTest();

        onView(withId(R.id.saveReport)).perform(click());
        onView(withId(R.id.saveAction)).perform(click());

        onView(withId(android.R.id.content)).check(matches(isDisplayed()));

        getInstrumentation().startActivitySync(
                SavedReportsActivity_.intent(getInstrumentation().getTargetContext())
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK).get());
        getInstrumentation().waitForIdleSync();

        // We are on the list page
        onView(withId(android.R.id.empty)).check(matches(not(isDisplayed())));
        onView(withId(android.R.id.list)).check(hasTotalCount(1));

        onData(is(instanceOf(File.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        onView(withId(R.id.deleteItem)).perform(doubleClick());

        onOverflowView(getActivity(), withText(getActivity().getString(R.string.sdr_drd_msg, RESOURCE_LABEL))).check(matches(isDisplayed()));
        onOverflowView(getActivity(), withText(R.string.spm_delete_btn)).perform(click());

        onView(withId(android.R.id.empty)).check(matches(allOf(withText(R.string.r_browser_nothing_to_display), isDisplayed())));
        onView(withId(android.R.id.list)).check(hasTotalCount(0));
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).in(Singleton.class);
            bind(JsXmlSpiceServiceWrapper.class).toInstance(jsXmlSpiceServiceWrapper);
        }
    }

}
