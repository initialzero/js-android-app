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
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.util.JasperSettings;

import java.net.CookieManager;
import java.net.CookieStore;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class UtilReceiver extends BroadcastReceiver {
    private static final String REMOVE_COOKIES = "jaspermobile.util.action.REMOVE_COOKIES";
    private static final String REMOVE_ALL_ACCOUNTS = "jaspermobile.util.action.REMOVE_ALL_ACCOUNTS";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(REMOVE_COOKIES)) {
            deleteToken();
            showMessage(context, "Cookies removed");
        } else if (action.equals(REMOVE_ALL_ACCOUNTS)) {
            removeAccounts(context);
            showMessage(context, "Accounts removed");
        }
    }

    private void deleteToken() {
        getCookieStore().removeAll();
    }

    private CookieStore getCookieStore() {
        CookieManager manager = (CookieManager) CookieManager.getDefault();
        return manager.getCookieStore();
    }

    private void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void removeAccounts(Context context) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType(JasperSettings.JASPER_ACCOUNT_TYPE);
        for (Account account : accounts) {
            manager.removeAccountExplicitly(account);
        }
    }
}