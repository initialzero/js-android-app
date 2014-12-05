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

import android.content.ContentValues;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetServerInfoRequest;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

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
    JsRestClient jsRestClient;
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
            final GetServerInfoRequest request = new GetServerInfoRequest(jsRestClient);
            spiceManager.execute(request, new GetServerInfoRequestListener(infoCallback));
        } else {
            infoCallback.onInfoReceived(mServerInfo);
        }
    }

    private class GetServerInfoRequestListener implements RequestListener<ServerInfo> {
        private final InfoCallback mCallback;

        public GetServerInfoRequestListener(InfoCallback infoCallback) {
            mCallback = infoCallback;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            if (activity != null) {
                RequestExceptionHandler.handle(spiceException, activity);
            }
        }

        @Override
        public void onRequestSuccess(ServerInfo serverInfo) {
            JsServerProfile profile = jsRestClient.getServerProfile();

            // This update only one time, oly for those profile instance which
            // is misses ServerInfo data, because of data being inconsistent
            // during migration of Database
            if (activity != null) {
                Uri uri = Uri.withAppendedPath(
                        JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                        String.valueOf(profile.getId()));
                ContentValues contentValues = new ContentValues();
                contentValues.put(ServerProfilesTable.VERSION_CODE, serverInfo.getVersionCode());
                contentValues.put(ServerProfilesTable.EDITION, serverInfo.getEdition());
                activity.getContentResolver().update(uri, contentValues, null, null);
            }

            mServerInfo.setEdition(serverInfo.getEdition());
            mServerInfo.setVersionCode(serverInfo.getVersionCode());
            mCallback.onInfoReceived(mServerInfo);
        }
    }

    public static interface InfoCallback {
        void onInfoReceived(ServerInfoSnapshot serverInfo);
    }
}
