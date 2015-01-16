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

package com.jaspersoft.android.jaspermobile.legacy;

import android.content.Context;

import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;
import com.jaspersoft.android.sdk.client.JsServerProfile;

/**
 * Util class which simplifies migration from legacy code to new SDK paradigms.
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class ProfileManager {

    public static JsServerProfile getServerProfile(Context context) {
        BasicAccountProvider accountProvider = BasicAccountProvider.get(context);
        AccountServerData serverData = AccountServerData.get(context, accountProvider.getAccount());
        return getServerProfile(serverData);
    }

    public static JsServerProfile getServerProfile(AccountServerData serverData) {
        JsServerProfile profile = new JsServerProfile();
        profile.setServerUrl(serverData.getServerUrl());
        profile.setOrganization(serverData.getOrganization());
        profile.setUsername(serverData.getUsername());
        profile.setPassword(serverData.getPassword());
        return profile;
    }

}
