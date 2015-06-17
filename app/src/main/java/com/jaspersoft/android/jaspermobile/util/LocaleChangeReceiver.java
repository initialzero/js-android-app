package com.jaspersoft.android.jaspermobile.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class LocaleChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        JasperAccountManager.get(context).invalidateActiveToken();
    }
}
