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

package com.jaspersoft.android.jaspermobile.util.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.text.TextUtils;

/**
 * TODO provide unit tests
 * Wraps JRS instance info and provides as bundle for the needs of {@link android.accounts.AccountManager}.
 * This class generify together credentials of specific user to the specified JRS instance.
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class AccountServerData {
    public static final String ALIAS_KEY = "ALIAS_KEY";
    public static final String SERVER_URL_KEY = "SERVER_URL_KEY";
    public static final String ORGANIZATION_KEY = "ORGANIZATION_KEY";
    public static final String USERNAME_KEY = "USERNAME_KEY";

    public static final String EDITION_KEY = "EDITION_KEY";
    public static final String VERSION_NAME_KEY = "VERSION_NAME_KEY";

    private static final AccountServerData EMPTY = new AccountServerData()
            .setAlias("")
            .setServerUrl("")
            .setOrganization("")
            .setUsername("")
            .setPassword("")
            .setEdition("CE")
            .setVersionName("0.0");

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
        if (account == null) {
            return EMPTY;
        }

        String alias = accountManager.getUserData(account, ALIAS_KEY);
        String serverUrl = accountManager.getUserData(account, SERVER_URL_KEY);
        String organization = accountManager.getUserData(account, ORGANIZATION_KEY);
        String username = accountManager.getUserData(account, USERNAME_KEY);
        String edition = accountManager.getUserData(account, EDITION_KEY);
        String password = accountManager.getPassword(account);
        String versionName = accountManager.getUserData(account, VERSION_NAME_KEY);

        AccountServerData data = new AccountServerData();
        data.setAlias(alias);
        data.setServerUrl(serverUrl);
        data.setOrganization(organization);
        data.setUsername(username);
        data.setPassword(password);
        data.setEdition(edition);
        data.setVersionName(versionName);

        return data;
    }

    public AccountServerData() {
    }

    public String getAlias() {
        return alias;
    }

    public AccountServerData setAlias(String alias) {
        if (alias == null) {
            throw new IllegalArgumentException("Alias should not be null");
        }
        this.alias = alias;
        return this;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public AccountServerData setServerUrl(String serverUrl) {
        if (serverUrl == null) {
            throw new IllegalArgumentException("Server url name should not be null");
        }
        this.serverUrl = serverUrl;
        return this;
    }

    public String getOrganization() {
        return organization;
    }

    public AccountServerData setOrganization(String organization) {
        if (TextUtils.isEmpty(organization)) {
            this.organization = "";
        } else {
            this.organization = organization;
        }
        return this;
    }

    public String getUsername() {
        return username;
    }

    public AccountServerData setUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username should not be null");
        }
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
        if (edition == null) {
            throw new IllegalArgumentException("Edition should not be null");
        }
        this.edition = edition;
        return this;
    }

    public String getVersionName() {
        return versionName;
    }

    public AccountServerData setVersionName(String versionName) {
        if (versionName == null) {
            throw new IllegalArgumentException("Version name should not be null");
        }
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

    @Override
    public String toString() {
        return "AccountServerData{" +
                "versionName='" + versionName + '\'' +
                ", edition='" + edition + '\'' +
                ", username='" + username + '\'' +
                ", organization='" + organization + '\'' +
                ", serverUrl='" + serverUrl + '\'' +
                ", alias='" + alias + '\'' +
                '}';
    }

    public static class Demo {
        public static final String ALIAS = "Mobile Demo";
        public static final String SERVER_URL = "http://mobiledemo2.jaspersoft.com/jasperserver-pro";
        public static final String ORGANIZATION = "organization_1";
        public static final String USERNAME = "phoneuser";
        public static final String PASSWORD = "phoneuser";

        private Demo() {
            throw new AssertionError();
        }
    }
}
