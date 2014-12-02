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
import android.content.Context;
import android.net.Uri;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetServerInfoRequest;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ServerInfoManager {

    @Inject
    Context context;
    @Inject
    JsRestClient jsRestClient;

    private ServerInfoSnapshot mServerInfo;

    @Inject
    public ServerInfoManager() {
        mServerInfo = new ServerInfoSnapshot();
    }

    public void getServerInfo(SpiceManager spiceManager, InfoCallback infoCallback) {
        // This situation possible while user has missing ServerInfo data missing
        // for his profile setup. Accepted situation is when user has migrated from
        // version of app 1.8 to 1.9
        if (mServerInfo.isServerInfoMissing()) {
            GetServerInfoRequest request = new GetServerInfoRequest(jsRestClient);
            spiceManager.execute(request, new GetServerInfoRequestListener(infoCallback));
        } else {
            infoCallback.onInfoReceived(mServerInfo);
        }
    }

    public void setServerInfo(ServerProfiles profile) {
        mServerInfo = new ServerInfoSnapshot(profile.getEdition(), profile.getVersioncode());
    }

    private class GetServerInfoRequestListener implements RequestListener<ServerInfo> {
        private final InfoCallback mCallback;

        public GetServerInfoRequestListener(InfoCallback infoCallback) {
            mCallback = infoCallback;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            RequestExceptionHandler.extractStatusCode(spiceException);
        }

        @Override
        public void onRequestSuccess(ServerInfo serverInfo) {
            JsServerProfile profile = jsRestClient.getServerProfile();

            // This update only one time, oly for those profile instance which
            // is misses ServerInfo data, because of data being inconsistent
            // during migration of Database
            Uri uri = Uri.withAppendedPath(
                    JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                    String.valueOf(profile.getId()));
            ContentValues contentValues = new ContentValues();
            contentValues.put(ServerProfilesTable.VERSION_CODE, serverInfo.getVersionCode());
            contentValues.put(ServerProfilesTable.EDITION, serverInfo.getEdition());
            context.getContentResolver().update(uri, contentValues, null, null);

            mServerInfo = new ServerInfoSnapshot(serverInfo.getEdition(), serverInfo.getVersionCode());
            mCallback.onInfoReceived(mServerInfo);
        }
    }

    public static interface InfoCallback {
        void onInfoReceived(ServerInfoSnapshot serverInfo);
    }
}
