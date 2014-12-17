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

package com.jaspersoft.android.jaspermobile.network;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity_;
import com.jaspersoft.android.jaspermobile.dialog.AlertDialogFragment;
import com.octo.android.robospice.exception.NetworkException;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.EnumMap;

/**
 * @author Ivan Gadzhega
 * @since 1.6
 */
public class RequestExceptionHandler {

    public RequestExceptionHandler() {
        throw new AssertionError();
    }

    public static void handle(Exception exception, Activity activity, boolean finishActivity) {
        handle(exception, activity, ExceptionRule.all(), finishActivity);
    }

    public static void handle(Exception exception, Activity activity) {
        handle(exception, activity, ExceptionRule.all(), false);
    }

    public static void handle(Exception exception, Activity activity,
                              EnumMap<HttpStatus, ExceptionRule> rules) {
        handle(exception, activity, rules, false);
    }

    public static void handle(Exception exception, Activity activity,
                              EnumMap<HttpStatus, ExceptionRule> rules,
                              boolean finishActivity) {
        HttpStatus statusCode = extractStatusCode(exception);
        String message = "No message";
        if (statusCode != null) {
            if (rules.keySet().contains(statusCode)) {
                ExceptionRule rule = rules.get(statusCode);
                message = activity.getString(rule.getMessage());
                if (statusCode == HttpStatus.UNAUTHORIZED) {
                    showAuthErrorDialog(rule.getMessage(), activity, finishActivity);
                } else {
                    showErrorDialog(rule.getMessage(), activity, finishActivity);
                }
            }
        } else {
            Throwable cause = exception.getCause();
            message = cause == null ? exception.getLocalizedMessage() : cause.getLocalizedMessage();
            showErrorDialog(message, activity, finishActivity);
        }
        ExceptionLogStrategy.log(exception, activity, message);
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
        AlertDialogFragment.AlertDialogBuilder builder = AlertDialogFragment.createBuilder(activity, getSupportFragmentManager(activity))
                .setIcon(android.R.drawable.ic_dialog_alert);
        if (finishActivity) {
            builder.setNeutralButton(
                    new AlertDialogFragment.NeutralClickListener() {
                        @Override
                        public void onNeutralClick(DialogFragment fragment) {
                            if (finishActivity) activity.finish();
                        }
                    }
            ).setNeutralButtonText(android.R.string.ok);
        } else {
            builder.setPositiveButtonText(android.R.string.ok);
        }
        builder
                .setTitle(R.string.error_msg)
                .setCancelableOnTouchOutside(false)
                .setMessage(message)
                .show();
    }

    private static void showAuthErrorDialog(int messageId, Activity activity, boolean finishActivity) {
        showAuthErrorDialog(activity.getString(messageId), activity, finishActivity);
    }

    private static void showAuthErrorDialog(String message, final Activity activity, final boolean finishActivity) {
        AlertDialogFragment.createBuilder(activity, getSupportFragmentManager(activity))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(new AlertDialogFragment.PositiveClickListener() {
                    @Override
                    public void onPositiveClick(DialogFragment fragment) {
                        HomeActivity_.intent(activity)
                                .action(HomeActivity.EDIT_SERVER_PROFILE_ACTION)
                                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                .start();
                    }
                })
                .setNegativeButton(new AlertDialogFragment.NegativeClickListener() {
                    @Override
                    public void onNegativeClick(DialogFragment fragment) {
                        if (finishActivity) activity.finish();
                    }
                })
                .setNegativeButtonText(android.R.string.cancel)
                .setPositiveButtonText(android.R.string.ok)
                .setTitle(R.string.error_msg)
                .setCancelableOnTouchOutside(false)
                .setMessage(message)
                .show();
    }

    /**
     * It is dirty method will leave here until
     * we move to android.app.FragmentManager
     *
     * @param activity instance of activity should support
     *                 getSupportFragmentManager() otherwise
     *                 will return null
     * @return FragmentManager or null
     */
    @Nullable
    private static FragmentManager getSupportFragmentManager(Activity activity) {
        if (activity instanceof FragmentActivity) {
            return ((FragmentActivity) activity).getSupportFragmentManager();
        } else {
            return null;
        }
    }

}