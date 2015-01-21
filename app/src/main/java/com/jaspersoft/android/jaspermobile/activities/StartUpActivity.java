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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.activities.account.AccountsActivity_;
import com.jaspersoft.android.jaspermobile.activities.auth.AuthenticatorActivity;
import com.jaspersoft.android.jaspermobile.db.MobileDbProvider;
import com.jaspersoft.android.jaspermobile.legacy.ProfileManager;
import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;

import roboguice.activity.RoboActivity;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EActivity
public class StartUpActivity extends RoboActivity {
    private static final int AUTHORIZE = 10;

    private final CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Inject
    protected JsRestClient jsRestClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signInOrCreateAnAccount();

        // Force database update
        getContentResolver().query(MobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                new String[]{"_ID"}, null, null, null);
    }

    private void signInOrCreateAnAccount() {
        final Context context = this;
        compose(
                AppObservable.bindActivity(this,
                        AccountManagerUtil.get(this)
                                .getActiveAccount()
                ).subscribe(
                        new Action1<Account>() {
                            @Override
                            public void call(Account account) {
                                ProfileManager.initLegacyJsRestClient(context, account, jsRestClient);
                                DrawerActivity_.intent(context).start();
                                finish();
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                recoverMissingAccountState();
                            }
                        })
        );
    }

    private void recoverMissingAccountState() {
        Account[] accounts = AccountManagerUtil.get(this).getAccounts();
        if (accounts.length == 0) {
            addAccount();
        } else {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(AccountsActivity_.class);
            stackBuilder.addNextIntent(AccountsActivity_.intent(this).get());
            stackBuilder.startActivities();
            finish();
        }
    }

    private void compose(Subscription subscription) {
        compositeSubscription.add(subscription);
    }

    private void addAccount() {
        Intent intent = new Intent(this, AuthenticatorActivity.class);
        intent.putExtra("account_types", new String[]{"com.jaspersoft"});
        startActivityForResult(intent, AUTHORIZE);
    }

    @OnActivityResult(AUTHORIZE)
    protected void onAuthorize(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            HomeActivity_.intent(this).start();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        compositeSubscription.unsubscribe();
        super.onDestroy();
    }
}
