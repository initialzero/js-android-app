/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.auth.AuthenticatorActivity;
import com.jaspersoft.android.jaspermobile.network.DefaultUrlConnectionClient;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;
import com.jaspersoft.android.retrofit.sdk.rest.JsRestClient2;
import com.jaspersoft.android.retrofit.sdk.rest.response.LoginResponse;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;

import retrofit.RetrofitError;
import rx.Observable;
import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class JasperAuthenticator extends AbstractAccountAuthenticator {
    private static final String SERVER_DATA_WAS_UPDATED = "Server version or edition has been updated";
    private final Context mContext;
    private final PasswordManager mPasswordManager;

    public JasperAuthenticator(Context context) {
        super(context);
        mContext = context;
        mPasswordManager = PasswordManager.create(mContext);
        Timber.tag(JasperAuthenticator.class.getSimpleName());
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        final Intent intent = new Intent(JasperSettings.ACTION_AUTHORIZE);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(final AccountAuthenticatorResponse response, final Account account, final String authTokenType, Bundle options) throws NetworkErrorException {
        Bundle result = new Bundle();
        AccountManager accountManager = AccountManager.get(mContext);
        String authToken = accountManager.peekAuthToken(account, authTokenType);

        if (!TextUtils.isEmpty(authToken)) {
            return result;
        }

        Observable<String> getPasswordOperation = mPasswordManager.get(account);
        String password = getPasswordOperation.toBlocking().firstOrDefault(null);

        if (TextUtils.isEmpty(password)) {
            return createErrorBundle(JasperAccountManager.TokenException.NO_PASSWORD_ERROR, mContext.getString(R.string.r_error_incorrect_credentials));
        }

        try {
            AccountServerData serverData = AccountServerData.get(mContext, account);
            JsRestClient2 jsRestClient2 = JsRestClient2
                    .configure()
                    .setEndpoint(serverData.getServerUrl() + JasperSettings.DEFAULT_REST_VERSION)
                    .setClient(new DefaultUrlConnectionClient(mContext))
                    .build();

            LoginResponse loginResponse = jsRestClient2.login(
                    serverData.getOrganization(), serverData.getUsername(), password
            ).toBlocking().firstOrDefault(null);

            ServerInfo serverInfo = loginResponse.getServerInfo();

            boolean serverInfoEditionUpdated = !serverInfo.getEdition().equals(serverData.getEdition());
            boolean serverInfoVersionUpdated = !serverInfo.getVersion().equals(serverData.getVersionName());

            Timber.d("Updating user data with server info: " + serverInfo);
            accountManager.setUserData(account, AccountServerData.EDITION_KEY, serverInfo.getEdition());
            accountManager.setUserData(account, AccountServerData.VERSION_NAME_KEY, serverInfo.getVersion());

            if (serverInfoEditionUpdated || serverInfoVersionUpdated) {
                return createErrorBundle(JasperAccountManager.TokenException.SERVER_UPDATED_ERROR, mContext.getString(R.string.r_error_server_not_found));
            }

            if (!ServerRelease.satisfiesMinVersion(serverInfo.getVersion())) {
                return createErrorBundle(JasperAccountManager.TokenException.INCORRECT_SERVER_VERSION_ERROR, SERVER_DATA_WAS_UPDATED);
            }

            authToken = loginResponse.getCookie();
            accountManager.setAuthToken(account, JasperSettings.JASPER_AUTH_TOKEN_TYPE, authToken);

            Timber.d("Prepare correct token bundle");
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);

            return result;
        } catch (RetrofitError retrofitError) {
            Timber.d(retrofitError, "We can not log in");
            int status;
            String message;
            if (retrofitError.getKind() == RetrofitError.Kind.NETWORK) {
                status = JasperAccountManager.TokenException.SERVER_NOT_FOUND;
                message = mContext.getString(R.string.r_error_server_not_found);
            } else {
                status = retrofitError.getResponse().getStatus();
                message = retrofitError.getMessage();
            }

            return createErrorBundle(status, message);
        }
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }

    private Bundle createErrorBundle(int status, String message) {
        Bundle result = new Bundle();

        // For android 4.4+ we need to send face intent with any data. In other case we will get error in AccountManagerFuture.getResult() method
        result.putParcelable(AccountManager.KEY_INTENT, new Intent(mContext, AuthenticatorActivity.class));
        result.putString(AccountManager.KEY_ERROR_MESSAGE, message);
        result.putInt(AccountManager.KEY_ERROR_CODE, status);

        return result;
    }
}
