package com.jaspersoft.android.jaspermobile;

import android.accounts.Account;
import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.sdk.network.AuthorizedClient;
import com.jaspersoft.android.sdk.network.Credentials;
import com.jaspersoft.android.sdk.network.Server;
import com.jaspersoft.android.sdk.network.SpringCredentials;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class RestClientProvider implements Provider<AuthorizedClient> {
//    private final PasswordManager passwordManager;

    @Inject
    Context mContext;
    @Inject
    Server mServer;
    @Inject
    CookieStore mCookieStore;

    @Inject
    public RestClientProvider() {
        // TODO fix password injection
    }

    @Override
    public AuthorizedClient get() {
        JasperAccountManager accountManager = JasperAccountManager.get(mContext);
        Account account = accountManager.getActiveAccount();
        AccountServerData serverData = AccountServerData.get(mContext, account);

        String pass = accountManager.getPassword(account);
//        try {
//            pass = passwordManager.decrypt(pass);
//        } catch (PasswordManager.DecryptionException e) {
//            pass = "";
//        }
        Credentials credentials = SpringCredentials.builder()
                .withUsername(serverData.getUsername())
                .withPassword(pass)
                .withOrganization(serverData.getOrganization())
                .build();

        CookieManager cookieManager = new CookieManager(mCookieStore, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        return mServer.newClient(credentials)
                .withCookieHandler(cookieManager)
                .create();
    }
}
