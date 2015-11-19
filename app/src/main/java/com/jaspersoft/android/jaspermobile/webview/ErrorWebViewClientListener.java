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

import android.content.Context;
import android.webkit.WebViewClient;

import com.jaspersoft.android.jaspermobile.R;

import java.lang.ref.WeakReference;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public class ErrorWebViewClientListener implements JasperWebViewClientListener {
    private final WeakReference<Context> mContextWeakReference;
    private final OnWebViewErrorListener mErrorListener;

    public ErrorWebViewClientListener(Context context, OnWebViewErrorListener onWebViewErrorListener) {
        if (onWebViewErrorListener == null) {
            throw new IllegalArgumentException("Error listener can not be null");
        }
        mContextWeakReference = new WeakReference<Context>(context);
        mErrorListener = onWebViewErrorListener;
    }

    @Override
    public void onPageStarted(String newUrl) {
    }

    @Override
    public void onReceivedError(int errorCode, String description, String failingUrl) {
        Context context = mContextWeakReference.get();
        if (context != null) {
            delegateError(errorCode, context);
        }
    }

    private void delegateError(int errorCode, Context context) {
        String message = null;
        String title = null;
        if (errorCode == WebViewClient.ERROR_AUTHENTICATION) {
            message = context.getString(R.string.error_webview_auth_msg);
            title = context.getString(R.string.error_webview_auth_title);
        } else if (errorCode == WebViewClient.ERROR_TIMEOUT) {
            message = context.getString(R.string.error_webview_timeout_msg);
            title = context.getString(R.string.error_webview_timeout_title);
        } else if (errorCode == WebViewClient.ERROR_TOO_MANY_REQUESTS) {
            message = context.getString(R.string.error_webview_too_many_requests_msg);
            title = context.getString(R.string.error_webview_too_many_requests_title);
        } else if (errorCode == WebViewClient.ERROR_UNKNOWN) {
            message = context.getString(R.string.error_webview_generic_msg);
            title = context.getString(R.string.error_webview_generic_title);
        } else if (errorCode == WebViewClient.ERROR_BAD_URL) {
            message = context.getString(R.string.error_webview_bad_url_msg);
            title = context.getString(R.string.error_webview_bad_url_title);
        } else if (errorCode == WebViewClient.ERROR_CONNECT) {
            message = context.getString(R.string.error_webview_connect_msg);
            title = context.getString(R.string.error_webview_connect_title);
        } else if (errorCode == WebViewClient.ERROR_FAILED_SSL_HANDSHAKE) {
            message = context.getString(R.string.error_webview_ssl_handshake_msg);
            title = context.getString(R.string.error_webview_ssl_handshake_title);
        } else if (errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
            message = context.getString(R.string.error_webview_host_lookup_msg);
            title = context.getString(R.string.error_webview_host_lookup_title);
        } else if (errorCode == WebViewClient.ERROR_PROXY_AUTHENTICATION) {
            message = context.getString(R.string.error_webview_proxy_auth_msg);
            title = context.getString(R.string.error_webview_proxy_auth_title);
        } else if (errorCode == WebViewClient.ERROR_REDIRECT_LOOP) {
            message = context.getString(R.string.error_webview_redirect_loop_msg);
            title = context.getString(R.string.error_webview_redirect_loop_title);
        } else if (errorCode == WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME) {
            message = context.getString(R.string.error_webview_unsupported_auth_scheme_msg);
            title = context.getString(R.string.error_webview_unsupported_auth_scheme_title);
        } else if (errorCode == WebViewClient.ERROR_UNSUPPORTED_SCHEME) {
            message = context.getString(R.string.error_webview_unsupported_scheme_msg);
            title = context.getString(R.string.error_webview_unsupported_scheme_title);
        } else if (errorCode == WebViewClient.ERROR_FILE) {
            message = context.getString(R.string.error_webview_error_file_msg);
            title = context.getString(R.string.error_webview_error_file_title);
        } else if (errorCode == WebViewClient.ERROR_FILE_NOT_FOUND) {
            message = context.getString(R.string.error_webview_file_not_found_msg);
            title = context.getString(R.string.error_webview_file_not_found_title);
        } else if (errorCode == WebViewClient.ERROR_IO) {
            message = context.getString(R.string.error_webview_io_msg);
            title = context.getString(R.string.error_webview_io_title);
        }
        mErrorListener.onWebViewError(title, message);
    }

    @Override
    public void onPageFinishedLoading(String url) {
    }

    public interface OnWebViewErrorListener {
        void onWebViewError(String title, String message);
    }
}
