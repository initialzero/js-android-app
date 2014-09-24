/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.model.Favorites;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileProvider;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;
import java.util.Map;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean
public class FavoritesHelper {
    @RootContext
    Context context;

    @Inject
    JsRestClient jsRestClient;

    @AfterInject
    void injectRoboGuiceDependencies() {
        final RoboInjector injector = RoboGuice.getInjector(context);
        injector.injectMembersWithoutViews(this);
    }

    public Uri addToFavorites(ResourceLookup resource) {
        JsServerProfile profile = jsRestClient.getServerProfile();
        Favorites favoriteEntry = new Favorites();
        favoriteEntry.setUri(resource.getUri());
        favoriteEntry.setLabel(resource.getLabel());
        favoriteEntry.setDescription(resource.getDescription());
        favoriteEntry.setWstype(resource.getResourceType().toString());
        favoriteEntry.setUsername(profile.getUsername());
        favoriteEntry.setOrganization(profile.getOrganization());
        favoriteEntry.setServerProfilesId(profile.getId());

        return context.getContentResolver().insert(JasperMobileProvider.FAVORITES_CONTENT_URI,
                favoriteEntry.getContentValues());
    }

    public Cursor queryFavoriteByResource(ResourceLookup resource) {
        JsServerProfile jsServerProfile = jsRestClient.getServerProfile();
        Map<String, String> mapValues = Maps.newHashMap();
        mapValues.put(FavoritesTable.LABEL, resource.getLabel());
        mapValues.put(FavoritesTable.URI, resource.getUri());
        mapValues.put(FavoritesTable.WSTYPE, resource.getResourceType().toString());
        mapValues.put(FavoritesTable.USERNAME, jsServerProfile.getUsername());
        mapValues.put(FavoritesTable.ORGANIZATION, jsServerProfile.getOrganization());
        mapValues.put(FavoritesTable.SERVER_PROFILES_ID, jsServerProfile.getId() + "");

        List<String> conditions = Lists.newArrayList();
        for (Map.Entry<String, String> entry : mapValues.entrySet()) {
            if (entry.getValue() == null) {
                conditions.add(entry.getKey() + " IS NULL");
            } else {
                conditions.add(entry.getKey() + "=?");
            }
        }

        List<String> args = FluentIterable.from(mapValues.values())
                .filter(Predicates.notNull())
                .toList();
        String selection = Joiner.on(" AND ").join(conditions);
        String[] selectionArgs = new String[mapValues.values().size()];
        args.toArray(selectionArgs);

        return context.getContentResolver().query(JasperMobileProvider.FAVORITES_CONTENT_URI,
                new String[]{FavoritesTable._ID}, selection, selectionArgs, null);
    }
}
