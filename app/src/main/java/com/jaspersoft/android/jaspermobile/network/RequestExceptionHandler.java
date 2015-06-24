/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.dialog.PasswordDialogFragment;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.octo.android.robospice.exception.NetworkException;
import com.octo.android.robospice.exception.NoNetworkException;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.net.UnknownHostException;

import retrofit.RetrofitError;

/**
 * @author Ivan Gadzhega
 * @since 1.6
 */
public class RequestExceptionHandler {

    public RequestExceptionHandler() {
        throw new AssertionError();
    }

    public static void handle(Exception exception, Context context) {
        if (exception == null) {
            throw new IllegalArgumentException("Exception should not be null");
        }

        int statusCode = extractStatusCode(exception);
        if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
            showAuthErrorDialog(context);
        } else if (statusCode == JasperAccountManager.TokenException.NO_ACCOUNTS_ERROR) {
            // do nothing, app will restart automatically
        } else {
            showCommonErrorMessage(context, exception);
        }
    }


    @Nullable
    public static String extractMessage(@NonNull Context context, @Nullable Exception exception) {
        if (exception == null) {
            throw new IllegalArgumentException("Exception should not be null");
        }

        int statusCode = extractStatusCode(exception);
        String message = extractMessage(context, exception, statusCode);
        if (TextUtils.isEmpty(message)) {
            return exception.getLocalizedMessage();
        } else {
            return extractMessage(context, exception, statusCode);
        }
    }

    /**
     * Extracts HttpStatus code otherwise returns 0.
     */
    public static int extractStatusCode(@NonNull Exception exception) {
        if (exception instanceof NetworkException) {
            Throwable cause = exception.getCause();
            if (cause instanceof HttpStatusCodeException) {
                return ((HttpStatusCodeException) cause).getStatusCode().value();
            }
            Throwable tokenCause = cause.getCause();
            if (tokenCause instanceof JasperAccountManager.TokenException){
                return ((JasperAccountManager.TokenException) tokenCause).getErrorCode();
            }else if (tokenCause instanceof UnknownHostException) {
                return JasperAccountManager.TokenException.SERVER_NOT_FOUND;
            }
        } else if (exception instanceof RetrofitError && ((RetrofitError) exception).getResponse() != null) {
            return ((RetrofitError) exception).getResponse().getStatus();
        }
        return 0;
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    /**
     * Extracts Localized message otherwise returns null.
     */
    @Nullable
    private static String extractMessage(@NonNull Context context, @NonNull Exception exception, int statusCode) {
        if (statusCode == 0) {
            if (exception instanceof NoNetworkException) {
                return context.getString(R.string.no_network);
            }
            return null;
        } else {
            ExceptionRule rule = ExceptionRule.all().get(statusCode);
            if (rule == null) {
                if (statusCode == JasperAccountManager.TokenException.SERVER_NOT_FOUND) {
                    return context.getString(R.string.r_error_server_not_found);
                }

                Throwable cause = exception.getCause();
                if (cause == null) {
                    return exception.getLocalizedMessage();
                }

                Throwable tokenCause = cause.getCause();
                if (tokenCause instanceof JasperAccountManager.TokenException) {
                    return tokenCause.getLocalizedMessage();
                }

                return exception.getLocalizedMessage();
            } else {
                int messageId = rule.getMessage();
                return context.getString(messageId);
            }
        }
    }

    private static void showCommonErrorMessage(@NonNull Context context, @NonNull Exception exception) {
        String message = extractMessage(context, exception);
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    private static void showAuthErrorDialog(@NonNull Context context) {
        PasswordDialogFragment.show(getSupportFragmentManager(context));
    }

    /**
     * It is dirty method will leave here until
     * we move to android.app.FragmentManager
     *
     * @param context instance of activity should support
     *                getSupportFragmentManager() otherwise
     *                will return null
     * @return FragmentManager or null
     */
    @Nullable
    private static FragmentManager getSupportFragmentManager(Context context) {
        if (context instanceof FragmentActivity) {
            return ((FragmentActivity) context).getSupportFragmentManager();
        } else {
            return null;
        }
    }

}