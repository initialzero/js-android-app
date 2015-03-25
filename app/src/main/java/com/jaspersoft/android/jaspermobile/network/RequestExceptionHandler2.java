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

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.dialog.PasswordDialogFragment;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.octo.android.robospice.exception.NetworkException;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.net.UnknownHostException;

import retrofit.RetrofitError;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class RequestExceptionHandler2 {

    public RequestExceptionHandler2() {
        throw new AssertionError();
    }

    public static void handle(Exception exception, Context context) {
        int statusCode = extractStatusCode(exception);
        if (statusCode != 0 && statusCode == HttpStatus.UNAUTHORIZED.value()) {
            showAuthErrorDialog(context);
        } else {
            String errorMessage = extractMessage(exception, context, statusCode);
            showCommonErrorMessage(errorMessage, context);
        }
    }

    /**
     * Extracts HttpStatus code otherwise returns 0.
     */
    public static int extractStatusCode(Exception exception) {
        if (exception instanceof NetworkException) {
            Throwable cause = exception.getCause();
            if (cause instanceof HttpStatusCodeException) {
                return ((HttpStatusCodeException) cause).getStatusCode().value();
            }
            Throwable tokenCause = cause.getCause();
            if (tokenCause instanceof JasperAccountManager.TokenException) {
                return ((JasperAccountManager.TokenException) tokenCause).getErrorCode();
            } else if (tokenCause instanceof UnknownHostException) {
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
    private static String extractMessage(Exception exception, Context context, int statusCode) {
        if (statusCode == JasperAccountManager.TokenException.SERVER_NOT_FOUND) {
            return context.getString(R.string.r_error_server_not_found);
        }

        Throwable cause = exception.getCause();
        if (cause == null) {
            return exception.getLocalizedMessage();
        }
        if (cause instanceof HttpStatusCodeException) {
            ExceptionRule rule = ExceptionRule.all().get(((HttpStatusCodeException) cause).getStatusCode());
            int messageId = rule.getMessage();
            return context.getString(messageId);
        }
        Throwable tokenCause = cause.getCause();
        if (tokenCause instanceof JasperAccountManager.TokenException) {
            return tokenCause.getLocalizedMessage();
        }
        return exception.getLocalizedMessage();
    }

    private static void showCommonErrorMessage(String message, final Context context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private static void showAuthErrorDialog(final Context context) {
        PasswordDialogFragment.show(getSupportFragmentManager(context), new PasswordDialogFragment.OnPasswordChangedListener() {
            @Override
            public void onPasswordChanged(String newPassword) {
                JasperAccountManager jasperAccountManager = JasperAccountManager.get(context);
                jasperAccountManager.invalidateActiveToken();
                jasperAccountManager.updateActiveAccountPassword(newPassword);
            }
        });
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