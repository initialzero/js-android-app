/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.webview;

import android.annotation.TargetApi;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.EditText;

import com.jaspersoft.android.jaspermobile.R;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class SystemChromeClient extends WebChromeClient {

    private static final String TAG = SystemChromeClient.class.getSimpleName();
    private long MAX_QUOTA = 100 * 1024 * 1024;

    private final Context context;
    private JasperChromeClientListener jasperChromeClientListener;

    private SystemChromeClient(Context context, JasperChromeClientListener jasperChromeClientListener) {
        this.context = context;
        this.jasperChromeClientListener = jasperChromeClientListener;
    }

    public Builder newBuilder(Context context) {
        return new Builder(context)
                .withDelegateListener(jasperChromeClientListener);
    }

    public JasperChromeClientListener getDelegate() {
        return jasperChromeClientListener;
    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
        jasperChromeClientListener.onProgressChanged(view, progress);
    }

    /**
     * Tell the client to display a javascript alert dialog.
     */
    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(context);
        dlg.setMessage(message);
        dlg.setTitle("Alert");
        //Don't let alerts break the back button
        dlg.setCancelable(true);
        dlg.setPositiveButton(R.string.ok,
                new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
        dlg.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        result.cancel();
                    }
                });
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            //DO NOTHING
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    result.confirm();
                    return false;
                } else
                    return true;
            }
        });
        return true;
    }

    /**
     * Tell the client to display a confirm dialog to the user.
     */
    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(context);
        dlg.setMessage(message);
        dlg.setTitle("Confirm");
        dlg.setCancelable(true);
        dlg.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
        dlg.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
        dlg.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        result.cancel();
                    }
                });
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            //DO NOTHING
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    result.cancel();
                    return false;
                } else
                    return true;
            }
        });
        return true;
    }

    /**
     * Tell the client to display a prompt dialog to the user.
     */
    @Override
    public boolean onJsPrompt(WebView view, String origin, String message, String defaultValue, JsPromptResult result) {

        // Returning false would also show a dialog, but the default one shows the origin (ugly).
        final JsPromptResult res = result;
        AlertDialog.Builder dlg = new AlertDialog.Builder(context);
        dlg.setMessage(message);
        final EditText input = new EditText(context);
        if (defaultValue != null) {
            input.setText(defaultValue);
        }
        dlg.setView(input);
        dlg.setCancelable(false);
        dlg.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String usertext = input.getText().toString();
                        res.confirm(usertext);
                    }
                });
        dlg.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        res.cancel();
                    }
                });
        return true;
    }

    /**
     * Handle database quota exceeded notification.
     */
    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize,
                                        long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
        Timber.d(TAG, "onExceededDatabaseQuota estimatedSize: %d  currentQuota: %d  totalUsedQuota: %d", estimatedSize, currentQuota, totalUsedQuota);
        quotaUpdater.updateQuota(MAX_QUOTA);
    }


    @TargetApi(8)
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (consoleMessage.message() != null)
            Timber.d(TAG, "%s: Line %d : %s", consoleMessage.sourceId(), consoleMessage.lineNumber(), consoleMessage.message());
        jasperChromeClientListener.onConsoleMessage(consoleMessage);
        return super.onConsoleMessage(consoleMessage);
    }

    private static class EmptyChromeCallbackDelegate implements JasperChromeClientListener {
        @Override
        public void onProgressChanged(WebView view, int progress) {
        }

        @Override
        public void onConsoleMessage(ConsoleMessage consoleMessage) {
        }
    }

    public static class Builder {
        private final Context mContext;
        private JasperChromeClientListener delegateListener;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder withDelegateListener(JasperChromeClientListener delegateListener) {
            this.delegateListener = delegateListener;
            return this;
        }

        public SystemChromeClient build() {
            if (delegateListener == null) {
                delegateListener = new EmptyChromeCallbackDelegate();
            }
            return new SystemChromeClient(mContext, delegateListener);
        }
    }

}
