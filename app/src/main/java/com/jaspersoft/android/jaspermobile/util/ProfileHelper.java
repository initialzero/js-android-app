/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.info.ServerInfoSnapshot;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean
public class ProfileHelper {
    public static final String TAG = ProfileHelper.class.getSimpleName();

    @RootContext
    Context context;
    @Pref
    GeneralPref_ generalPref;
    @Bean
    DefaultPrefHelper defaultPrefHelper;

    @Inject
    ServerInfoSnapshot serverInfoSnapshot;
    @Inject
    JsRestClient jsRestClient;

    @AfterInject
    void injectRoboGuiceDependencies() {
        final RoboInjector injector = RoboGuice.getInjector(context);
        injector.injectMembersWithoutViews(this);
    }

    public void initJsRestClient() {
        // set timeouts
        jsRestClient.setConnectTimeout(defaultPrefHelper.getConnectTimeoutValue());
        jsRestClient.setReadTimeout(defaultPrefHelper.getReadTimeoutValue());

        // restore server profile id from preferences
        long profileId = generalPref.currentProfileId().getOr(-1);

        setCurrentServerProfile(profileId);
    }

    public void initServerInfoSnapshot() {
        long profileId = generalPref.currentProfileId().getOr(-1);
        setCurrentInfoSnapshot(profileId);
    }

    public void setCurrentInfoSnapshot(long profileId) {
        Cursor cursor = queryServerProfile(profileId);
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    cursor.moveToPosition(0);
                    serverInfoSnapshot.setProfile(new ServerProfiles(cursor));
                }
            } finally {
                cursor.close();
            }
        }
    }

    public void updateCurrentInfoSnapshot(long profileId, ServerInfo serverInfo) {
        Cursor cursor = queryServerProfile(profileId);
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    cursor.moveToPosition(0);
                    ServerProfiles profile = new ServerProfiles(cursor);
                    profile.setVersionCode(serverInfo.getVersionCode());
                    profile.setEdition(serverInfo.getEdition());

                    Uri uri = Uri.withAppendedPath(
                            JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI, String.valueOf(profileId));
                    context.getContentResolver().update(uri, profile.getContentValues(), null, null);
                    serverInfoSnapshot.setProfile(profile);
                }
            } finally {
                cursor.close();
            }
        }
    }

    public void setCurrentServerProfile(long id) {
        Cursor cursor = queryServerProfile(id);

        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    cursor.moveToPosition(0);
                    setCurrentServerProfile(cursor);
                }
            } finally {
                cursor.close();
            }
        }
    }
    // TODO replace with new SDK calls
    public JsServerProfile createProfileFromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(ServerProfilesTable._ID));
        ServerProfiles dbProfile = new ServerProfiles(cursor);
        return new JsServerProfile(id, dbProfile.getAlias(),
                dbProfile.getServerUrl(), dbProfile.getOrganization(),
                null, null);
    }

    public void setCurrentServerProfile(Cursor cursor) {
        JsServerProfile serverProfile = createProfileFromCursor(cursor);
        jsRestClient.setServerProfile(serverProfile);
    }

    private Cursor queryServerProfile(long id) {
        String where = ServerProfilesTable._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        return context.getContentResolver()
                .query(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                        ServerProfilesTable.ALL_COLUMNS, where, selectionArgs, null);
    }

}
