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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ListItemView;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ListItemView_;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.SingleChoiceArrayAdapter;
import com.jaspersoft.android.jaspermobile.dialog.ConfirmDialogFragment;
import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import roboguice.activity.RoboFragmentActivity;
import rx.functions.Action1;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EActivity(R.layout.common_list_layout)
public class AccountsActivity extends RoboFragmentActivity {

    @ViewById
    protected ListView mListView;
    private AccountsAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountManagerUtil.get(this)
                .listAccounts().subscribe(new Action1<Account>() {
            @Override
            public void call(Account account) {

            }
        });
        mAdapter = new AccountsAdapter(savedInstanceState, this, new ArrayList<Account>());
    }

    private static class AccountsAdapter extends SingleChoiceArrayAdapter<Account> {

        private final String activeAccountName;

        public AccountsAdapter(Bundle savedInstanceState, Context context, List<Account> accounts) {
            super(savedInstanceState, context, 0, 0, accounts);
            activeAccountName = BasicAccountProvider.get(context).getAccountName();
        }

        @Override
        protected View getViewImpl(int position, View convertView, ViewGroup parent) {
            ListItemView itemView = (ListItemView) convertView;
            if (itemView == null) {
                itemView = ListItemView_.build(getContext());
            }

            Account account = getItem(position);
            AccountServerData serverData = AccountServerData.get(getContext(), account);
            itemView.setTitle(serverData.getUsername());
            itemView.setSubTitle(serverData.getServerUrl());

            boolean isActive = (activeAccountName.equals(serverData.getUsername()));
            itemView.getImageView().setImageResource(isActive ?
                    R.drawable.ic_composed_active_server : R.drawable.ic_composed_server);

            return itemView;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.am_servers_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.deleteItem) {
                FragmentActivity activity = (FragmentActivity) getContext();
                ConfirmDialogFragment.builder(activity.getSupportFragmentManager())
                        .title(R.string.warning_msg)
                        .message(R.string.spm_ad_delete_profile_msg)
                        .positiveClick(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Account account = getItem(getCurrentPosition());
                                AccountManagerUtil
                                        .get(getContext()).removeAccount(account)
                                        .subscribe(new Action1<Boolean>() {
                                            @Override
                                            public void call(Boolean aBoolean) {
                                                notifyDataSetChanged();
                                            }
                                        });
                            }
                        });
                return true;
            }
            return false;
        }
    }
}
