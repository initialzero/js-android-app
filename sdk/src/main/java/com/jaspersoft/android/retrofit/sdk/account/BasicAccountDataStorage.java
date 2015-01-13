/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.retrofit.sdk.account;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class BasicAccountDataStorage implements AccountDataStorage {
    private static final String PREF_NAME = BasicAccountDataStorage.class.getSimpleName();
    private static final String SERVER_COOKIE_KEY = "SERVER_COOKIE_KEY";
    private static final String SERVER_VERSION_KEY = "SERVER_VERSION_KEY";
    private static final String SERVER_EDITION_KEY = "SERVER_EDITION_KEY";

    private final SharedPreferences mPreference;

    private BasicAccountDataStorage(Context context) {
        mPreference = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
    }

    public static BasicAccountDataStorage get(Context context) {
        return new BasicAccountDataStorage(context);
    }

    public BasicAccountDataStorage putCookie(String cookie) {
        setServerCookie(cookie);
        return this;
    }

    @Override
    public void setServerCookie(String cookie) {
        putString(SERVER_COOKIE_KEY, cookie);
    }

    @Override
    public String getServerCookie() {
        return mPreference.getString(SERVER_COOKIE_KEY, "");
    }

    public BasicAccountDataStorage putVersionName(String versionName) {
        setServerVersion(versionName);
        return this;
    }

    @Override
    public void setServerVersion(String versionName) {
        putString(SERVER_VERSION_KEY, versionName);
    }

    @Override
    public String getServerVersion() {
        return mPreference.getString(SERVER_VERSION_KEY, "");
    }

    public BasicAccountDataStorage putEdition(String editionName) {
        setServerEdition(editionName);
        return this;
    }

    @Override
    public void setServerEdition(String editionName) {
        putString(SERVER_EDITION_KEY, editionName);
    }

    @Override
    public String getServerEdition() {
        return mPreference.getString(SERVER_EDITION_KEY, "");
    }

    private void putString(String key, String value) {
        if (TextUtils.isEmpty(value)) {
            throw new IllegalArgumentException(key + " value should not be empty");
        }
        mPreference.edit().putString(key, value).apply();
    }
}
