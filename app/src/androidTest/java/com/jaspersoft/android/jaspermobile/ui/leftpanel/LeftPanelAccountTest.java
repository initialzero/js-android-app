package com.jaspersoft.android.jaspermobile.ui.leftpanel;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.jaspersoft.android.jaspermobile.support.page.LeftPanelPageObject;
import com.jaspersoft.android.jaspermobile.support.page.LoginPageObject;
import com.jaspersoft.android.jaspermobile.support.rule.AuthenticateProfileTestRule;
import com.jaspersoft.android.jaspermobile.ui.view.activity.NavigationActivity_;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * @author Tom Koptel
 * @since 2.6
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LeftPanelAccountTest {
    private LeftPanelPageObject leftPanelPageObject = new LeftPanelPageObject();
    private LoginPageObject loginPageObject = new LoginPageObject();

    @Rule
    public ActivityTestRule<NavigationActivity_> page = new ActivityTestRule<>(NavigationActivity_.class);

    @ClassRule
    public static TestRule authRule = AuthenticateProfileTestRule.configure()
            .withFakeProfile("testLogin")
            .withFakeProfile("Mobile Demo")
            .done();

    @Test
    public void addAccount() {
        leftPanelPageObject.swipeToOpenMenu();
        leftPanelPageObject.clickAccountsButton();
        leftPanelPageObject.clickAddAccountButton();
        loginPageObject.loginButtonMatches(isDisplayed());
    }

    @Test
    public void listAccounts() {
        leftPanelPageObject.swipeToOpenMenu();
        leftPanelPageObject.clickAccountsButton();
        leftPanelPageObject.accountsMatches(isDisplayed());
    }

    @Test
    public void switchAccount() {
        leftPanelPageObject.swipeToOpenMenu();
        leftPanelPageObject.clickAccountsButton();
        leftPanelPageObject.selectAccount("testLogin");

        leftPanelPageObject.clickBurgerButton();
        leftPanelPageObject.profileMatches(withText("testLogin"));
    }

    @Test
    public void demoAccountSelected() {
        leftPanelPageObject.clickBurgerButton();
        leftPanelPageObject.clickAccountsButton();
        leftPanelPageObject.selectAccount("Mobile Demo");

        leftPanelPageObject.clickBurgerButton();
        leftPanelPageObject.profileMatches(withText("Mobile Demo"));
    }
}
