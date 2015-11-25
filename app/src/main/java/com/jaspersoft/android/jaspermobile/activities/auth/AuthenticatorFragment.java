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

package com.jaspersoft.android.jaspermobile.activities.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.network.RestClient;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.rest.LoginHelper;
import com.jaspersoft.android.retrofit.sdk.rest.LoginResponse;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;
import com.jaspersoft.android.sdk.service.auth.Credentials;
import com.jaspersoft.android.sdk.service.auth.SpringCredentials;
import com.jaspersoft.android.sdk.service.data.server.ServerInfo;
import com.jaspersoft.android.sdk.service.exception.ServiceException;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.fragment.RoboFragment;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static rx.android.app.AppObservable.bindFragment;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EFragment(R.layout.add_account_layout)
public class AuthenticatorFragment extends RoboFragment {

    @Inject
    @Named("DEMO_ENDPOINT")
    protected String demoServerUrl;

    @ViewById
    protected EditText aliasEdit;
    @ViewById
    protected EditText usernameEdit;
    @ViewById
    protected EditText organizationEdit;
    @ViewById
    protected EditText serverUrlEdit;
    @ViewById
    protected EditText passwordEdit;
    @ViewById(R.id.tryDemoContainer)
    protected ViewGroup tryDemoLayout;
    @InstanceState
    protected boolean mFetching;

    @SystemService
    protected InputMethodManager inputMethodManager;

    private Observable<LoginResponse> loginDemoTask;
    private Observable<LoginResponse> tryDemoTask;
    private Subscription loginSubscription = Subscriptions.empty();
    private Subscription demoSubscription = Subscriptions.empty();
    private Subscription addAccountSubscription = Subscriptions.empty();

    private final Action1<Throwable> onError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            Timber.e(throwable, "Login failed");

            String exceptionMessage;
            if (throwable instanceof InvalidServerVersionException) {
                exceptionMessage = getString(R.string.r_error_server_not_supported);
            } else if (throwable instanceof ServiceException) {
                exceptionMessage = RequestExceptionHandler.extractMessage(getContext(), throwable);
            } else {
                exceptionMessage = getString(R.string.failure_add_account, throwable.getMessage());
            }

            Toast.makeText(getActivity(), exceptionMessage, Toast.LENGTH_LONG).show();
            setProgressEnabled(false);
        }
    };
    private final Action1<AccountServerData> onSuccess = new Action1<AccountServerData>() {
        @Override
        public void call(AccountServerData serverData) {
            setProgressEnabled(false);
            addAccount(serverData);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(AuthenticatorFragment.class.getSimpleName());
        setRetainInstance(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (addAccountSubscription != null) {
            addAccountSubscription.unsubscribe();
        }
        if (loginSubscription != null) {
            loginSubscription.unsubscribe();
        }
        if (demoSubscription != null) {
            demoSubscription.unsubscribe();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (demoAccountExist()) {
            tryDemoLayout.setVisibility(View.GONE);
        } else {
            tryDemoLayout.setVisibility(View.VISIBLE);
        }

        setProgressEnabled(mFetching);
        if (loginDemoTask != null && mFetching) {
            requestCustomLogin();
        }
        if (tryDemoTask != null && mFetching) {
            requestDemoLogin();
        }
    }

    @Click(R.id.addAccount)
    public void addAccount() {
        hideKeyboard();
        if (!isFormValid()) return;

        setProgressEnabled(true);

        if (loginSubscription != null) {
            loginSubscription.unsubscribe();
        }

        String serverUrl = trimUrl(serverUrlEdit.getText().toString());
        String username = usernameEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        String organization = organizationEdit.getText().toString().trim();

        Observable<LoginResponse> loginObservable = initLogin(serverUrl, username, password, organization);

        loginDemoTask = bindFragment(this, loginObservable.cache());
        requestCustomLogin();
    }

    @TextChange({R.id.aliasEdit, R.id.usernameEdit, R.id.serverUrlEdit, R.id.passwordEdit})
    protected void removeValidationErrorOnTextChange(TextView editText, CharSequence text) {
        editText.setError(null);
    }

    private void requestCustomLogin() {
        loginSubscription = loginDemoTask
                .flatMap(new Func1<LoginResponse, Observable<AccountServerData>>() {
                    @Override
                    public Observable<AccountServerData> call(LoginResponse response) {
                        validateServerVersion(response);
                        return createUserAccountData(response);
                    }
                })
                .subscribe(onSuccess, onError);
    }

    private void validateServerVersion(LoginResponse response) {
        double version = response.getServerInfo().getVersion();
        if (!ServerRelease.satisfiesMinVersion(String.valueOf(version))) {
            throw new InvalidServerVersionException(version);
        }
    }

    @Click(R.id.tryDemo)
    public void tryDemo() {
        hideKeyboard();

        setProgressEnabled(true);

        if (demoSubscription != null) {
            demoSubscription.unsubscribe();
        }

        Observable<LoginResponse> tryDemoObservable = initLogin(
                AccountServerData.Demo.SERVER_URL,
                AccountServerData.Demo.USERNAME,
                AccountServerData.Demo.PASSWORD,
                AccountServerData.Demo.ORGANIZATION
        );

        tryDemoTask = bindFragment(this, tryDemoObservable.cache());
        requestDemoLogin();
    }

    private void requestDemoLogin() {
        demoSubscription = tryDemoTask
                .flatMap(new Func1<LoginResponse, Observable<AccountServerData>>() {
                    @Override
                    public Observable<AccountServerData> call(LoginResponse response) {
                        return createDemoAccountData(response);
                    }
                })
                .subscribe(onSuccess, onError);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private boolean demoAccountExist() {
        Account[] accounts = JasperAccountManager.get(getActivity()).getAccounts();
        for (Account account : accounts) {
            if (account.name.equals(AccountServerData.Demo.ALIAS))
                return true;
        }
        return false;
    }

    private Observable<AccountServerData> createUserAccountData(LoginResponse response) {
        ServerInfo serverInfo = response.getServerInfo();

        AccountServerData serverData = new AccountServerData()
                .setServerCookie(response.getCookie())
                .setAlias(aliasEdit.getText().toString())
                .setServerUrl(trimUrl(serverUrlEdit.getText().toString()))
                .setOrganization(organizationEdit.getText().toString().trim())
                .setUsername(usernameEdit.getText().toString())
                .setPassword(passwordEdit.getText().toString())
                .setEdition(String.valueOf(serverInfo.getEdition()))
                .setVersionName(String.valueOf(serverInfo.getVersion()));

        return Observable.just(serverData);
    }

    private Observable<AccountServerData> createDemoAccountData(LoginResponse response) {
        ServerInfo serverInfo = response.getServerInfo();

        AccountServerData serverData = new AccountServerData()
                .setServerCookie(response.getCookie())
                .setAlias(AccountServerData.Demo.ALIAS)
                .setServerUrl(demoServerUrl)
                .setOrganization(AccountServerData.Demo.ORGANIZATION)
                .setUsername(AccountServerData.Demo.USERNAME)
                .setPassword(AccountServerData.Demo.PASSWORD)
                .setEdition(String.valueOf(serverInfo.getEdition()))
                .setVersionName(String.valueOf(serverInfo.getVersion()));

        return Observable.just(serverData);
    }

    private void addAccount(final AccountServerData serverData) {
        addAccountSubscription = JasperAccountManager.get(getActivity())
                .addAccountExplicitly(serverData)
                .subscribe(new Action1<Account>() {
                    @Override
                    public void call(Account account) {
                        JasperAccountManager.get(getActivity()).activateAccount(account);
                        activateAccount(serverData.getServerCookie());
                        setProgressEnabled(false);
                    }
                }, onError);
    }

    private void activateAccount(String authToken) {
        AccountManager accountManager = AccountManager.get(getActivity());
        Account account = JasperAccountManager.get(getActivity()).getActiveAccount();
        accountManager.setAuthToken(account, JasperSettings.JASPER_AUTH_TOKEN_TYPE, authToken);

        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, JasperSettings.JASPER_ACCOUNT_TYPE);
        data.putString(AccountManager.KEY_AUTHTOKEN, authToken);
        getAccountAuthenticatorActivity().setAccountAuthenticatorResult(data);

        Toast.makeText(getActivity(),
                getString(R.string.success_add_account, account.name),
                Toast.LENGTH_SHORT).show();

        Intent resultIntent = new Intent();
        resultIntent.putExtras(data);
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        getActivity().finish();
    }

    private void setProgressEnabled(boolean enabled) {
        mFetching = enabled;
        if (mFetching) {
            ProgressDialogFragment.builder(getFragmentManager())
                    .setLoadingMessage(R.string.account_add)
                    .show();
        } else {
            ProgressDialogFragment.dismiss(getFragmentManager());
        }
    }

    private AuthenticatorActivity getAccountAuthenticatorActivity() {
        if (getActivity() instanceof AuthenticatorActivity) {
            return (AuthenticatorActivity) getActivity();
        } else {
            throw new IllegalStateException("Fragment can only be consumed " +
                    "within com.jaspersoft.android.jaspermobile.activities.auth.AuthenticatorActivity");
        }
    }

    private boolean isFormValid() {
        String serverUrl = serverUrlEdit.getText().toString();
        String alias = aliasEdit.getText().toString();

        Map<EditText, String> valueMap = new HashMap<EditText, String>();
        valueMap.put(aliasEdit, alias);
        valueMap.put(serverUrlEdit, serverUrl);
        valueMap.put(usernameEdit, usernameEdit.getText().toString());
        valueMap.put(passwordEdit, passwordEdit.getText().toString());

        boolean isFieldValid;
        boolean formValid = true;
        for (Map.Entry<EditText, String> entry : valueMap.entrySet()) {
            isFieldValid = !TextUtils.isEmpty(entry.getValue().trim());
            if (!isFieldValid) {
                entry.getKey().setError(getString(R.string.sp_error_field_required));
                entry.getKey().requestFocus();
            }
            formValid &= isFieldValid;
        }

        if (!TextUtils.isEmpty(serverUrl)) {
            String url = trimUrl(serverUrl);
            if (!URLUtil.isNetworkUrl(url)) {
                serverUrlEdit.setError(getString(R.string.sp_error_url_not_valid));
                serverUrlEdit.requestFocus();
                formValid &= false;
            }
        }

        if (!TextUtils.isEmpty(alias)) {
            Account account = new Account(alias, JasperSettings.JASPER_ACCOUNT_TYPE);
            List<Account> accountList = new ArrayList<Account>();
            Collections.addAll(accountList, JasperAccountManager.get(getActivity()).getAccounts());
            if (accountList.contains(account)) {
                aliasEdit.setError(getString(R.string.sp_error_duplicate_alias));
                aliasEdit.requestFocus();
                formValid &= false;
            }

            if (alias.equals(JasperSettings.RESERVED_ACCOUNT_NAME)) {
                aliasEdit.setError(getString(R.string.sp_error_reserved_alias));
                aliasEdit.requestFocus();
                formValid &= false;
            }
        }

        return formValid;
    }

    private String trimUrl(String url) {
        if (!TextUtils.isEmpty(url) && url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private void hideKeyboard() {
        View focus = getActivity().getCurrentFocus();
        if (focus != null) {
            IBinder token = focus.getWindowToken();
            if (token != null) {
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    @NonNull
    private Observable<LoginResponse> initLogin(String serverUrl, String username, String password, String organization) {
        RestClient restClient = RestClient.builder()
                .serverUrl(serverUrl)
                .create();
        Credentials credentials = SpringCredentials.builder()
                .password(password)
                .username(username)
                .organization(organization)
                .build();
        return LoginHelper.loginAsObservable(
                restClient, credentials
        ).subscribeOn(Schedulers.io());
    }

    private static class InvalidServerVersionException extends RuntimeException {
        public InvalidServerVersionException(double code) {
            super("Minimal server version condition not acquired. Passed version was: " + code);
        }
    }
}
