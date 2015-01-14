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

package com.jaspersoft.android.jaspermobile.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.activities.auth.AuthenticatorActivity;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountDataStorage;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EActivity
public class StartUpActivity extends Activity {
    private static final int AUTHORIZE = 10;
    private final Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        signInOrCreateAnAccount();
    }

    private void signInOrCreateAnAccount() {
        //Get list of accounts on device.
        AccountManager am = AccountManager.get(this);
        Account[] accountArray = am.getAccountsByType(JasperSettings.JASPER_ACCOUNT_TYPE);
        if (accountArray.length == 0) {
            addAccount();
        } else {
            reauthorize(accountArray[0]);

//            finish();
//            HomeActivity_.intent(this).start();
        }
    }

    private void reauthorize(Account account) {
        AccountManager am = AccountManager.get(this);
        String authtoken = BasicAccountDataStorage.get(this).getServerCookie();
        am.invalidateAuthToken(account.type, authtoken);
        am.removeAccount(account, new AccountManagerCallback<Boolean>() {
            @Override
            public void run(AccountManagerFuture<Boolean> future) {
                try {
                    Boolean result = future.getResult();
                    if (result) {
                        addAccount();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, mHandler);
    }

    private void addAccount() {
        Intent intent = new Intent(this, AuthenticatorActivity.class);
        intent.putExtra("account_types", new String[]{"com.jaspersoft"});
        startActivityForResult(intent, AUTHORIZE);
    }

    @OnActivityResult(AUTHORIZE)
    protected void onAuthorize(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            // do something
        } else {
            finish();
        }
    }
}
