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
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.os.Handler;

import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Actions;
import timber.log.Timber;

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
        JasperAccountManager managerUtil = JasperAccountManager.get(mContext);
        Account[] accounts = managerUtil.getAccounts();
        if (accounts.length > 0) {
            for (Account account : accounts) {
                removeAccount(account, null).toBlocking().forEach(Actions.empty());
            }
        }
        JasperAccountManager.get(mContext).deactivateAccount();
        return this;
    }

    private Observable<Boolean> removeAccount(final Account account, final Handler handler) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                AccountManager.get(mContext).removeAccount(account, new AccountManagerCallback<Boolean>() {
                    @Override
                    public void run(AccountManagerFuture<Boolean> future) {
                        try {
                            Boolean result = future.getResult();
                            Timber.d("Remove status for Account[" + account.name + "]: " + result);
                            subscriber.onNext(result);
                            subscriber.onCompleted();
                        } catch (Exception ex) {
                            Timber.e(ex, "Failed to removeAccount()");
                            subscriber.onError(ex);
                        }
                    }
                }, handler);
            }
        });
    }

    public AccountUnit addAccount(AccountServerData serverData) {
        JasperAccountManager accountManager = JasperAccountManager.get(mContext);
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
            JasperAccountManager.get(mContext).activateAccount(mAccount);
            return this;
        }

        public Account getAccount() {
            return mAccount;
        }

        public AccountUtil getUtil() {
            return AccountUtil.this;
        }
    }
}
