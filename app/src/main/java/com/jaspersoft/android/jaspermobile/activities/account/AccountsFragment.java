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

package com.jaspersoft.android.jaspermobile.activities.account;

import android.accounts.Account;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity;
import com.jaspersoft.android.jaspermobile.activities.account.adapter.AccountsAdapter;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.legacy.ProfileManager;
import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import roboguice.fragment.RoboFragment;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static rx.android.app.AppObservable.bindActivity;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@OptionsMenu(R.menu.accounts_page_menu)
@EFragment(R.layout.common_list_layout)
public class AccountsFragment extends RoboFragment {
    private static final String TAG = AccountsActivity.class.getSimpleName();
    private static final int ADD_ACCOUNT = 10;

    @Inject
    protected JsRestClient jsRestClient;

    @ViewById(android.R.id.list)
    protected ListView mListView;
    @ViewById(android.R.id.empty)
    protected TextView mEmptyText;

    @InstanceState
    protected boolean mFetching;
    @InstanceState
    protected Account selectedAccount;

    private Subscription addAccountSubscription = Subscriptions.empty();
    private Subscription loadAccountSubscription = Subscriptions.empty();
    private Observable<String> updateTokenTask;
    private Bundle mSavedInstanceState;
    private AccountsAdapter mAdapter;

    private final Action1<Throwable> errorLogAction = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            Timber.e(throwable, "Failed to load subscriptions");
            Toast.makeText(getActivity(), "Failed to activate account", Toast.LENGTH_SHORT).show();
            setProgressEnabled(false);
        }
    };
    private final Action1<List<Account>> listAllAccounts = new Action1<List<Account>>() {
        @Override
        public void call(List<Account> accounts) {
            Timber.d("Accounts are: " + accounts.toString());
            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();
            mAdapter.setNotifyOnChange(true);
            mAdapter.addAll(accounts);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Timber.tag(TAG);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    @AfterViews
    final void initialize() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mAdapter = new AccountsAdapter(getActivity(), mSavedInstanceState);
        mAdapter.registerDataSetObserver(new SimpleDataSetObserver());
        mAdapter.setAdapterView(mListView);
        mListView.setAdapter(mAdapter);
        loadAccounts();
    }

    @Override
    public void onStart() {
        super.onStart();
        setProgressEnabled(mFetching);
        if (selectedAccount != null &&
                updateTokenTask != null &&
                mFetching) {
            onAccountSelection(selectedAccount);
        }
    }

    @Override
    public void onDestroyView() {
        addAccountSubscription.unsubscribe();
        loadAccountSubscription.unsubscribe();
        super.onDestroy();
    }


    @ItemClick(android.R.id.list)
    final void onAccountSelection(final Account account) {
        setProgressEnabled(true);

        selectedAccount = account;
        updateTokenTask = AccountManagerUtil.get(getActivity())
                .activateAccount(account);
        addAccountSubscription = bindActivity(getActivity(), updateTokenTask.cache())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        mAdapter.notifyDataSetChanged();
                        BasicAccountProvider.get(getActivity()).putAccount(selectedAccount);
                        ProfileManager.initLegacyJsRestClient(getActivity(), jsRestClient);
                        setProgressEnabled(false);
                    }
                }, errorLogAction);
    }

    @OptionsItem(android.R.id.home)
    final void showHome() {
        HomeActivity.goHome(getActivity());
    }

    @OptionsItem
    final void addAccount() {
        Intent intent = new Intent(JasperSettings.ACTION_AUTHORIZE);
        intent.putExtra("account_types", new String[]{"com.jaspersoft"});
        startActivityForResult(intent, ADD_ACCOUNT);
    }

    @OnActivityResult(ADD_ACCOUNT)
    final void onAuthorize(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            loadAccounts();
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void loadAccounts() {
        loadAccountSubscription = AccountManagerUtil.get(getActivity())
                .listAccounts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listAllAccounts, errorLogAction);
    }

    private void setProgressEnabled(boolean enabled) {
        mFetching = enabled;
        if (mFetching) {
            ProgressDialogFragment.builder(getFragmentManager())
                    .setLoadingMessage(R.string.activating_account)
                    .show();
        } else {
            ProgressDialogFragment.dismiss(getFragmentManager());
        }
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private class SimpleDataSetObserver extends DataSetObserver {
        private boolean mFirstRun;

        private SimpleDataSetObserver() {
            mFirstRun = true;
        }

        @Override
        public void onChanged() {
            if (mFirstRun) {
                mFirstRun = false;
            }
            if (mAdapter.getCount() == 0) {
                mEmptyText.setVisibility(View.VISIBLE);
                mEmptyText.setText(mFirstRun ? R.string.loading_msg : R.string.no_accounts);
                return;
            }
            mEmptyText.setVisibility(View.GONE);
        }
    }

}
