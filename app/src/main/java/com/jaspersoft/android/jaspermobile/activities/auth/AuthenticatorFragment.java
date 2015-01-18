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

package com.jaspersoft.android.jaspermobile.activities.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.legacy.ProfileManager;
import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;
import com.jaspersoft.android.retrofit.sdk.ojm.ServerInfo;
import com.jaspersoft.android.retrofit.sdk.rest.JsRestClient2;
import com.jaspersoft.android.retrofit.sdk.rest.response.LoginResponse;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

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
@EFragment(R.layout.login_layout)
public class AuthenticatorFragment extends RoboFragment {
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
    @InstanceState
    protected boolean mFetching;
    @Inject
    protected JsRestClient legacyRestClient;

    private Observable<LoginResponse> loginDemoTask;
    private Subscription loginSubscription = Subscriptions.empty();
    private Subscription addAccountSubscription = Subscriptions.empty();

    private final Action1<Throwable> onError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            Timber.e(throwable, "Login failed");
            Toast.makeText(getActivity(),
                    getString(R.string.failure_add_account, throwable.getMessage()),
                    Toast.LENGTH_LONG).show();
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
    public void onStart() {
        super.onStart();
        setProgressEnabled(mFetching);
        if (loginDemoTask != null && mFetching) {
            addAccount();
        }
    }

    @Override
    public void onDestroyView() {
        addAccountSubscription.unsubscribe();
        loginSubscription.unsubscribe();
        super.onDestroyView();
    }

    @Click
    public void addAccount() {
        if (!isFormValid()) return;

        setProgressEnabled(true);

        if (loginSubscription != null) {
            loginSubscription.unsubscribe();
        }

        String endpoint = trimUrl(serverUrlEdit.getText().toString())
                + JasperSettings.DEFAULT_REST_VERSION;
        JsRestClient2 restClient = JsRestClient2.forEndpoint(endpoint);
        Observable<LoginResponse> loginObservable = restClient.login(
                organizationEdit.getText().toString(),
                usernameEdit.getText().toString(),
                passwordEdit.getText().toString()
        ).subscribeOn(Schedulers.io());

        loginDemoTask = bindFragment(this, loginObservable.cache());
        loginSubscription = loginDemoTask
                .flatMap(new Func1<LoginResponse, Observable<AccountServerData>>() {
                    @Override
                    public Observable<AccountServerData> call(LoginResponse response) {
                        return createUserAccountData(response);
                    }
                })
                .subscribe(onSuccess, onError);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private Observable<AccountServerData> createUserAccountData(LoginResponse response) {
        ServerInfo serverInfo = response.getServerInfo();

        AccountServerData serverData = new AccountServerData()
                .setServerCookie(response.getCookie())
                .setServerUrl(trimUrl(serverUrlEdit.getText().toString()))
                .setOrganization(organizationEdit.getText().toString())
                .setUsername(usernameEdit.getText().toString())
                .setPassword(passwordEdit.getText().toString())
                .setEdition(serverInfo.getEdition())
                .setVersionName(serverInfo.getVersion());

        return Observable.just(serverData);
    }

    private void addAccount(final AccountServerData serverData) {
        legacyRestClient.setServerProfile(ProfileManager.getServerProfile(serverData));

        addAccountSubscription = AccountManagerUtil.get(getActivity())
                .addAccountExplicitly(serverData)
                .subscribe(new Action1<Account>() {
                    @Override
                    public void call(Account account) {
                        BasicAccountProvider.get(getActivity()).putAccount(account);
                        activateAccount(serverData.getServerCookie());
                        setProgressEnabled(false);
                    }
                }, onError);
    }

    private void activateAccount(String authToken) {
        AccountManager accountManager = AccountManager.get(getActivity());
        Account account = BasicAccountProvider.get(getActivity()).getAccount();
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
                    .setLoadingMessage(R.string.adding_account)
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

        Map<EditText, String> valueMap = Maps.newHashMap();
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
            List<Account> accountList = AccountManagerUtil.get(getActivity())
                    .listAccounts().toBlocking().first();
            if (accountList.contains(account)) {
                aliasEdit.setError(getString(R.string.sp_error_duplicate_alias));
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
}
