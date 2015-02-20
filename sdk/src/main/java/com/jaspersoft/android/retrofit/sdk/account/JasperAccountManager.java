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
import android.accounts.AuthenticatorException;
import android.accounts.OnAccountsUpdateListener;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;
import timber.log.Timber;

/**
 * TODO provide unit tests
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class JasperAccountManager {
    private static final String PREF_NAME = JasperAccountManager.class.getSimpleName();
    private static final String ACCOUNT_NAME_KEY = "ACCOUNT_NAME_KEY";

    private final Context mContext;
    private final SharedPreferences mPreference;

    public static JasperAccountManager get(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context should not be null");
        }
        return new JasperAccountManager(context);
    }

    private JasperAccountManager(Context context) {
        mContext = context;
        mPreference = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        Timber.tag(PREF_NAME);
    }

    public Account getActiveAccount() {
        String accountName = mPreference.getString(ACCOUNT_NAME_KEY, "");
        if (TextUtils.isEmpty(accountName)) {
            return null;
        }
        return new Account(accountName, JasperSettings.JASPER_ACCOUNT_TYPE);
    }

    public void activateAccount(Account account) {
        AccountManager accountManager = AccountManager.get(mContext);
        String tokenToInvalidate = accountManager.peekAuthToken(account, JasperSettings.JASPER_AUTH_TOKEN_TYPE);
        accountManager.invalidateAuthToken(JasperSettings.JASPER_ACCOUNT_TYPE, tokenToInvalidate);
        mPreference.edit().putString(ACCOUNT_NAME_KEY, account.name).apply();
    }

    public void activateFirstAccount() {
        Account account = getAccounts()[0];
        AccountManager accountManager = AccountManager.get(mContext);
        String tokenToInvalidate = accountManager.peekAuthToken(account, JasperSettings.JASPER_AUTH_TOKEN_TYPE);
        accountManager.invalidateAuthToken(JasperSettings.JASPER_ACCOUNT_TYPE, tokenToInvalidate);
        mPreference.edit().putString(ACCOUNT_NAME_KEY, account.name).apply();
    }

    public void deactivateAccount() {
        mPreference.edit().putString(ACCOUNT_NAME_KEY, "").apply();
    }

    public List<AccountServerData> getInactiveAccountsData() {
        Account activeAccount = getActiveAccount();
        final String activeName = (activeAccount == null) ? "" : activeAccount.name;

        return Observable.from(getAccounts())
                .filter(new Func1<Account, Boolean>() {
                    @Override
                    public Boolean call(Account account) {
                        return !activeName.equals(account.name);
                    }
                }).map(new Func1<Account, AccountServerData>() {
                    @Override
                    public AccountServerData call(Account account) {
                        return AccountServerData.get(mContext, account);
                    }
                }).toList().toBlocking().first();
    }

    public void setOnAccountsUpdatedListener(OnAccountsUpdateListener listener){
        AccountManager.get(mContext).addOnAccountsUpdatedListener(listener, null, false);
    }

    public void removeOnAccountsUpdatedListener(OnAccountsUpdateListener listener){
        AccountManager.get(mContext).removeOnAccountsUpdatedListener(listener);
    }

    public Observable<AccountServerData> getActiveServerData() {
        Account activeAccount = getActiveAccount();
        return getServerData(activeAccount);
    }

    private Observable<AccountServerData> getServerData(Account account) {
        return getAuthToken(account).zipWith(Observable.just(account), new Func2<String, Account, AccountServerData>() {
            @Override
            public AccountServerData call(String cookie, Account account) {
                AccountServerData accountServerData = AccountServerData.get(mContext, account);
                accountServerData.setServerCookie(cookie);
                return accountServerData;
            }
        });
    }

    public Observable<String> getActiveAuthToken() {
        Account activeAccount = getActiveAccount();
        return getAuthToken(activeAccount);
    }

    /**
     * Retrieves token from {@link android.accounts.AccountManager} for specified {@link android.accounts.Account}.
     *
     * @param account which represents both JRS and user data configuration for more details refer to {@link com.jaspersoft.android.retrofit.sdk.account.AccountServerData}
     * @return token which in our case is cookie string for specified account. Can be <b>null</b> or empty if token is missing
     */
    private Observable<String> getAuthToken(final Account account) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    AccountManager accountManager = AccountManager.get(mContext);
                    AccountManagerFuture<Bundle> future = accountManager.getAuthToken(account,
                            JasperSettings.JASPER_AUTH_TOKEN_TYPE, null, true, null, null);
                    Bundle output = future.getResult();
                    if (output.containsKey(AccountManager.KEY_ERROR_MESSAGE)) {
                        subscriber.onError(new TokenNotReceivedException(output));
                        return;
                    }

                    String token = output.getString(AccountManager.KEY_AUTHTOKEN);
                    subscriber.onNext(token);
                    subscriber.onCompleted();
                } catch (AuthenticatorException ex) {
                    Timber.e(ex, "Failed to getAuthToken() AuthenticatorException");
                    subscriber.onError(ex);
                } catch (OperationCanceledException ex) {
                    Timber.e(ex, "Failed to getAuthToken() OperationCanceledException");
                    subscriber.onError(ex);
                } catch (IOException ex) {
                    Timber.e(ex, "Failed to getAuthToken() IOException");
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

    private Observable<Account> listFlatAccounts() {
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

    public Observable<Boolean> removeAccount(Account account) {
        return removeAccount(account, null);
    }

    private Observable<Boolean> removeAccount(final Account account, final Handler handler) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                AccountManager.get(mContext).removeAccount(account, new AccountManagerCallback<Boolean>() {
                    @Override
                    public void run(AccountManagerFuture<Boolean> future) {
                        try {
                            Account currentAccount = getActiveAccount();
                            if (currentAccount != null && currentAccount.name.equals(account.name)) {
                                Timber.d("Account removed from AccountProvider");
                                deactivateAccount();
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

    public static class AccountNotFoundException extends Throwable {
        public AccountNotFoundException() {
        }

        public AccountNotFoundException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static class TokenNotReceivedException extends Throwable {
        private final Bundle mOutput;

        public TokenNotReceivedException(Bundle output) {
            super(output.getString(AccountManager.KEY_ERROR_MESSAGE));
            mOutput = output;
        }

        public Bundle getOutput() {
            return mOutput;
        }
    }
}
