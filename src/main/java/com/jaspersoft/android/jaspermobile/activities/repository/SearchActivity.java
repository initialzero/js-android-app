/*
 * Copyright (C) 2012-2013 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.repository;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.SettingsActivity;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.SearchResourcesRequest;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.ArrayList;

import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType;

/**
 * @author Ivan Gadzhega
 * @since 1.0
 */
public class SearchActivity extends BaseBrowserSearchActivity {

    public static final String EXTRA_RESOURCE_TYPES = "SearchActivity.EXTRA_RESOURCE_TYPES";

    private String uri;
    private String query;
    private ArrayList<String> types;

    @Override
    public boolean onSearchRequested() {
        // Provide additional data in the intent that sends to the searchable activity
        Bundle appData = getIntent().getBundleExtra(SearchManager.APP_DATA);
        // Passing search context data
        startSearch(null, false, appData, false);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent, false);
    }

    protected void handleIntent(Intent intent, boolean forceUpdate) {
        Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);

        String title = getString(R.string.s_title);
        String subtitle = appData.getString(EXTRA_BC_TITLE_SMALL);
        updateTitles(title, subtitle);

        uri = appData.getString(EXTRA_RESOURCE_URI);
        query = intent.getStringExtra(SearchManager.QUERY);
        types = appData.getStringArrayList(EXTRA_RESOURCE_TYPES);

        super.handleIntent(intent, forceUpdate);
    }

    protected void getResources(boolean ignoreCache) {
        SearchResourcesRequest request = new SearchResourcesRequest(jsRestClient, uri, query, types, true, 0);
        long cacheExpiryDuration = (forceUpdate) ? DurationInMillis.ALWAYS_EXPIRED : SettingsActivity.getRepoCacheExpirationValue(this);
        serviceManager.execute(request, request.createCacheKey(), cacheExpiryDuration, new SearchResourcesListener());
    }

    protected void getResourceLookups(boolean ignoreCache) {
        if (types == null || types.isEmpty()) {
            types = new ArrayList<String>();
            types.add(ResourceType.reportUnit.toString());
            types.add(ResourceType.dashboard.toString());
        }

        GetResourceLookupsRequest request = new GetResourceLookupsRequest(jsRestClient, uri, query, types, true, offset, LIMIT);
        long cacheExpiryDuration = ignoreCache ? DurationInMillis.ALWAYS_EXPIRED : SettingsActivity.getRepoCacheExpirationValue(this);
        serviceManager.execute(request, request.createCacheKey(), cacheExpiryDuration, new SearchResourceLookupsListener());
    }

}
