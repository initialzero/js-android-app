package com.jaspersoft.android.jaspermobile.auth;

import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class RestErrorReceiver extends BroadcastReceiver {
    public static final String KEY_EXCEPTION_MESSAGE = "exceptionMessage";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(JasperSettings.ACTION_INVALID_PASSWORD)) {
            String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Toast.makeText(context, context.getString(R.string.account_password_changed, accountName),
                    Toast.LENGTH_LONG).show();
        }
        if (action.equals(JasperSettings.ACTION_TOKEN_EXPIRED)) {
            // TODO reload report viewer
        }
        if (action.equals(JasperSettings.ACTION_REST_ERROR)) {
            String exceptionMessage = intent.getStringExtra(KEY_EXCEPTION_MESSAGE);
            Toast.makeText(context, exceptionMessage, Toast.LENGTH_LONG).show();
        }
    }

}
