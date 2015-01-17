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
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity;
import com.jaspersoft.android.jaspermobile.activities.account.adapter.AccountsAdapter;
import com.jaspersoft.android.jaspermobile.activities.auth.AuthenticatorActivity;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboAccentFragmentActivity;
import com.jaspersoft.android.jaspermobile.util.rx.RxActions;
import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@OptionsMenu(R.menu.accounts_page_menu)
@EActivity(R.layout.common_list_layout)
public class AccountsActivity extends RoboAccentFragmentActivity {
    private static final String TAG = AccountsActivity.class.getSimpleName();
    private static final int ADD_ACCOUNT = 10;

    @ViewById(android.R.id.list)
    protected ListView mListView;
    @ViewById(android.R.id.empty)
    protected TextView mEmptyText;

    private final CompositeSubscription compositeSubscription = new CompositeSubscription();
    private AccountsAdapter mAdapter;
    private boolean mLoaded;

    private final Action1<Throwable> errorLogAction = RxActions.createLogErrorAction(TAG);
    private final Action1<List<Account>> listAllAccounts = new Action1<List<Account>>() {
        @Override
        public void call(List<Account> accounts) {
            Timber.d("Accounts are: " + accounts.toString());
            mLoaded = true;
            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();
            mAdapter.setNotifyOnChange(true);
            mAdapter.addAll(accounts);
        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(TAG);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mAdapter = new AccountsAdapter(AccountsActivity.this, savedInstanceState);
        mAdapter.registerDataSetObserver(new SimpleDataSetObserver());
    }

    @AfterViews
    final void init() {
        mAdapter.setAdapterView(mListView);
        mListView.setAdapter(mAdapter);
    }

    @ItemClick(android.R.id.list)
    final void onAccountSelection(Account account) {
        BasicAccountProvider.get(this).putAccount(account);
    }

    @OptionsItem(android.R.id.home)
    final void showHome() {
        HomeActivity.goHome(this);
    }

    @OptionsItem
    final void addAccount() {
        Intent intent = new Intent(this, AuthenticatorActivity.class);
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
    // Protected methods
    //---------------------------------------------------------------------

    @Override
    protected void onStart() {
        super.onStart();

        if (!mLoaded) {
            loadAccounts();
        }
    }

    @Override
    protected void onDestroy() {
        compositeSubscription.unsubscribe();
        super.onDestroy();
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void loadAccounts() {
        compositeSubscription.add(
                AccountManagerUtil.get(this)
                        .listAccounts()
                        .subscribe(listAllAccounts, errorLogAction)
        );
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
