package com.jaspersoft.android.jaspermobile.support.system;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import com.jaspersoft.android.jaspermobile.util.account.AccountStorage;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.6
 */
final class ProfilePasswordStoreAction {
    private static final String KEY = "PASSWORD_KEY";

    private final ProfileMapper mapper;
    private final Context context;
    private final String storagePassword;

    ProfilePasswordStoreAction(Context context, ProfileMapper mapper) {
        this.context = context;
        this.mapper = mapper;
        this.storagePassword = initSecret(context);
    }

    @SuppressLint("HardwareIds")
    private String initSecret(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
    }

    public boolean perform(final Profile profile) {
        Account account = mapper.toAccount(profile);
        AccountManager accountManager = AccountManager.get(context);
        AccountStorage accountStorage = new AccountStorage(accountManager, account);

        Observable<Boolean> observable = Hawk.init(context)
                .setEncryptionMethod(HawkBuilder.EncryptionMethod.HIGHEST)
                .setPassword(storagePassword)
                .setStorage(accountStorage)
                .buildRx();

        Boolean inited = observable.toBlocking().first();
        if (inited) {
            return Hawk.put(KEY, profile.getPassword());
        }
        return false;
    }
}
