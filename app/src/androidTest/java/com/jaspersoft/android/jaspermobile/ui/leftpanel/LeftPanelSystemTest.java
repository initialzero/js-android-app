package com.jaspersoft.android.jaspermobile.ui.leftpanel;

import android.app.Activity;
import android.app.Instrumentation;
import android.provider.Settings;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.jaspersoft.android.jaspermobile.support.page.LeftPanelPageObject;
import com.jaspersoft.android.jaspermobile.support.rule.AuthenticateProfileTestRule;
import com.jaspersoft.android.jaspermobile.ui.view.activity.NavigationActivity_;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;

/**
 * @author Tom Koptel
 * @since 2.6
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LeftPanelSystemTest {
    private LeftPanelPageObject leftPanelPageObject = new LeftPanelPageObject();

    @Rule
    public IntentsTestRule<NavigationActivity_> page = new IntentsTestRule<>(NavigationActivity_.class);

    @ClassRule
    public static TestRule authRule = AuthenticateProfileTestRule.create();

    @Test
    public void manageAccount() {
        intending(hasAction(Settings.ACTION_SYNC_SETTINGS))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
        leftPanelPageObject.swipeToOpenMenu();
        leftPanelPageObject.clickAccountsButton();
        leftPanelPageObject.clickManageAccountButton();
        intended(hasAction(Settings.ACTION_SYNC_SETTINGS));
    }

    @Test
    public void showFeedback() {
        intending(hasData("mailto:"))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
        leftPanelPageObject.showFeedback();
        intended(hasData("mailto:"));
    }
}
