/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.retrofit.sdk.rest.response;

import com.jaspersoft.android.retrofit.sdk.ojm.ServerInfo;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class LoginResponse {
    private final String mCookie;
    private final ServerInfo mServerInfo;

    public LoginResponse(String mCookie, ServerInfo mServerInfo) {
        this.mCookie = mCookie;
        this.mServerInfo = mServerInfo;
    }

    public String getCookie() {
        return mCookie;
    }

    public ServerInfo getServerInfo() {
        return mServerInfo;
    }
}
