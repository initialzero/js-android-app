/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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
import android.widget.Toast;

import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;

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
            JasperAccountManager.get(context).invalidateActiveToken();
            Toast.makeText(context, "Cookies removed", Toast.LENGTH_LONG).show();
        } else if (action.equals(REMOVE_ALL_ACCOUNTS)) {
            Account[] accounts = JasperAccountManager.get(context).getAccounts();
            AccountManager accountManager = AccountManager.get(context);
            for (Account account : accounts) {
                accountManager.removeAccount(account, null, null);
            }
            Toast.makeText(context, "Accounts removed", Toast.LENGTH_LONG).show();
        }
    }
}
