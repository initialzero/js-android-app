/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.retrofit.sdk.account;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface AccountDataStorage {
    void setServerCookie(String cookieValue);
    String getServerCookie();
    void setServerVersion(String versionName);
    String getServerVersion();
    void setServerEdition(String editionName);
    String getServerEdition();
}
