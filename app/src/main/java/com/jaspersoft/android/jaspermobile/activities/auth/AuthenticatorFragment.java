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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.profile.ServersManagerActivity_;
import com.jaspersoft.android.jaspermobile.db.MobileDbProvider;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.legacy.ProfileManager;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;
import com.jaspersoft.android.retrofit.sdk.ojm.ServerInfo;
import com.jaspersoft.android.retrofit.sdk.rest.JsRestClient2;
import com.jaspersoft.android.retrofit.sdk.rest.response.LoginResponse;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import roboguice.fragment.RoboFragment;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static rx.android.app.AppObservable.bindFragment;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EFragment(R.layout.activity_login)
public class AuthenticatorFragment extends RoboFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int CHOOSE_SERVER = 15;
    private static final String[] FROM = {ServerProfilesTable.ALIAS};
    private static final int[] TO = {android.R.id.text1};

    @ViewById
    protected Spinner profiles;
    @InstanceState
    protected boolean mFetching;
    @Inject
    @Named("JASPER_DEMO")
    protected JsRestClient2 demoRestClient;
    @Inject
    protected JsRestClient legacyRestClient;

    private SimpleCursorAdapter cursorAdapter;
    private Observable<LoginResponse> tryDemoTask;
    private Subscription loginSubscription = Subscriptions.empty();
    private Observable<LoginResponse> demoLoginObservable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(AuthenticatorFragment.class.getSimpleName());
        setRetainInstance(true);

        setProgressEnabled(mFetching);
        demoLoginObservable = demoRestClient.login(
                AccountServerData.Demo.ORGANIZATION,
                AccountServerData.Demo.USERNAME,
                AccountServerData.Demo.PASSWORD
        ).subscribeOn(Schedulers.io());
    }

    @AfterViews
    final void init() {
        profiles.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    ServersManagerActivity_.intent(getActivity()).startForResult(CHOOSE_SERVER);
                }
                return true;
            }
        });

        cursorAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1, null, FROM, TO, 0);
        profiles.setAdapter(cursorAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), MobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                ServerProfilesTable.ALL_COLUMNS, null, null, ServerProfilesTable.CREATED_AT + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (cursorAdapter != null) {
            cursorAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (cursorAdapter != null) {
            cursorAdapter.swapCursor(null);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (tryDemoTask != null && mFetching) {
            tryDemo();
        }
    }

    @Override
    public void onDestroyView() {
        loginSubscription.unsubscribe();
        super.onDestroyView();
    }

    @Click
    public void tryDemo() {
        final Context context = getActivity();
        setProgressEnabled(true);

        if (loginSubscription != null) {
            loginSubscription.unsubscribe();
        }

        tryDemoTask = bindFragment(this, demoLoginObservable.cache());
        loginSubscription = tryDemoTask
                .subscribe(new Action1<LoginResponse>() {
                    @Override
                    public void call(LoginResponse response) {
                        Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
                        setProgressEnabled(false);
                        applyDemoAccountData(response);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e("Login failed", throwable);
                        Toast.makeText(context, "Login failed because of: " + throwable.getMessage(),
                                Toast.LENGTH_LONG).show();
                        setProgressEnabled(false);
                    }
                });
    }


    @OnActivityResult(CHOOSE_SERVER)
    final void chooseServer(int resultCode) {
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void applyDemoAccountData(LoginResponse response) {
        String cookie = response.getCookie();
        ServerInfo serverInfo = response.getServerInfo();

        AccountServerData serverData = new AccountServerData()
                .setAlias(AccountServerData.Demo.ALIAS)
                .setServerUrl(AccountServerData.Demo.SERVER_URL)
                .setOrganization(AccountServerData.Demo.ORGANIZATION)
                .setUsername(AccountServerData.Demo.USERNAME)
                .setPassword(AccountServerData.Demo.PASSWORD)
                .setEdition(serverInfo.getEdition())
                .setVersionName(serverInfo.getVersion());

        applyAccountData(cookie, serverData);
    }

    private void applyAccountData(String authToken, AccountServerData serverData) {
        legacyRestClient.setServerProfile(ProfileManager.getServerProfile(serverData));

        Account account = BasicAccountProvider.get(getActivity())
                .putAccountName(serverData.getAlias())
                .getAccount();

        AccountManager accountManager = AccountManager.get(getActivity());
        accountManager.addAccountExplicitly(account, serverData.getPassword(), serverData.toBundle());
        accountManager.setAuthToken(account, JasperSettings.JASPER_AUTH_TOKEN_TYPE, authToken);

        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, serverData.getAlias());
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, JasperSettings.JASPER_ACCOUNT_TYPE);
        data.putString(AccountManager.KEY_AUTHTOKEN, authToken);
        getAccountAuthenticatorActivity().setAccountAuthenticatorResult(data);

        Intent resultIntent = new Intent();
        resultIntent.putExtras(data);
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        getActivity().finish();
    }

    private void setProgressEnabled(boolean enabled) {
        mFetching = enabled;
        getActivity().setProgressBarIndeterminateVisibility(mFetching);
    }

    private AuthenticatorActivity getAccountAuthenticatorActivity() {
        if (getActivity() instanceof AuthenticatorActivity) {
            return (AuthenticatorActivity) getActivity();
        } else {
            throw new IllegalStateException("Fragment can only be consumed " +
                    "within com.jaspersoft.android.jaspermobile.activities.auth.AuthenticatorActivity");
        }
    }
}
