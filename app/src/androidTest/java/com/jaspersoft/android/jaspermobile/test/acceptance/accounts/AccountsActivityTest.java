package com.jaspersoft.android.jaspermobile.test.acceptance.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.support.test.espresso.DataInteraction;
import android.support.test.runner.AndroidJUnit4;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.account.AccountsActivity_;
import com.jaspersoft.android.jaspermobile.test.junit.ActivityRule;
import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@RunWith(AndroidJUnit4.class)
public class AccountsActivityTest {
    private static final String TEST_NAME = "Test user";
    private static final String TEST_URL = "http://example.com";

    @Rule
    public final ActivityRule<AccountsActivity_> activityRule =
            ActivityRule.create(AccountsActivity_.class);

    @Before
    public void before() {
        removeAccountOnDemand();
        createTestAccount();

        assertThat(activityRule.instrumentation(), notNullValue());
        assertThat(activityRule.get(), notNullValue());
    }

    @After
    public void after() {
        activityRule.get().finish();
        removeAccountOnDemand();
    }

    @Test
    public void testInitialLoad() {
        DataInteraction firsItem = onData(Matchers.is(instanceOf(Account.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0);
        firsItem.onChildView(withId(android.R.id.text1)).check(matches(withText(TEST_NAME)));
        firsItem.onChildView(withId(android.R.id.text2)).check(matches(withText(TEST_URL)));
    }

    @Test
    public void testDeleteActionShouldRemoveAccount() {
        onData(Matchers.is(instanceOf(Account.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(longClick());
        onView(withId(R.id.deleteItem)).perform(click());

        onOverflowView(activityRule.get(), withText(R.string.spm_delete_btn)).perform(click());

        onView(withId(android.R.id.empty)).check(matches(withText(R.string.no_accounts)));
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void createTestAccount() {
        AccountManager accountManager = AccountManager.get(activityRule.getApplicationContext());
        AccountServerData serverData = new AccountServerData()
                .setUsername(TEST_NAME)
                .setServerUrl(TEST_URL);
        Account account = new Account("test", JasperSettings.JASPER_ACCOUNT_TYPE);
        assertTrue(accountManager.addAccountExplicitly(account, "1234", serverData.toBundle()));
    }

    private void removeAccountOnDemand() {
        AccountManagerUtil managerUtil = AccountManagerUtil
                .get(activityRule.getApplicationContext());
        if (managerUtil.getAccounts().length > 0) {
            managerUtil.removeAccounts().toBlocking().first();
        }
    }

}
