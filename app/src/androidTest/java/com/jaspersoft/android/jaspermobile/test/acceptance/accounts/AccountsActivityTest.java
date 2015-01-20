package com.jaspersoft.android.jaspermobile.test.acceptance.accounts;

import android.accounts.Account;
import android.support.test.espresso.DataInteraction;
import android.support.test.runner.AndroidJUnit4;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.account.AccountsActivity_;
import com.jaspersoft.android.jaspermobile.test.junit.ActivityRule;
import com.jaspersoft.android.jaspermobile.test.junit.WebMockRule;
import com.jaspersoft.android.jaspermobile.test.utils.AccountUtil;
import com.jaspersoft.android.jaspermobile.test.utils.TestResource;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.internal.matchers.StringContains.containsString;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@RunWith(AndroidJUnit4.class)
public class AccountsActivityTest {
    private static final String TEST_NAME = "Test user";
    private static final String TEST_URL = "http://example.com";

    @Rule
    public WebMockRule webMockRule = new WebMockRule();

    @Rule
    public final ActivityRule<AccountsActivity_> activityRule =
            ActivityRule.create(AccountsActivity_.class);

    @After
    public void after() {
        activityRule.get().finish();
        removeAccountOnDemand();
    }

    @Test
    public void testInitialLoad() {
        createTestAccount();
        activityRule.saveStart();

        DataInteraction firsItem = onData(is(instanceOf(Account.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0);
        firsItem.onChildView(withId(android.R.id.text1)).check(matches(withText(TEST_NAME)));
        firsItem.onChildView(withId(android.R.id.text2)).check(matches(withText(TEST_URL)));
    }

    @Test
    public void testDeleteActionShouldRemoveAccount() {
        createTestAccount();
        activityRule.saveStart();

        onData(is(instanceOf(Account.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.deleteItem)).perform(click());

        onOverflowView(activityRule.get(), withText(R.string.spm_delete_btn)).perform(click());

        onView(withId(android.R.id.empty)).check(matches(withText(R.string.no_accounts)));
    }

    @Test
    public void testHomeAsUpDisplayHomePage() {
        activityRule.saveStart();
        onView(withId(android.R.id.home)).perform(click());
        onView(withText(R.string.app_label)).check(matches(isDisplayed()));
    }

    @Test
    public void testAliasShouldBeUnique() {
        AccountServerData data = new AccountServerData()
                .setAlias(AccountServerData.Demo.ALIAS)
                .setUsername(AccountServerData.Demo.USERNAME)
                .setPassword(AccountServerData.Demo.PASSWORD)
                .setServerUrl(AccountServerData.Demo.SERVER_URL);
        AccountUtil.get(activityRule.getApplicationContext())
                .removeAllAccounts()
                .addAccount(data)
                .setAuthToken()
                .activate();
        activityRule.saveStart();

        onView(withId(R.id.addAccount)).perform(click());

        onView(withId(R.id.aliasEdit)).perform(typeText(AccountServerData.Demo.ALIAS));
        onView(withId(R.id.usernameEdit)).perform(scrollTo());
        onView(withId(R.id.usernameEdit)).perform(typeText(AccountServerData.Demo.USERNAME));
        onView(withId(R.id.serverUrlEdit)).perform(scrollTo());
        onView(withId(R.id.serverUrlEdit)).perform(typeText(webMockRule.getEndpoint()));
        onView(withId(R.id.passwordEdit)).perform(scrollTo());
        onView(withId(R.id.passwordEdit)).perform(typeText(AccountServerData.Demo.PASSWORD));

        onView(withId(R.id.addAccount)).perform(scrollTo());
        onView(withId(R.id.addAccount)).perform(click());
        onView(withId(R.id.aliasEdit)).check(matches(hasErrorText(R.string.sp_error_duplicate_alias)));
    }

    @Test
    public void testAddAccountItemAltersPageState() {
        mockLoginAction();
        createTestAccount();
        activityRule.saveStart();

        // Given user opens add account page
        onView(withId(R.id.addAccount)).perform(click());

        // When user adds account
        onView(withId(R.id.serverUrlEdit)).perform(typeText(webMockRule.getEndpoint()));
        onView(withId(R.id.organizationEdit)).perform(typeText(AccountServerData.Demo.ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(scrollTo());
        onView(withId(R.id.usernameEdit)).perform(typeText(AccountServerData.Demo.USERNAME));
        onView(withId(R.id.passwordEdit)).perform(scrollTo());
        onView(withId(R.id.passwordEdit)).perform(typeText(AccountServerData.Demo.PASSWORD));
        onView(withId(R.id.addAccount)).perform(scrollTo());
        onView(withId(R.id.addAccount)).perform(click());

        // Then he should see new account
        DataInteraction firsItem = onData(is(instanceOf(Account.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(1);
        firsItem.onChildView(withId(android.R.id.text1)).check(matches(withText(AccountServerData.Demo.USERNAME)));
        firsItem.onChildView(withId(android.R.id.text2)).check(matches(withText(containsString(webMockRule.getEndpoint()))));
    }

    @Test
    public void testClickOnItemListActivatesAccount() {
        mockLoginAction();
        AccountServerData data = new AccountServerData()
                .setUsername("name1")
                .setPassword("password")
                .setServerUrl(webMockRule.getEndpoint());
        AccountServerData data1 = new AccountServerData()
                .setUsername("name2")
                .setPassword("password")
                .setServerUrl(webMockRule.getEndpoint());
        AccountUtil.get(activityRule.getApplicationContext())
                .removeAllAccounts()
                .addAccount(data)
                .setAuthToken()
                .getUtil()
                .addAccount(data1)
                .setAuthToken()
                .activate();
        activityRule.saveStart();

        onData(is(instanceOf(Account.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        Account account = BasicAccountProvider
                .get(activityRule.getApplicationContext())
                .getAccount();
        assertThat(account.name, is("name1"));
    }

    //---------------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------------

    private void createTestAccount() {
        AccountServerData serverData = new AccountServerData()
                .setUsername(TEST_NAME)
                .setServerUrl(TEST_URL);
        AccountUtil.get(activityRule.getApplicationContext())
                .removeAllAccounts()
                .addAccount(serverData)
                .setAuthToken()
                .activate();
    }

    private void removeAccountOnDemand() {
        AccountUtil.get(activityRule.getApplicationContext()).removeAllAccounts();
    }

    private void mockLoginAction() {
        MockResponse authResponse = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Set-Cookie", "JSESSIONID=4202A2DF42507EDEC7A66A1348C62195; Path=/jasperserver-pro/; HttpOnly")
                .addHeader("Set-Cookie", "userLocale=en_US;Expires=Thu, 15-Jan-2015 12:15:36 GMT;HttpOnly")
                .throttleBody(Integer.MAX_VALUE, 1, TimeUnit.MILLISECONDS)
                .setBody("{}");
        MockResponse mobileDemoServerResponse = authResponse.clone()
                .setBody(TestResource.getJson().rawData("mobile_demo"));

        webMockRule.get().enqueue(authResponse);
        webMockRule.get().enqueue(mobileDemoServerResponse);
    }

}
