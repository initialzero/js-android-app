package com.jaspersoft.android.jaspermobile.ui.login;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.jaspersoft.android.jaspermobile.support.page.LoginPageObject;
import com.jaspersoft.android.jaspermobile.ui.view.activity.NavigationActivity_;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;

/**
 * @author Tom Koptel
 * @since 2.6
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoginFormValidationsTest {

    @Rule
    public ActivityTestRule<NavigationActivity_> page = new ActivityTestRule<>(NavigationActivity_.class);

    private final LoginPageObject loginPageObject = new LoginPageObject();

    @Test
    public void loginWithEmptyAlias() {
        loginPageObject.typeAlias("");
        loginPageObject.clickLoginButton();
        loginPageObject.aliasMatches(hasErrorText("This field is required."));
    }

    @Test
    public void loginWithSpacedAlias() {
        loginPageObject.typeAlias("      ");
        loginPageObject.clickLoginButton();
        loginPageObject.aliasMatches(hasErrorText("This field is required."));
    }

    @Test
    public void loginWithEmptyUrl() {
        loginPageObject.typeAlias("testEmptyUrl");
        loginPageObject.typeUrl("");
        loginPageObject.clickLoginButton();
        loginPageObject.urlMatches(hasErrorText("This field is required."));
    }

    @Test
    public void loginWithSpacedUrl() {
        loginPageObject.typeAlias("testSpacedUrl");
        loginPageObject.typeUrl("      ");
        loginPageObject.clickLoginButton();
        loginPageObject.urlMatches(hasErrorText("URL is not valid."));
    }

    @Test
    public void loginWithInvalidUrl() {
        loginPageObject.typeAlias("testInvalidUrl");
        loginPageObject.typeUrl("error url");
        loginPageObject.clickLoginButton();
        loginPageObject.urlMatches(hasErrorText("URL is not valid."));
    }

    @Test
    public void loginWithEmptyUserName() {
        loginPageObject.typeAlias("testEmptyUserName");
        loginPageObject.typeUrl("http://mobiledemo2.jaspersoft.com/jasperserver-pro");
        loginPageObject.typeUserName("");
        loginPageObject.clickLoginButton();
        loginPageObject.userNameMatches(hasErrorText("This field is required."));
    }

    @Test
    public void loginWithEmptyPassword() {
        loginPageObject.typeAlias("testEmptyPassword");
        loginPageObject.typeUrl("http://mobiledemo2.jaspersoft.com/jasperserver-pro");
        loginPageObject.typeUserName("joeuser");
        loginPageObject.typePassword("");
        loginPageObject.clickLoginButton();
        loginPageObject.passwordMatches(hasErrorText("This field is required."));
    }

    @Test
    public void loginWithSpacedPassword() {
        loginPageObject.typeAlias("testSpacedPassword");
        loginPageObject.typeUrl("http://mobiledemo2.jaspersoft.com/jasperserver-pro");
        loginPageObject.typeUserName("joeuser");
        loginPageObject.typePassword("");
        loginPageObject.clickLoginButton();
        loginPageObject.passwordMatches(hasErrorText("This field is required."));
    }

    @Test
    public void loginWithSpacedUserName() {
        loginPageObject.typeAlias("testSpacedUserName");
        loginPageObject.typeUrl("http://mobiledemo2.jaspersoft.com/jasperserver-pro");
        loginPageObject.typeUserName("     ");
        loginPageObject.clickLoginButton();
        loginPageObject.userNameMatches(hasErrorText("This field is required."));
    }
}
