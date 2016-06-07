/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.test.support;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.jaspersoft.android.jaspermobile.util.JasperSettings;

import java.io.IOException;

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
        removeAllJasperAccounts();
        deactivateAccount();
        return this;
    }

    private void removeAllJasperAccounts() {
        AccountManager managerUtil = AccountManager.get(mContext);
        Account[] accounts = managerUtil.getAccountsByType(JasperSettings.JASPER_ACCOUNT_TYPE);
        if (accounts.length > 0) {
            for (Account account : accounts) {
                AccountManagerFuture<Boolean> result = AccountManager.get(mContext).removeAccount(account, null, null);
                try {
                    result.getResult();
                } catch (OperationCanceledException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (AuthenticatorException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void deactivateAccount() {
        SharedPreferences pref = mContext.getSharedPreferences("JasperAccountManager", Activity.MODE_PRIVATE);
        pref.edit().clear().apply();
    }

}
