package com.jaspersoft.android.jaspermobile;

import android.accounts.Account;
import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;
import com.jaspersoft.android.sdk.service.RestClient;
import com.jaspersoft.android.sdk.service.Session;
import com.jaspersoft.android.sdk.service.auth.Credentials;
import com.jaspersoft.android.sdk.service.auth.SpringCredentials;
import com.jaspersoft.android.sdk.service.token.TokenCache;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class SessionProvider implements Provider<Session> {
    @Inject
    RestClient mRestClient;
    @Inject
    TokenCache mCache;

    private final Context mContext;
    private final PasswordManager mPasswordManager;
    private final JasperAccountManager mAccountManager;

    @Inject
    public SessionProvider(Context context) {
        mContext = context;
        mAccountManager = JasperAccountManager.get(context);

        String secret = context.getString(R.string.password_salt_key);
        mPasswordManager = PasswordManager.init(mContext, secret);
    }

    @Override
    public Session get() {
        Account account = mAccountManager.getActiveAccount();
        AccountServerData serverData = AccountServerData.get(mContext, account);

        String password = mPasswordManager.decrypt(serverData.getPassword());

        Credentials credentials = SpringCredentials.builder()
                .username(serverData.getUsername())
                .password(password)
                .organization(serverData.getOrganization())
                .build();
        return mRestClient.newSession(credentials)
                .tokenCache(mCache)
                .create();
    }
}
