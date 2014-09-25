package com.jaspersoft.android.jaspermobile.test.acceptance.save;

import com.google.inject.Singleton;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.report.SaveReportActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.SmartMockedSpiceManager;
import com.jaspersoft.android.jaspermobile.util.JsXmlSpiceServiceWrapper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class SaveReportPageTest extends ProtoActivityInstrumentation<SaveReportActivity_> {

    @Mock
    JsXmlSpiceServiceWrapper jsXmlSpiceServiceWrapper;

    private SmartMockedSpiceManager mockedSpiceManager;

    public SaveReportPageTest() {
        super(SaveReportActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        mockedSpiceManager = SmartMockedSpiceManager.createMockedManager(JsXmlSpiceService.class);
        when(jsXmlSpiceServiceWrapper.getSpiceManager()).thenReturn(mockedSpiceManager);
        registerTestModule(new TestModule());
        setDefaultCurrentProfile();
    }

    public void testInitialLoad() {
        startActivityUnderTest();
        onView(withId(getActionBarTitleId())).check(matches(withText(R.string.sr_ab_title)));
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).in(Singleton.class);
            bind(JsXmlSpiceServiceWrapper.class).toInstance(jsXmlSpiceServiceWrapper);
        }
    }
}
