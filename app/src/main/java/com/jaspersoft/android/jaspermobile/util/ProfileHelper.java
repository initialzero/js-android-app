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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.info.ServerInfoManager;
import com.jaspersoft.android.jaspermobile.info.ServerInfoSnapshot;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean
public class ProfileHelper {
    public static final String TAG = ProfileHelper.class.getSimpleName();

    public static final String DEFAULT_ALIAS = "Mobile Demo";
    public static final String DEFAULT_ORGANIZATION = "organization_1";
    public static final String DEFAULT_SERVER_URL = "http://mobiledemo.jaspersoft.com/jasperserver-pro";
    public static final String DEFAULT_USERNAME = "phoneuser";
    public static final String DEFAULT_PASS = "phoneuser";

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
        jsRestClient.setConnectTimeout(defaultPrefHelper.getConnectTimeoutValue() * 1000);
        jsRestClient.setReadTimeout(defaultPrefHelper.getReadTimeoutValue() * 1000);

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
                    profile.setVersioncode(serverInfo.getVersionCode());
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

    public JsServerProfile createProfileFromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(ServerProfilesTable._ID));
        ServerProfiles dbProfile = new ServerProfiles(cursor);
        return new JsServerProfile(id, dbProfile.getAlias(),
                dbProfile.getServerUrl(), dbProfile.getOrganization(),
                dbProfile.getUsername(), dbProfile.getPassword());
    }

    public void setCurrentServerProfile(Cursor cursor) {
        JsServerProfile serverProfile = createProfileFromCursor(cursor);
        jsRestClient.setServerProfile(serverProfile);
    }

    public void seedProfilesIfNeed() {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                new String[]{ServerProfilesTable._ID}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.getCount() == 0) {
                    ServerProfiles testProfile = new ServerProfiles();

                    testProfile.setAlias(DEFAULT_ALIAS);
                    testProfile.setServerUrl(DEFAULT_SERVER_URL);
                    testProfile.setOrganization(DEFAULT_ORGANIZATION);
                    testProfile.setUsername(DEFAULT_USERNAME);
                    testProfile.setPassword(DEFAULT_PASS);

                    contentResolver.insert(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI, testProfile.getContentValues());
                    populateTestInstances(contentResolver);
                }
            } finally {
                cursor.close();
            }
        }
    }

    private Cursor queryServerProfile(long id) {
        String where = ServerProfilesTable._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        return context.getContentResolver()
                .query(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                        ServerProfilesTable.ALL_COLUMNS, where, selectionArgs, null);
    }

    /**
     * Remove method when testing needs will be fulfilled
     * @param contentResolver
     */
    private void populateTestInstances(ContentResolver contentResolver) {
        InputStream is = context.getResources().openRawResource(R.raw.profiles);
        try {
            String json = IOUtils.toString(is);
            Gson gson = new Gson();
            Profiles profiles = gson.fromJson(json, Profiles.class);
            for (ServerProfiles profile : profiles.getData()) {
                // We need populate content values manually ;(
                profile.setAlias(profile.getAlias());
                profile.setServerUrl(profile.getServerUrl());
                profile.setOrganization(profile.getOrganization());
                profile.setUsername(profile.getUsername());
                profile.setPassword(profile.getPassword());

                ContentValues contentValues = profile.getContentValues();
                contentResolver.insert(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI, contentValues);
            }
        } catch (IOException e) {
            Log.w(TAG, "Ignoring population of data");
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private static class Profiles {
        private List<ServerProfiles> profiles;

        public List<ServerProfiles> getData() {
            return profiles;
        }
    }
}
