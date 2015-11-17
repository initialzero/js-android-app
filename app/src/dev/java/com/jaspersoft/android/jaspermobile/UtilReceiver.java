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

package com.jaspersoft.android.jaspermobile;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import java.lang.reflect.Type;
import java.util.List;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.functions.Func1;

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
    private static final String LOAD_PROFILES = "jaspermobile.util.action.LOAD_PROFILES";

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
        } else if (action.equals(LOAD_PROFILES)) {
            loadProfiles(context, intent);
        }
    }

    private void deleteToken(Context context) {
        JasperAccountManager.get(context).invalidateActiveToken();
        Toast.makeText(context, "Cookies removed", Toast.LENGTH_SHORT).show();
    }

    private void overrideTokenWithOldOne(Context context) {
        Account account = JasperAccountManager.get(context).getActiveAccount();
        if (account != null) {
            AccountManager accountManager = AccountManager.get(context);
            accountManager.setAuthToken(account, JasperSettings.JASPER_AUTH_TOKEN_TYPE, INVALID_COOKIE);
            Toast.makeText(context, "Cookie was deprecated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "No active account. Nothing to deprecate.", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeAccounts(Context context) {
        Account[] accounts = JasperAccountManager.get(context).getAccounts();
        AccountManager accountManager = AccountManager.get(context);
        for (Account account : accounts) {
            accountManager.removeAccount(account, null, null);
        }
        Toast.makeText(context, "Accounts removed", Toast.LENGTH_SHORT).show();
    }

    private void downgradeServerVersion(Context context, Intent intent) {
        Account account = JasperAccountManager.get(context).getActiveAccount();
        if (account == null) {
            Toast.makeText(context, "No active account. Nothing to downgrade.", Toast.LENGTH_SHORT).show();
        } else {
            String versionName = intent.getStringExtra("target_version");
            if (TextUtils.isEmpty(versionName)) {
                Toast.makeText(context, "Target version missing. Can't downgrade.", Toast.LENGTH_SHORT).show();
            } else {
                AccountManager accountManager = AccountManager.get(context);
                accountManager.setUserData(account, AccountServerData.VERSION_NAME_KEY, versionName);
                Toast.makeText(context, "Server was downgraded to version: " + versionName, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void changeServerVersion(Context context, Intent intent) {
        Account account = JasperAccountManager.get(context).getActiveAccount();
        if (account == null) {
            Toast.makeText(context, "No active account. No way to change edition.", Toast.LENGTH_SHORT).show();
        } else {
            String editionName = intent.getStringExtra("edition_version");
            if (TextUtils.isEmpty(editionName)) {
                Toast.makeText(context, "Target server edition not found. Can't update server.", Toast.LENGTH_SHORT).show();
            } else {
                AccountManager accountManager = AccountManager.get(context);
                accountManager.setUserData(account, AccountServerData.EDITION_KEY, editionName);
                Toast.makeText(context, "Server edition was changed: " + editionName, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadProfiles(final Context context, Intent intent) {
        String json  = intent.getStringExtra("source_json");
        if (TextUtils.isEmpty(json)) {
            Toast.makeText(context, "Source json is missing", Toast.LENGTH_SHORT).show();
        } else {
            populateProfiles(context, json);
        }
    }

    private void populateProfiles(final Context context, String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<AccountServerData>>() {
        }.getType();

        final JasperAccountManager accountManager = JasperAccountManager.get(context);
        List<AccountServerData> datum = gson.fromJson(json, listType);
        Observable.from(datum).flatMap(new Func1<AccountServerData, Observable<Account>>() {
            @Override
            public Observable<Account> call(AccountServerData serverData) {
                return accountManager.addAccountExplicitly(serverData);
            }
        }).subscribe(Actions.empty(), new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Toast.makeText(context, "Failed to add profile: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, new Action0() {
            @Override
            public void call() {
                Toast.makeText(context, "Profiles loaded.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
