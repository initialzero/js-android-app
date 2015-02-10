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

import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.ojm.ServerInfo;
import com.jaspersoft.android.retrofit.sdk.rest.JsRestClient2;
import com.jaspersoft.android.retrofit.sdk.rest.response.LoginResponse;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import retrofit.RetrofitError;
import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class JasperAuthenticator extends AbstractAccountAuthenticator {
    private final Context mContext;

    public JasperAuthenticator(Context context) {
        super(context);
        mContext = context;
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

        Timber.d("We have peek token: " + authToken);

        // Lets give another try to authenticate the user
        if (TextUtils.isEmpty(authToken)) {
            String password = accountManager.getPassword(account);
            Timber.d(String.format("Password for account[%s] : %s", account.name, password));

            if (password != null) {
                AccountServerData serverData = AccountServerData.get(mContext, account);
                JsRestClient2 jsRestClient2 = JsRestClient2.forEndpoint(
                        serverData.getServerUrl() + JasperSettings.DEFAULT_REST_VERSION);
                try {
                    LoginResponse loginResponse = jsRestClient2.login(
                            serverData.getOrganization(), serverData.getUsername(), password
                    ).toBlocking().firstOrDefault(null);

                    ServerInfo serverInfo = loginResponse.getServerInfo();
                    Timber.d("Updating user data with server info: " + serverInfo);
                    accountManager.setUserData(account, AccountServerData.EDITION_KEY, serverInfo.getEdition());
                    accountManager.setUserData(account, AccountServerData.VERSION_NAME_KEY, serverInfo.getVersion());
                    authToken = loginResponse.getCookie();

                    accountManager.setAuthToken(account, JasperSettings.JASPER_AUTH_TOKEN_TYPE, authToken);
                    Timber.d("New token: " + authToken);
                } catch (RetrofitError retrofitError) {
                    Timber.d(retrofitError, "We cant access user password :(");
                    int status = retrofitError.getResponse().getStatus();

                    result.putString(AccountManager.KEY_ERROR_MESSAGE, retrofitError.getMessage());
                    result.putInt(AccountManager.KEY_ERROR_CODE, status);

                    Intent intent = new Intent();
                    if (status == 401) {
                        intent.setAction(JasperSettings.ACTION_INVALID_PASSWORD);
                        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
                    } else {
                        intent.setAction(JasperSettings.ACTION_REST_ERROR);
                        intent.putExtra(RestErrorReceiver.KEY_EXCEPTION_MESSAGE, retrofitError.getMessage());
                    }
                    mContext.sendBroadcast(intent);
                }
            }
        }

        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken)) {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
        }

        return result;
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
}
