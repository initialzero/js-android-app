package com.jaspersoft.android.jaspermobile.test.acceptance.save;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleCallback;
import com.google.android.apps.common.testing.testrunner.Stage;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.google.android.apps.common.testing.ui.espresso.contrib.CountingIdlingResource;
import com.google.inject.Singleton;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.report.SaveReportActivity_;
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

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;
import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class SaveReportGeneralTest extends ProtoActivityInstrumentation<ReportHtmlViewerActivity_> {

    protected static final String RESOURCE_URI = "/Reports/2_Sales_Mix_by_Demographic_Report";
    protected static final String RESOURCE_LABEL = "02. Sales Mix by Demographic Report";

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

    // TODO: provide test case
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

    private class ActivityMonitor implements ActivityLifecycleCallback {
        private final CountingIdlingResource idlingResource;

        private ActivityMonitor() {
            idlingResource = new CountingIdlingResource("Activity idle resource");
            Espresso.registerIdlingResources(idlingResource);
        }

        @Override
        public void onActivityLifecycleChanged(Activity activity, Stage stage) {
            handleReportViewerLifeCycle(activity, stage);
            handleSaveItemLifeCycle(activity, stage);
        }

        private void handleReportViewerLifeCycle(Activity activity, Stage stage) {
            ComponentName targetComponentName =
                    new ComponentName(activity, ReportHtmlViewerActivity_.class);

            ComponentName currentComponentName = activity.getComponentName();
            if (!currentComponentName.equals(targetComponentName)) return;

            switch (stage) {
                case RESTARTED:
                    idlingResource.increment();
                    break;
                case RESUMED:
                    if (!idlingResource.isIdleNow()) {
                        idlingResource.decrement();
                    }
                    break;
                default: // NOP
            }
        }

        private void handleSaveItemLifeCycle(Activity activity, Stage stage) {
            ComponentName targetComponentName =
                    new ComponentName(activity, SaveReportActivity_.class);

            ComponentName currentComponentName = activity.getComponentName();
            if (!currentComponentName.equals(targetComponentName)) return;

            switch (stage) {
                case RESUMED:
                    idlingResource.increment();
                case DESTROYED:
                    idlingResource.decrement();
                    break;
                default: // NOP
            }
        }
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).in(Singleton.class);
            bind(JsXmlSpiceServiceWrapper.class).toInstance(jsXmlSpiceServiceWrapper);
        }
    }

}
