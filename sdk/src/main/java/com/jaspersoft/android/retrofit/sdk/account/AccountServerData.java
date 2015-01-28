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

package com.jaspersoft.android.retrofit.sdk.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;

/**
 * TODO provide unit tests
 * Wraps JRS instance info and provides as bundle for the needs of {@link android.accounts.AccountManager}.
 * This class generify together credentials of specific user to the specified JRS instance.
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class AccountServerData {
    private static final String ALIAS_KEY = "ALIAS_KEY";
    private static final String SERVER_URL_KEY = "SERVER_URL_KEY";
    private static final String ORGANIZATION_KEY = "ORGANIZATION_KEY";
    private static final String USERNAME_KEY = "USERNAME_KEY";
    public static final String EDITION_KEY = "EDITION_KEY";
    public static final String VERSION_NAME_KEY = "VERSION_NAME_KEY";

    private String alias;
    private String serverUrl;
    private String organization;
    private String username;
    private String password;
    private String edition;
    private String versionName;
    private String serverCookie;

    public static AccountServerData get(Context context, Account account) {
        AccountManager accountManager = AccountManager.get(context);

        return new AccountServerData()
                .setAlias(accountManager.getUserData(account, ALIAS_KEY))
                .setServerUrl(accountManager.getUserData(account, SERVER_URL_KEY))
                .setOrganization(accountManager.getUserData(account, ORGANIZATION_KEY))
                .setUsername(accountManager.getUserData(account, USERNAME_KEY))
                .setPassword(accountManager.getPassword(account))
                .setEdition(accountManager.getUserData(account, EDITION_KEY))
                .setVersionName(accountManager.getUserData(account, VERSION_NAME_KEY));
    }

    public AccountServerData() {
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(ALIAS_KEY, alias);
        bundle.putString(SERVER_URL_KEY, serverUrl);
        bundle.putString(ORGANIZATION_KEY, organization);
        bundle.putString(USERNAME_KEY, username);
        bundle.putString(EDITION_KEY, edition);
        bundle.putString(VERSION_NAME_KEY, versionName);
        return bundle;
    }

    public String getAlias() {
        return alias;
    }

    public AccountServerData setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public AccountServerData setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        return this;
    }

    public String getOrganization() {
        return organization;
    }

    public AccountServerData setOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public AccountServerData setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public AccountServerData setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEdition() {
        return edition;
    }

    public AccountServerData setEdition(String edition) {
        this.edition = edition;
        return this;
    }

    public String getVersionName() {
        return versionName;
    }

    public AccountServerData setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    public String getServerCookie() {
        return serverCookie;
    }

    public AccountServerData setServerCookie(String serverCookie) {
        this.serverCookie = serverCookie;
        return this;
    }

    public static class Demo {
        public static final String ALIAS = "Mobile Demo";
        public static final String SERVER_URL = "http://mobiledemo.jaspersoft.com/jasperserver-pro";
        public static final String ORGANIZATION = "organization_1";
        public static final String USERNAME = "phoneuser";
        public static final String PASSWORD = "phoneuser";

        private Demo() {
            throw new AssertionError();
        }
    }
}
