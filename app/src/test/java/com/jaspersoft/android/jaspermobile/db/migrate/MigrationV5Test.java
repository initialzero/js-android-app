package com.jaspersoft.android.jaspermobile.db.migrate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.multidex.ShadowMultiDex;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21, shadows = {ShadowMultiDex.class})
public class MigrationV5Test {
    private static final String JASPER_ACCOUNT_TYPE = "com.jaspersoft";
    private static final String JASPER_AUTH_TOKEN_TYPE = "FULL ACCESS";

    private MigrationV5 migrationV5;
    private AccountManager accountManager;

    @Before
    public void setUp() throws Exception {
        migrationV5 = new MigrationV5(RuntimeEnvironment.application);
        accountManager = AccountManager.get(RuntimeEnvironment.application);
    }

    @Test
    public void testMigrate() throws Exception {
        Account account1 = addAccount("fake1", "token1");
        Account account2 = addAccount("fake2", "token2");

        assertThat("Failed precondition account token should be set",
                accountManager.peekAuthToken(account1, JASPER_AUTH_TOKEN_TYPE),
                is("token1"));
        assertThat("Failed precondition account token should be set",
                accountManager.peekAuthToken(account2, JASPER_AUTH_TOKEN_TYPE),
                is("token2"));
        migrationV5.migrate(null);

        assertThat("Failed migration. Account token should be null",
                accountManager.peekAuthToken(account1, JASPER_AUTH_TOKEN_TYPE),
                is(nullValue()));
        assertThat("Failed migration. Account token should be null",
                accountManager.peekAuthToken(account2, JASPER_AUTH_TOKEN_TYPE),
                is(nullValue()));
    }

    @NonNull
    private Account addAccount(String name, String token) {
        Account account = new Account(name, JASPER_ACCOUNT_TYPE);
        accountManager.addAccountExplicitly(account, null, Bundle.EMPTY);
        accountManager.setAuthToken(account, JASPER_AUTH_TOKEN_TYPE, token);
        return account;
    }
}