/*
 * Copyright (c) 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class UtilReceiver extends BroadcastReceiver {
    private static final String REMOVE_COOKIES = "jaspermobile.util.action.REMOVE_COOKIES";
    private static final String DEPRECATE_COOKIES = "jaspermobile.util.action.DEPRECATE_COOKIES";
    private static final String REMOVE_ALL_ACCOUNTS = "jaspermobile.util.action.REMOVE_ALL_ACCOUNTS";
    private static final String DOWNGRADE_SERVER_VERSION = "jaspermobile.util.action.DOWNGRADE_SERVER_VERSION";
    private static final String CHANGE_SERVER_EDITION = "jaspermobile.util.action.CHANGE_SERVER_EDITION";

    private static final String INVALID_COOKIE = "JSESSIONID=5513E1DE5437AE6B9F41CC5C8309B153; " +
            "Path=/jasperserver-pro/; HttpOnlyuserLocale=en_US;Expires=Sat, 23-May-2015 09:15:46 GMT;HttpOnly";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(REMOVE_COOKIES)) {
            deleteToken(context);
        } else if (action.equals(DEPRECATE_COOKIES)) {
            overrideTokenWithOldOne(context);
        } else if (action.equals(REMOVE_ALL_ACCOUNTS)) {
            removeAccounts(context);
        } else if (action.equals(DOWNGRADE_SERVER_VERSION)) {
            downgradeServerVersion(context, intent);
            overrideTokenWithOldOne(context);
        } else if (action.equals(CHANGE_SERVER_EDITION)) {
            changeServerVersion(context, intent);
            overrideTokenWithOldOne(context);
        }
    }

    private void deleteToken(Context context) {
        JasperAccountManager.get(context).invalidateActiveToken();
        Toast.makeText(context, "Cookies removed", Toast.LENGTH_LONG).show();
    }

    private void overrideTokenWithOldOne(Context context) {
        Account account = JasperAccountManager.get(context).getActiveAccount();
        if (account != null) {
            AccountManager accountManager = AccountManager.get(context);
            accountManager.setAuthToken(account, JasperSettings.JASPER_AUTH_TOKEN_TYPE, INVALID_COOKIE);
            Toast.makeText(context, "Cookie was deprecated", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "No active account. Nothing to deprecate.", Toast.LENGTH_LONG).show();
        }
    }

    private void removeAccounts(Context context) {
        Account[] accounts = JasperAccountManager.get(context).getAccounts();
        AccountManager accountManager = AccountManager.get(context);
        for (Account account : accounts) {
            accountManager.removeAccount(account, null, null);
        }
        Toast.makeText(context, "Accounts removed", Toast.LENGTH_LONG).show();
    }

    private void downgradeServerVersion(Context context, Intent intent) {
        Account account = JasperAccountManager.get(context).getActiveAccount();
        if (account == null) {
            Toast.makeText(context, "No active account. Nothing to downgrade.", Toast.LENGTH_LONG).show();
        } else {
            String versionName = intent.getStringExtra("target_version");
            if (TextUtils.isEmpty(versionName)) {
                Toast.makeText(context, "Target version missing. Can't downgrade.", Toast.LENGTH_LONG).show();
            } else {
                AccountManager accountManager = AccountManager.get(context);
                accountManager.setUserData(account, AccountServerData.VERSION_NAME_KEY, versionName);
                Toast.makeText(context, "Server was downgraded to version: " + versionName, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void changeServerVersion(Context context, Intent intent) {
        Account account = JasperAccountManager.get(context).getActiveAccount();
        if (account == null) {
            Toast.makeText(context, "No active account. No way to change edition.", Toast.LENGTH_LONG).show();
        } else {
            String editionName = intent.getStringExtra("edition_version");
            if (TextUtils.isEmpty(editionName)) {
                Toast.makeText(context, "Target server edition not found. Can't update server.", Toast.LENGTH_LONG).show();
            } else {
                AccountManager accountManager = AccountManager.get(context);
                accountManager.setUserData(account, AccountServerData.EDITION_KEY, editionName);
                Toast.makeText(context, "Server edition was changed: " + editionName, Toast.LENGTH_LONG).show();
            }
        }
    }
}
