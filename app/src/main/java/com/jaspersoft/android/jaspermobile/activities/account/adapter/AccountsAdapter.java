package com.jaspersoft.android.jaspermobile.activities.account.adapter;

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

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ListItemView;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ListItemView_;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.SingleChoiceArrayAdapter;
import com.jaspersoft.android.jaspermobile.dialog.ConfirmDialogFragment;
import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.retrofit.sdk.account.AccountProvider;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;

import rx.functions.Action1;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class AccountsAdapter extends SingleChoiceArrayAdapter<Account> {

    private final AccountProvider accountProvider;

    public AccountsAdapter(Context context, Bundle savedInstanceState) {
        super(savedInstanceState, context, 0);
        accountProvider = BasicAccountProvider.get(getContext());
    }

    @Override
    protected View getViewImpl(int position, View convertView, ViewGroup parent) {
        ListItemView itemView = (ListItemView) convertView;
        if (itemView == null) {
            itemView = ListItemView_.build(getContext());
        }

        Account account = getItem(position);
        AccountServerData serverData = AccountServerData.get(getContext(), account);
        itemView.setTitle(account.name);
        itemView.setSubTitle(serverData.getServerUrl());

        Account activeAccount = accountProvider.getAccount();
        if (activeAccount != null) {
            boolean isActive = (activeAccount.name.equals(account.name));
            itemView.getImageView().setImageResource(isActive ?
                    R.drawable.ic_composed_active_server : R.drawable.ic_composed_server);
        } else {
            itemView.getImageView().setImageResource(R.drawable.ic_composed_server);
        }

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
        return true;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.deleteItem) {
            FragmentActivity activity = (FragmentActivity) getContext();
            ConfirmDialogFragment.builder(activity.getSupportFragmentManager())
                    .title(R.string.warning_msg)
                    .message(R.string.spm_ad_delete_profile_msg)
                    .positiveClick(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Account account = getItem(getCurrentPosition());
                            AccountManagerUtil.get(getContext())
                                    .removeAccount(account)
                                    .subscribe(new Action1<Boolean>() {
                                        @Override
                                        public void call(Boolean aBoolean) {
                                            remove(account);
                                            notifyDataSetChanged();
                                            finishActionMode();
                                        }
                                    });
                        }
                    }).show();
            return true;
        }
        return false;
    }
}
