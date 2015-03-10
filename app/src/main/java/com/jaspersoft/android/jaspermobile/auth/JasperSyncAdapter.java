package com.jaspersoft.android.jaspermobile.auth;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
public class JasperSyncAdapter extends AbstractThreadedSyncAdapter {

    public JasperSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
    }


}