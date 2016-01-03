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
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.exception.StatusCodes;
import com.octo.android.robospice.exception.NetworkException;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.exception.RequestCancelledException;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.net.UnknownHostException;


/**
 * @author Ivan Gadzhega
 * @since 1.6
 */
public class RequestExceptionHandler {
    private final Context mContext;

    public RequestExceptionHandler(Context context) {
        mContext = context;
    }

    public static void handle(Exception exception, Context context) {
        if (exception == null) {
            throw new IllegalArgumentException("Exception should not be null");
        }

        int statusCode = extractStatusCode(exception);
        if (statusCode == HttpStatus.UNAUTHORIZED.value() || statusCode == JasperAccountManager.TokenException.NO_PASSWORD_ERROR) {
            showAuthErrorDialog(context);
        } else if (statusCode == JasperAccountManager.TokenException.NO_ACCOUNTS_ERROR || statusCode == JasperAccountManager.TokenException.SERVER_UPDATED_ERROR) {
            // do nothing, app will restart automatically
        } else {
            showCommonErrorMessage(context, exception);
        }
    }

    @Nullable
    public String extractMessage(@Nullable Throwable exception) {
        if (exception == null) {
            throw new IllegalArgumentException("Exception should not be null");
        }

        if (exception instanceof ServiceException) {
            ServiceException serviceException = ((ServiceException) exception);
            return adaptServiceMessage(mContext, serviceException.code());
        }

        int statusCode = extractStatusCode(exception);
        String message = extractMessage(mContext, exception, statusCode);
        if (TextUtils.isEmpty(message)) {
            return exception.getLocalizedMessage();
        } else {
            return extractMessage(mContext, exception, statusCode);
        }
    }

    @Nullable
    public static String extractMessage(@NonNull Context context, @Nullable Throwable exception) {
        RequestExceptionHandler handler= new RequestExceptionHandler(context);
        return handler.extractMessage(exception);
    }

    private static String adaptServiceMessage(Context context, int code) {
        switch (code) {
            case StatusCodes.NETWORK_ERROR:
                return context.getString(R.string.no_network);
            case StatusCodes.AUTHORIZATION_ERROR:
                return context.getString(R.string.error_http_401);
            case StatusCodes.PERMISSION_DENIED_ERROR:
                return context.getString(R.string.error_http_403);
            case StatusCodes.CLIENT_ERROR:
                return context.getString(R.string.error_http_404);
            case StatusCodes.INTERNAL_ERROR:
                return context.getString(R.string.error_http_500);
            case StatusCodes.EXPORT_PAGE_OUT_OF_RANGE:
                return context.getString(R.string.rv_out_of_range);
            case StatusCodes.REPORT_EXECUTION_CANCELLED:
                return context.getString(R.string.error_report_cancelled);
            case StatusCodes.REPORT_EXECUTION_FAILED:
                return context.getString(R.string.error_report_failed);
            case StatusCodes.EXPORT_EXECUTION_FAILED:
                return context.getString(R.string.error_export_failed);
            case StatusCodes.EXPORT_EXECUTION_CANCELLED:
                return context.getString(R.string.error_export_cancelled);
            default:
                return context.getString(R.string.error_undefined);
        }
    }

    /**
     * Extracts HttpStatus code otherwise returns 0.
     */
    public static int extractStatusCode(@NonNull Throwable exception) {
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
        } else if (exception instanceof JasperAccountManager.TokenException) {
            return ((JasperAccountManager.TokenException) exception).getErrorCode();
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
    private static String extractMessage(@NonNull Context context, @NonNull Throwable exception, int statusCode) {
        if (statusCode == 0) {
            if (exception instanceof NoNetworkException) {
                return context.getString(R.string.no_network);
            }
            if (exception instanceof RequestCancelledException) {
                return context.getString(R.string.request_was_cancelled_explicitly);
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