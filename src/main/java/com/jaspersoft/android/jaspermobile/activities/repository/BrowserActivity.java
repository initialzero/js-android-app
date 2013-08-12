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

import android.content.Intent;
import android.os.Bundle;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourcesRequest;
import com.octo.android.robospice.persistence.DurationInMillis;

/**
 * @author Ivan Gadzhega
 * @since 1.0
 */
public class BrowserActivity extends BaseBrowserSearchActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent(), false);
    }

    @Override
    public boolean onSearchRequested() {
        // Provide additional data in the intent that the system sends to the searchable activity
        Intent intent = getIntent();
        Bundle appData = new Bundle();
        appData.putString(EXTRA_BC_TITLE_SMALL, intent.getStringExtra(EXTRA_BC_TITLE_LARGE));
        appData.putString(EXTRA_RESOURCE_URI, intent.getStringExtra(EXTRA_RESOURCE_URI));
        // Passing search context data
        startSearch(null, false, appData, false);
        return true;
    }

    protected void handleIntent(Intent intent, boolean forceUpdate) {
        //get extra pieces of data from intent
        Bundle extras = getIntent().getExtras();
        String subtitle = extras.getString(EXTRA_BC_TITLE_SMALL);
        String title = extras.getString(EXTRA_BC_TITLE_LARGE);
        String uri = extras.getString(EXTRA_RESOURCE_URI);

        //update titles
        if (subtitle != null && subtitle.length() > 0) {
            getSupportActionBar().setSubtitle(subtitle);
        }
        getSupportActionBar().setTitle(title);

        nothingToDisplayText.setText(R.string.loading_msg);
        setListAdapter(null);

        GetResourcesRequest request = new GetResourcesRequest(jsRestClient, uri);
        long cacheExpiryDuration = (forceUpdate) ? DurationInMillis.ALWAYS_EXPIRED : DurationInMillis.ONE_HOUR;
        serviceManager.execute(request, request.createCacheKey(), cacheExpiryDuration, new GetResourcesListener());
    }

}
