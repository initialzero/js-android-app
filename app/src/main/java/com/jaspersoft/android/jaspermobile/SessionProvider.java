package com.jaspersoft.android.jaspermobile;

import android.accounts.Account;
import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.sdk.service.RestClient;
import com.jaspersoft.android.sdk.service.Session;
import com.jaspersoft.android.sdk.service.auth.Credentials;
import com.jaspersoft.android.sdk.service.auth.SpringCredentials;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class SessionProvider implements Provider<Session> {
    @Inject
    Context mContext;
    @Inject
    RestClient mRestClient;

    @Override
    public Session get() {
        JasperAccountManager accountManager = JasperAccountManager.get(mContext);
        Account account = accountManager.getActiveAccount();
        AccountServerData serverData = AccountServerData.get(mContext, account);
        Credentials credentials = SpringCredentials.builder()
                .username(serverData.getUsername())
                .password(serverData.getPassword())
                .organization(serverData.getOrganization())
                .build();
        return mRestClient.newSession(credentials).create();
    }
}
