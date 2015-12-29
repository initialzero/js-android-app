package com.jaspersoft.android.jaspermobile.db.migrate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class MigrationV5 implements Migration {
    private static final String JASPER_ACCOUNT_TYPE = "com.jaspersoft";
    private static final String JASPER_AUTH_TOKEN_TYPE = "FULL ACCESS";

    private final Context mContext;

    public MigrationV5(Context context) {
        mContext = context;
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        AccountManager accountManager = AccountManager.get(mContext);
        Account[] accounts = accountManager.getAccountsByType(JASPER_ACCOUNT_TYPE);
        for (Account account : accounts) {
            String token = accountManager.peekAuthToken(account, JASPER_AUTH_TOKEN_TYPE);
            if (!TextUtils.isEmpty(token)){
                accountManager.invalidateAuthToken(JASPER_ACCOUNT_TYPE, token);
            }
        }
    }
}
