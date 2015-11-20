/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;

import org.robolectric.RuntimeEnvironment;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class FakeAccount {
    public static final String ACCOUNT_TYPE = "com.jaspersoft";

    private final Account mAccount;

    public FakeAccount(Account account) {
        mAccount = account;
    }

    public Account get() {
        return mAccount;
    }

    public static SetupCredentialsBuilder injectAccount(Profile profile) {
        return new SetupAccountBuilder(RuntimeEnvironment.application).injectAccount(profile);
    }

    public static class SetupAccountBuilder {
        private final Context mContext;

        private SetupAccountBuilder(Context context) {
            mContext = context;
        }

        public SetupCredentialsBuilder injectAccount(Profile profile) {
            Account account = new Account(profile.getKey(), ACCOUNT_TYPE);
            AccountManager accountManager = AccountManager.get(mContext);

            assertThat("Failed precondition. Can not create account for profile: " + profile,
                    accountManager.addAccountExplicitly(account, null, null)
            );
            assertThat("Fake account should be registered in system",
                    accountManager.getAccountsByType(ACCOUNT_TYPE).length > 0
            );
            return new SetupCredentialsBuilder(mContext, account);
        }
    }

    public static class SetupCredentialsBuilder {
        private final Context mContext;
        private final Account mAccount;

        private SetupCredentialsBuilder(Context context, Account account) {
            mContext = context;
            mAccount = account;
        }

        public SetupCredentialsBuilder injectCredentials(BaseCredentials credentials) {
            AccountManager accountManager = AccountManager.get(mContext);
            accountManager.setUserData(mAccount, "ORGANIZATION_KEY", credentials.getOrganization());
            accountManager.setUserData(mAccount, "USERNAME_KEY", credentials.getUsername());
            accountManager.setPassword(mAccount, credentials.getPassword());
            return this;
        }

        public SetupCredentialsBuilder injectServer(JasperServer fakeServer) {
            AccountManager accountManager = AccountManager.get(mContext);
            accountManager.setUserData(mAccount, "SERVER_URL_KEY", fakeServer.getBaseUrl());
            accountManager.setUserData(mAccount, "EDITION_KEY", fakeServer.getEdition());
            accountManager.setUserData(mAccount, "VERSION_NAME_KEY", String.valueOf(fakeServer.getVersion()));
            return this;
        }

        public FakeAccount done() {
            return new FakeAccount(mAccount);
        }
    }
}
