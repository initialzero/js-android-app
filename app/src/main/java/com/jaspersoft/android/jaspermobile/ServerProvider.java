package com.jaspersoft.android.jaspermobile;

import android.accounts.Account;
import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.sdk.network.Server;

import java.util.concurrent.TimeUnit;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class ServerProvider implements Provider<Server> {
    @Inject
    Context mContext;

    @Override
    public Server get() {
        DefaultPrefHelper defaultPrefHelper = DefaultPrefHelper_.getInstance_(mContext);

        JasperAccountManager accountManager = JasperAccountManager.get(mContext);
        Account account = accountManager.getActiveAccount();
        AccountServerData serverData = AccountServerData.get(mContext, account);

        return Server.builder()
                .withBaseUrl(serverData.getServerUrl())
                .withReadTimeout(defaultPrefHelper.getReadTimeoutValue(), TimeUnit.MILLISECONDS)
                .withConnectionTimeOut(defaultPrefHelper.getConnectTimeoutValue(), TimeUnit.MILLISECONDS)
                .build();
    }
}
