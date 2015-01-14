package com.jaspersoft.android.retrofit.sdk.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.os.Handler;

import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * TODO provide unit tests
 *
 * @author Tom Koptel
 * @since 1.9
 */
public class AccountManagerUtil {
    private Context mContext;

    public static AccountManagerUtil get(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context should not be null");
        }
        return new AccountManagerUtil(context);
    }

    private AccountManagerUtil(Context context) {
        mContext = context;
    }

    public Observable<Account> listAccounts() {
        AccountManager am = AccountManager.get(mContext);
        Account[] accountArray = am.getAccountsByType(JasperSettings.JASPER_ACCOUNT_TYPE);
        return Observable.from(accountArray);
    }

    public Observable<Boolean> removeAccounts() {
        return listAccounts()
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

    public Observable<Boolean> removeAccount(final Account account, final Handler handler) {
        final AccountManager am = AccountManager.get(mContext);
        String authToken = BasicAccountDataStorage.get(mContext).getServerCookie();
        am.invalidateAuthToken(account.type, authToken);
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
}
