/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.test.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import rx.functions.Actions;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public final class AccountUtil {

    private final Context mContext;

    private AccountUtil(Context context) {
        mContext = context;
    }

    public static AccountUtil get(Context context) {
        return new AccountUtil(context);
    }

    public AccountUtil removeAllAccounts() {
        AccountManagerUtil managerUtil = AccountManagerUtil.get(mContext);
        if (managerUtil.getAccounts().length > 0) {
            managerUtil.removeAccounts().toBlocking().forEach(Actions.empty());
        }
        BasicAccountProvider.get(mContext).removeAccount();
        return this;
    }

    public AccountUnit addAccount(AccountServerData serverData) {
        AccountManagerUtil accountManager = AccountManagerUtil.get(mContext);
        Account account = accountManager.addAccountExplicitly(serverData).toBlocking().first();
        return new AccountUnit(account);
    }

    public class AccountUnit {
        private final Account mAccount;

        public AccountUnit(Account account) {
            mAccount = account;
        }

        public AccountUnit setAuthToken() {
            AccountManager accountManager = AccountManager.get(mContext);
            accountManager.setAuthToken(mAccount, JasperSettings.JASPER_AUTH_TOKEN_TYPE, "token");
            return this;
        }

        public AccountUnit activate() {
            BasicAccountProvider.get(mContext).putAccount(mAccount);
            return this;
        }

        public AccountUtil getUtil() {
            return AccountUtil.this;
        }
    }
}
