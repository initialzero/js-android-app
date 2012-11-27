/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.async;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity;
import com.jaspersoft.android.sdk.client.async.task.JsAsyncTask;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import roboguice.util.Ln;

/**
 * @author Volodya Sabadosh (vsabadosh@jaspersoft.com)
 * @author Ivan Gadzhega
 * @version $Id$
 */
public class AsyncTaskExceptionHandler {

    /**
     *
     * @param task  task in which exception occurred.
     * @param activity activity in which task threw the exception.
     */
    public static void handle(JsAsyncTask task, Activity activity, boolean finishActivity) {
        Exception exception = task.getTaskException();
        if (exception != null) {
            // show error dialog
            if (exception instanceof RestClientException) {
                if (exception instanceof HttpStatusCodeException) {
                    HttpStatus statusCode = ((HttpStatusCodeException) exception).getStatusCode();
                    switch (statusCode) {
                        case BAD_REQUEST:
                            showErrorDialog(R.string.error_http_400, activity, finishActivity);
                            break;
                        case UNAUTHORIZED:
                            showAuthErrorDialog(R.string.error_http_401, activity, finishActivity);
                            break;
                        case FORBIDDEN:
                            showErrorDialog(R.string.error_http_403, activity, finishActivity);
                            break;
                        case NOT_FOUND:
                            showErrorDialog(R.string.error_http_404, activity, finishActivity);
                            break;
                        case INTERNAL_SERVER_ERROR:
                            showErrorDialog(R.string.error_http_500, activity, finishActivity);
                            break;
                        case BAD_GATEWAY:
                            showErrorDialog(R.string.error_http_502, activity, finishActivity);
                            break;
                        case SERVICE_UNAVAILABLE:
                            showErrorDialog(R.string.error_http_503, activity, finishActivity);
                            break;
                        case GATEWAY_TIMEOUT:
                            showErrorDialog(R.string.error_http_504, activity, finishActivity);
                            break;
                    }
                } else {
                    showErrorDialog(exception.getLocalizedMessage(), activity, finishActivity);
                }
                // log the exception details
                Ln.e(exception);
            }  else {
                throw new RuntimeException(exception);
            }
        }
    }

    private static void showErrorDialog(int messageId, Activity activity, boolean finishActivity) {
        showErrorDialog(activity.getString(messageId), activity, finishActivity);
    }

    private static void showErrorDialog(String message, final Activity activity, final boolean finishActivity) {
        // prepare the alert box
        AlertDialog.Builder alertBox = new AlertDialog.Builder(activity);
        alertBox.setTitle(R.string.error_msg).setIcon(android.R.drawable.ic_dialog_alert);

        // set the message to display
        alertBox.setMessage(message);

        // add a neutral button to the alert box and assign a click listener
        alertBox.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    // click listener on the alert box
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (finishActivity) activity.finish();
                    }
                });

        alertBox.show();
    }

    private static void showAuthErrorDialog(int messageId, Activity activity, boolean finishActivity) {
        showAuthErrorDialog(activity.getString(messageId), activity, finishActivity);
    }

    private static void showAuthErrorDialog(String message, final Activity activity, final boolean finishActivity) {
        // prepare the alert box
        AlertDialog.Builder alertBox = new AlertDialog.Builder(activity);
        alertBox.setTitle(R.string.error_msg).setIcon(android.R.drawable.ic_dialog_alert);

        // set the message to display
        alertBox.setMessage(message);

        alertBox.setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        intent.setClass(activity, HomeActivity.class);
                        intent.setAction(HomeActivity.EDIT_SERVER_PROFILE_ACTION);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        activity.startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (finishActivity) activity.finish();
                    }
                });

        alertBox.show();
    }
    
}