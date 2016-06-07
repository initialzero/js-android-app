/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.data.cache.profile.PreferencesActiveProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.util.JasperSettings;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class UtilReceiver extends BroadcastReceiver {
    private static final String REMOVE_COOKIES = "jaspermobile.util.action.REMOVE_COOKIES";
    private static final String REMOVE_ALL_ACCOUNTS = "jaspermobile.util.action.REMOVE_ALL_ACCOUNTS";
    private static final String INVALIDATE_PASSWORD = "jaspermobile.util.action.INVALIDATE_PASSWORD";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(REMOVE_COOKIES)) {
            deleteToken();
            showMessage(context, "Cookies removed");
        } else if (action.equals(REMOVE_ALL_ACCOUNTS)) {
            removeAccounts(context);
            showMessage(context, "Accounts removed");
        } else if (action.equals(INVALIDATE_PASSWORD)) {
            invalidatePassword(context);
            deleteToken();
            showMessage(context, "Password invalidated and cookies removed");
        }
    }

    private void invalidatePassword(Context context) {
        PreferencesActiveProfileCache activeProfileCache = new PreferencesActiveProfileCache(context);
        Profile profile = activeProfileCache.get();

        AccountManager manager = AccountManager.get(context);
        Account account = new Account(profile.getKey(), JasperSettings.JASPER_ACCOUNT_TYPE);
        manager.setPassword(account, null);
    }

    private void deleteToken() {
        getCookieStore().removeAll();
    }

    private CookieStore getCookieStore() {
        CookieManager manager = (CookieManager) CookieManager.getDefault();
        if (manager == null) {
            return new NullCookieStore();
        }
        return manager.getCookieStore();
    }

    private void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void removeAccounts(Context context) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType(JasperSettings.JASPER_ACCOUNT_TYPE);
        for (Account account : accounts) {
            manager.removeAccountExplicitly(account);
        }
    }

    private static class NullCookieStore implements CookieStore {
        @Override
        public void add(URI uri, HttpCookie cookie) {
        }

        @Override
        public List<HttpCookie> get(URI uri) {
            return null;
        }

        @Override
        public List<HttpCookie> getCookies() {
            return null;
        }

        @Override
        public List<URI> getURIs() {
            return null;
        }

        @Override
        public boolean remove(URI uri, HttpCookie cookie) {
            return false;
        }

        @Override
        public boolean removeAll() {
            return false;
        }
    }
}