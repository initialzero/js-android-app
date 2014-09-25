package com.jaspersoft.android.jaspermobile.test.acceptance.save;

import android.content.Intent;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.report.SaveReportActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;

import org.mockito.MockitoAnnotations;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class SaveReportValidationsTest extends ProtoActivityInstrumentation<SaveReportActivity_> {

    protected static final String RESOURCE_URI = "/Reports/2_Sales_Mix_by_Demographic_Report";
    protected static final String RESOURCE_LABEL = "02. Sales Mix by Demographic Report";

    public SaveReportValidationsTest() {
        super(SaveReportActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        registerTestModule(new TestModule());
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        super.tearDown();
    }

    public void testValidateFieldShouldNotAcceptReservedSymbols() {
        prepareIntent();
        startActivityUnderTest();

        char[] chars = {'*', '\\', '/', '"', '\'', ':', '?', '|', '<', '>', '+', '[', ']'};

        for (char symbol : chars) {
            onView(withId(R.id.report_name_input)).perform(clearText());
            onView(withId(R.id.report_name_input)).perform(typeText(String.valueOf(symbol)));
            onView(withId(R.id.saveAction)).perform(click());
            onView(withId(R.id.report_name_input)).check(matches(hasErrorText(getActivity().getString(R.string.sr_error_characters_not_allowed))));
        }
    }

    public void testValidateFieldShouldNotBeEmpty() {
        prepareIntent();
        startActivityUnderTest();
        onView(withId(getActionBarTitleId())).check(matches(withText(R.string.sr_ab_title)));

        onView(withId(R.id.report_name_input)).perform(clearText());
        onView(withId(R.id.saveAction)).perform(click());

        onView(withId(R.id.report_name_input)).check(matches(hasErrorText(getActivity().getString(R.string.sr_error_field_is_empty))));
    }

    private void prepareIntent() {
        Intent metaIntent = new Intent();
        metaIntent.putExtra(SaveReportActivity_.RESOURCE_LABEL_EXTRA, RESOURCE_LABEL);
        metaIntent.putExtra(SaveReportActivity_.RESOURCE_URI_EXTRA, RESOURCE_URI);
        setActivityIntent(metaIntent);
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
        }
    }
}
