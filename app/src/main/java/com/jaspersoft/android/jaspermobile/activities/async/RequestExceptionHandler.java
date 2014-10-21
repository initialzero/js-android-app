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

package com.jaspersoft.android.jaspermobile.activities.async;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity_;
import com.jaspersoft.android.jaspermobile.network.ExceptionRule;
import com.octo.android.robospice.exception.NetworkException;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.EnumMap;

import roboguice.util.Ln;

/**
 * @author Ivan Gadzhega
 * @since 1.6
 */
public class RequestExceptionHandler {

    public static void handle(Exception exception, Activity activity, boolean finishActivity) {
        handle(exception, activity, ExceptionRule.all(), finishActivity);
    }

    public static void handle(Exception exception, Activity activity) {
        handle(exception, activity, ExceptionRule.all(), false);
    }

    public static void handle(Exception exception, Activity activity,
                              EnumMap<HttpStatus, ExceptionRule> rules,
                              boolean finishActivity) {
        HttpStatus statusCode = extractStatusCode(exception);
        if (statusCode != null) {
            if (rules.keySet().contains(statusCode)) {
                ExceptionRule rule = rules.get(statusCode);
                if (statusCode == HttpStatus.UNAUTHORIZED) {
                    showAuthErrorDialog(rule.getMessage(), activity, finishActivity);
                } else {
                    showErrorDialog(rule.getMessage(), activity, finishActivity);
                }
            }
        } else {
            showErrorDialog(exception.getLocalizedMessage(), activity, finishActivity);
        }
        Ln.e(exception);
    }

    /**
     * Extracts HttpStatus code otherwise returns null.
     */
    @Nullable
    public static HttpStatus extractStatusCode(Exception exception) {
        if (exception instanceof NetworkException) {
            Throwable cause = exception.getCause();
            if (cause instanceof HttpStatusCodeException) {
                return ((HttpStatusCodeException) cause).getStatusCode();
            }
        }
        return null;
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

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
                        HomeActivity_.intent(activity)
                                .action(HomeActivity.EDIT_SERVER_PROFILE_ACTION)
                                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                .start();
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