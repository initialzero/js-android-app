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

import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.interactor.GetTokenUseCase;
import com.jaspersoft.android.jaspermobile.domain.network.RestErrorCodes;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToRetrieveCredentials;
import com.jaspersoft.android.jaspermobile.presentation.view.activity.AuthenticatorActivity;
import com.jaspersoft.android.jaspermobile.util.JasperSettings;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class JasperAuthenticator extends AbstractAccountAuthenticator {
    private static final String SERVER_DATA_WAS_UPDATED = "Server version or edition has been updated";
    private final Context mContext;
    private final PasswordManager mPasswordManager;

    @Inject
    GetTokenUseCase mGetTokenUseCase;

    public JasperAuthenticator(Context context) {
        super(context);
        JasperMobileApplication.getComponent(context).inject(this);

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
    public Bundle getAuthToken(final AccountAuthenticatorResponse response,
                               final Account account,
                               final String authTokenType, Bundle options) throws NetworkErrorException {
        try {
            Profile profile = Profile.create(account.name);
            String authToken = mGetTokenUseCase.execute(profile);

            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);

            return result;
        } catch (RestStatusException restEx) {
            int status;
            String message;

            if (restEx.code() == RestErrorCodes.NETWORK_ERROR) {
                status = JasperAccountManager.TokenException.SERVER_NOT_FOUND;
                message = mContext.getString(R.string.r_error_server_not_found);
            } else {
                status = restEx.code();
                message = restEx.getMessage();
            }

            return createErrorBundle(status, message);
        } catch (FailedToRetrieveCredentials failedToRetrieveCredentials) {
            return createErrorBundle(
                    JasperAccountManager.TokenException.NO_PASSWORD_ERROR,
                    mContext.getString(R.string.r_error_incorrect_credentials)
            );
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
