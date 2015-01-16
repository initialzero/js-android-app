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
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.account.adapter.AccountsAdapter;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboAccentFragmentActivity;
import com.jaspersoft.android.jaspermobile.util.rx.RxActions;
import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EActivity(R.layout.common_list_layout)
public class AccountsActivity extends RoboAccentFragmentActivity {
    private static final String TAG = AccountsActivity.class.getSimpleName();

    @ViewById(android.R.id.list)
    protected ListView mListView;
    @ViewById(android.R.id.empty)
    protected TextView mEmptyText;

    private final CompositeSubscription compositeSubscription = new CompositeSubscription();
    private AccountsAdapter mAdapter;

    private final Action1<Throwable> errorLogAction = RxActions.createLogErrorAction(TAG);
    private final Action1<List<Account>> listAllAccounts = new Action1<List<Account>>() {
        @Override
        public void call(List<Account> accounts) {
            Timber.d("Accounts are: " + accounts.toString());
            mAdapter.addAll(accounts);
        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(TAG);

        mAdapter = new AccountsAdapter(AccountsActivity.this, savedInstanceState);
        mAdapter.registerDataSetObserver(new SimpleDataSetObserver());
    }

    @AfterViews
    final void init() {
        mListView.setAdapter(mAdapter);
        mAdapter.setAdapterView(mListView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        compositeSubscription.add(
                AccountManagerUtil.get(this)
                        .listAccounts()
                        .subscribe(listAllAccounts, errorLogAction)
        );
    }

    @Override
    protected void onDestroy() {
        compositeSubscription.unsubscribe();
        super.onDestroy();
    }

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
