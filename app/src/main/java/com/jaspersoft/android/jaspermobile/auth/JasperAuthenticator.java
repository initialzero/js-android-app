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
import com.jaspersoft.android.jaspermobile.network.RestClient;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;
import com.jaspersoft.android.retrofit.sdk.rest.LoginHelper;
import com.jaspersoft.android.retrofit.sdk.rest.LoginResponse;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;
import com.jaspersoft.android.sdk.service.auth.Credentials;
import com.jaspersoft.android.sdk.service.auth.SpringCredentials;
import com.jaspersoft.android.sdk.service.data.server.ServerInfo;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.exception.StatusCodes;

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

        String secret = mContext.getString(R.string.password_salt_key);
        mPasswordManager = PasswordManager.init(mContext, secret);

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

        String encrypted = accountManager.getPassword(account);
        Timber.d(String.format("Encrypted Password for account[%s] : %s", account.name, encrypted));

        String password = null;
        if (!TextUtils.isEmpty(encrypted)) {
            password = mPasswordManager.decrypt(encrypted);
            Timber.d(String.format("Password for account[%s] : %s", account.name, password));
        }

        if (TextUtils.isEmpty(password)) {
            return createErrorBundle(JasperAccountManager.TokenException.NO_PASSWORD_ERROR, mContext.getString(R.string.r_error_incorrect_credentials));
        }

        try {
            AccountServerData serverData = AccountServerData.get(mContext, account);

            RestClient restClient = createRestClient(serverData);
            Credentials credentials = SpringCredentials.builder()
                    .password(password)
                    .username(serverData.getUsername())
                    .organization(serverData.getOrganization())
                    .build();
            LoginResponse loginResponse = LoginHelper.login(
                    restClient, credentials
            );

            ServerInfo serverInfo = loginResponse.getServerInfo();

            boolean oldEdition = Boolean.valueOf(serverData.getEdition());
            boolean serverInfoEditionUpdated = serverInfo.isEditionPro() != oldEdition;
            boolean serverInfoVersionUpdated = !String.valueOf(serverInfo.getVersion()).equals(serverData.getVersionName());

            Timber.d("Updating user data with server info: " + serverInfo);
            accountManager.setUserData(account, AccountServerData.EDITION_KEY, String.valueOf(serverInfo.isEditionPro()));
            accountManager.setUserData(account, AccountServerData.VERSION_NAME_KEY, String.valueOf(serverInfo.getVersion()));

            if (serverInfoEditionUpdated || serverInfoVersionUpdated) {
                return createErrorBundle(JasperAccountManager.TokenException.SERVER_UPDATED_ERROR, mContext.getString(R.string.r_error_server_not_found));
            }

            if (serverInfo.getVersion().lessThan(ServerVersion.v5_5)) {
                return createErrorBundle(JasperAccountManager.TokenException.INCORRECT_SERVER_VERSION_ERROR, SERVER_DATA_WAS_UPDATED);
            }

            authToken = loginResponse.getCookie();
            accountManager.setAuthToken(account, JasperSettings.JASPER_AUTH_TOKEN_TYPE, authToken);

            Timber.d("Prepare correct token bundle");
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);

            return result;
        } catch (ServiceException serviceException) {
            Timber.d(serviceException, "We can not log in");
            int status;
            String message;
            if (serviceException.code() == StatusCodes.CLIENT_ERROR) {
                status = JasperAccountManager.TokenException.SERVER_NOT_FOUND;
                message = mContext.getString(R.string.r_error_server_not_found);
            } else {
                status = serviceException.code();
                message = serviceException.getMessage();
            }

            return createErrorBundle(status, message);
        }
    }

    private RestClient createRestClient(AccountServerData serverData) {
        DefaultPrefHelper defaultPrefHelper = DefaultPrefHelper_.getInstance_(mContext);
        return RestClient.builder()
                .serverUrl(serverData.getServerUrl())
                .connectionReadTimeOut(defaultPrefHelper.getReadTimeoutValue())
                .connectionTimeOut(defaultPrefHelper.getConnectTimeoutValue())
                .create();
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
