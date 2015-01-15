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
 * <http://www.gnu.org/licenses/lgpl>./
 */

package com.jaspersoft.android.jaspermobile.info;

import android.support.v4.app.FragmentActivity;

import com.google.inject.Inject;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;
import com.jaspersoft.android.retrofit.sdk.server.DefaultVersionParser;
import com.octo.android.robospice.SpiceManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean
public class ServerInfoManager {
    @RootContext
    FragmentActivity activity;

    @Inject
    ServerInfoSnapshot mServerInfo;

    @AfterInject
    void injectRoboGuiceDependencies() {
        final RoboInjector injector = RoboGuice.getInjector(activity);
        injector.injectMembersWithoutViews(this);
    }

    public void getServerInfo(final SpiceManager spiceManager, final InfoCallback infoCallback) {
        // This situation possible while user has missing ServerInfo data missing
        // for his profile setup. Accepted situation is when user has migrated from
        // version of app 1.8 to 1.9
        if (mServerInfo.isMissing()) {
            BasicAccountProvider accountProvider = BasicAccountProvider.get(activity);
            AccountServerData serverData = AccountServerData.get(activity, accountProvider.getAccount());
            String edition = serverData.getEdition();
            double versionCode = DefaultVersionParser.getVersionCode(serverData.getVersionName());
            mServerInfo.setEdition(edition);
            mServerInfo.setVersionCode(versionCode);
        } else {
            infoCallback.onInfoReceived(mServerInfo);
        }
    }

    public static interface InfoCallback {
        void onInfoReceived(ServerInfoSnapshot serverInfo);
    }
}
