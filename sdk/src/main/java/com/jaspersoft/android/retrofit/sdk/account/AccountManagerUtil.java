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
import android.os.Bundle;
import android.os.Handler;

import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * TODO provide unit tests
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class AccountManagerUtil {
    private static final String TAG = AccountManagerUtil.class.getSimpleName();

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
        Timber.tag(TAG);
    }

    public Observable<Account> getActiveAccount() {
        Account account = BasicAccountProvider.get(mContext).getAccount();
        if (account == null) {
            return Observable.error(new AccountNotFoundException("There is no active account"));
        }
        return Observable.just(account);
    }

    public List<AccountServerData> getAccountServers(boolean withoutActive) {
        List<AccountServerData> mJasperAccounts = new ArrayList<AccountServerData>();
        Account activeAccount = BasicAccountProvider.get(mContext).getAccount();
        Account[] accounts = getAccounts();
        for (Account jasperAccount : accounts) {
            if(!withoutActive || !(activeAccount != null && activeAccount.equals(jasperAccount)))
                mJasperAccounts.add(AccountServerData.get(mContext, jasperAccount));
        }
        return mJasperAccounts;
    }

    /**
     * Activates account by requesting auth token for new account invalidating it, if exists, and requesting new one.
     *
     * @param newAccount Account we are requesting token for. As soon as, token received we are
     *                   persisting within {@link com.jaspersoft.android.retrofit.sdk.account.AccountProvider}
     * @return observable which wraps account for activating
     */
    public Observable<Account> activateAccount(final Account newAccount) {
        return getAuthToken(newAccount)
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String tokenToInvalidate) {
                        AccountManager accountManager = AccountManager.get(mContext);
                        accountManager.invalidateAuthToken(JasperSettings.JASPER_ACCOUNT_TYPE, tokenToInvalidate);
                        return getAuthToken(newAccount);
                    }
                }).flatMap(new Func1<String, Observable<Account>>() {
                    @Override
                    public Observable<Account> call(String newToken) {
                        BasicAccountProvider.get(mContext).putAccount(newAccount);
                        return Observable.just(newAccount);
                    }
                });
    }

    public Observable<String> getActiveAuthToken() {
       return getActiveAccount().flatMap(new Func1<Account, Observable<String>>() {
           @Override
           public Observable<String> call(Account account) {
               return getAuthToken(account);
           }
       });
    }

    public Observable<String> getAuthToken(final Account account) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    AccountManager accountManager = AccountManager.get(mContext);
                    AccountManagerFuture<Bundle> future = accountManager.getAuthToken(account,
                            JasperSettings.JASPER_AUTH_TOKEN_TYPE, null, true, null, null);
                    Bundle bundle = future.getResult();
                    String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    subscriber.onNext(token);
                    subscriber.onCompleted();
                } catch (Exception ex) {
                    Timber.e(ex, "Failed to getAuthToken()");
                    subscriber.onError(ex);
                }
            }
        });
    }

    public Account[] getAccounts() {
        Account[] accounts = AccountManager.get(mContext).getAccountsByType(JasperSettings.JASPER_ACCOUNT_TYPE);
        Timber.d(Arrays.toString(accounts));
        return accounts;
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
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                AccountManager.get(mContext).removeAccount(account, new AccountManagerCallback<Boolean>() {
                    @Override
                    public void run(AccountManagerFuture<Boolean> future) {
                        try {
                            Account currentAccount = mAccountProvider.getAccount();
                            if (currentAccount != null && currentAccount.name.equals(account.name)) {
                                Timber.d("Account removed from AccountProvider");
                                mAccountProvider.removeAccount();
                            }
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

    public Observable<Account> addAccountExplicitly(final AccountServerData serverData) {
        return Observable.create(new Observable.OnSubscribe<Account>() {
            @Override
            public void call(Subscriber<? super Account> subscriber) {
                try {
                    AccountManager accountManager = AccountManager.get(mContext);
                    Account account = new Account(serverData.getAlias(),
                            JasperSettings.JASPER_ACCOUNT_TYPE);
                    accountManager.addAccountExplicitly(account,
                            serverData.getPassword(), serverData.toBundle());
                    subscriber.onNext(account);
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

    public static class AccountNotFoundException extends Throwable {
        public AccountNotFoundException() {
        }

        public AccountNotFoundException(String detailMessage) {
            super(detailMessage);
        }
    }
}
