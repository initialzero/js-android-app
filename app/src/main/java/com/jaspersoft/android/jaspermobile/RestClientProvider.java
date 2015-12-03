package com.jaspersoft.android.jaspermobile;

import android.accounts.Account;
import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.sdk.service.RestClient;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class RestClientProvider implements Provider<RestClient> {
    @Inject
    Context mContext;

    @Override
    public RestClient get() {
        JasperAccountManager accountManager = JasperAccountManager.get(mContext);
        Account account = accountManager.getActiveAccount();
        AccountServerData serverData = AccountServerData.get(mContext, account);
        return RestClient.builder()
                .serverUrl(serverData.getServerUrl())
                .create();
    }
}
