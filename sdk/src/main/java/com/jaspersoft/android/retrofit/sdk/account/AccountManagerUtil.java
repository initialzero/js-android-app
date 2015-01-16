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
package com.jaspersoft.android.retrofit.sdk.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.os.Handler;

import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * TODO provide unit tests
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class AccountManagerUtil {
    private final Context mContext;
    private final AccountProvider mAccountProvider;

    public static AccountManagerUtil get(Context context) {
        return new Builder(context).build();
    }

    public static Builder builder(Context context) {
        return new Builder(context);
    }

    private AccountManagerUtil(Context context, AccountProvider accountProvider) {
        mContext = context;
        mAccountProvider = accountProvider;
    }

    public Account[] getAccounts() {
       return AccountManager.get(mContext).getAccountsByType(JasperSettings.JASPER_ACCOUNT_TYPE);
    }

    public Observable<List<Account>> listAccounts() {
        List<Account> accounts = new ArrayList<Account>();
        Collections.addAll(accounts, getAccounts());
        return Observable.just(accounts);
    }

    public Observable<Account> listFlatAccounts() {
        return listAccounts()
                .flatMap(
                        new Func1<List<Account>, Observable<Account>>() {
                            @Override
                            public Observable<Account> call(List<Account> accounts) {
                                return Observable.from(accounts);
                            }
                        });
    }

    public Observable<Boolean> removeAccounts() {
        return listFlatAccounts()
                .flatMap(new Func1<Account, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Account account) {
                        return removeAccount(account);
                    }
                });
    }

    public Observable<Boolean> removeAccount() {
        return removeAccount(mAccountProvider.getAccount(), null);
    }

    public Observable<Boolean> removeAccount(Account account) {
        return removeAccount(account, null);
    }

    public Observable<Boolean> removeAccount(final Account account, final Handler handler) {
        final AccountManager am = AccountManager.get(mContext);
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                am.removeAccount(account, new AccountManagerCallback<Boolean>() {
                    @Override
                    public void run(AccountManagerFuture<Boolean> future) {
                        try {
                            Boolean result = future.getResult();
                            subscriber.onNext(result);
                            subscriber.onCompleted();
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                    }
                }, handler);
            }
        });
    }

    public Observable<Boolean> addAccountExplicitly(final AccountServerData serverData) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    AccountManager accountManager = AccountManager.get(mContext);
                    Account account = mAccountProvider
                            .putAccountName(serverData.getUsername())
                            .getAccount();

                    boolean result = accountManager.addAccountExplicitly(account,
                            serverData.getPassword(), serverData.toBundle());
                    subscriber.onNext(result);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static class Builder {
        private final Context mContext;
        private AccountProvider accountProvider;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public Builder accountProvider(AccountProvider accountProvider) {
            this.accountProvider = accountProvider;
            return this;
        }

        public AccountManagerUtil build() {
            if (mContext == null) {
                throw new IllegalArgumentException("Context should not be null");
            }
            ensureSaneDefaults();
            return new AccountManagerUtil(mContext, accountProvider);
        }

        private void ensureSaneDefaults() {
            if (accountProvider == null) {
                accountProvider = BasicAccountProvider.get(mContext);
            }
        }
    }
}
