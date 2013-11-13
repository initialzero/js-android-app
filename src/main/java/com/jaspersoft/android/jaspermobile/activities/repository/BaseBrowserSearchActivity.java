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
import android.view.View;
import android.widget.AbsListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.SettingsActivity;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetServerInfoRequest;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.sdk.client.oxm.ResourcesList;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.jaspersoft.android.sdk.ui.adapters.ResourceLookupArrayAdapter;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Ivan Gadzhega
 * @since 1.5
 */
public abstract class BaseBrowserSearchActivity extends BaseRepositoryActivity implements AbsListView.OnScrollListener {

    // Action Bar IDs
    protected static final int ID_AB_FAVORITES = 31;
    protected static final int ID_AB_REFRESH = 32;
    protected static final int ID_AB_SEARCH = 33;

    protected static final int LIMIT = 20;

    protected int offset, total;
    protected boolean forceUpdate;
    protected Menu optionsMenu;

    private boolean refreshing;
    private View progressView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressView = getLayoutInflater().inflate(R.layout.list_indeterminate_progress, null);
        handleIntent(getIntent(), false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        // Add actions to the action bar
        MenuItem item = menu.add(Menu.NONE, ID_AB_REFRESH, Menu.NONE, R.string.r_ab_refresh);
        item.setIcon(R.drawable.ic_action_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setActionView(R.layout.actionbar_indeterminate_progress);

        menu.add(Menu.NONE, ID_AB_SEARCH, Menu.NONE, R.string.r_ab_search)
                .setIcon(R.drawable.ic_action_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(Menu.NONE, ID_AB_FAVORITES, Menu.NONE, R.string.r_ab_favorites)
                .setIcon(R.drawable.ic_action_favorites).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case ID_AB_REFRESH:
                setRefreshActionButtonState(true);
                handleIntent(getIntent(), true);
                return true;
            case ID_AB_FAVORITES:
                Intent favoritesIntent = new Intent();
                favoritesIntent.setClass(this, FavoritesActivity.class);
                favoritesIntent.putExtra(FavoritesActivity.EXTRA_BC_TITLE_LARGE, getString(R.string.f_title));
                startActivity(favoritesIntent);
                return true;
            case ID_AB_SEARCH:
                onSearchRequested();
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onOptionsItemSelected(item);
        }
    }

    protected void handleIntent(Intent intent, boolean forceUpdate) {
        this.forceUpdate = forceUpdate;

        nothingToDisplayText.setText(R.string.loading_msg);
        setListAdapter(null);

        GetServerInfoRequest request = new GetServerInfoRequest(jsRestClient);
        long cacheExpiryDuration = SettingsActivity.getRepoCacheExpirationValue(this);
        serviceManager.execute(request, request.createCacheKey(), cacheExpiryDuration, new GetServerInfoListener());
    }

    protected void updateTitles(String title, String subtitle) {
        if (subtitle != null && subtitle.length() > 0) {
            getSupportActionBar().setSubtitle(subtitle);
        }
        getSupportActionBar().setTitle(title);
    }

    protected void setRefreshActionButtonState(boolean refreshing) {
        if (optionsMenu == null) return;
        this.refreshing = refreshing;

        final MenuItem refreshItem = optionsMenu.findItem(ID_AB_REFRESH);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    protected abstract void getResources(boolean ignoreCache);

    protected abstract void getResourceLookups(boolean ignoreCache);

    //---------------------------------------------------------------------
    // OnScrollListener
    //---------------------------------------------------------------------

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) { }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (!refreshing && offset + LIMIT < total && firstVisibleItem + visibleItemCount >= totalItemCount) {
            offset += LIMIT;
            setRefreshActionButtonState(true);
            getResourceLookups(forceUpdate);
        }
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class GetServerInfoListener implements RequestListener<ServerInfo> {
        @Override
        public void onRequestFailure(SpiceException e) {
            RequestExceptionHandler.handle(e, BaseBrowserSearchActivity.this, false);
        }

        @Override
        public void onRequestSuccess(ServerInfo serverInfo) {
            if (serverInfo.getVersionCode() < ServerInfo.VERSION_CODES.EMERALD_TWO) {
                getResources(forceUpdate); // REST v1
            } else {
                offset = 0;
                getResourceLookups(forceUpdate); // REST v2
                getListView().setOnScrollListener(BaseBrowserSearchActivity.this);
            }
        }
    }

    protected class GetResourcesListener implements RequestListener<ResourcesList> {
        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, BaseBrowserSearchActivity.this, true);
        }

        @Override
        public void onRequestSuccess(ResourcesList resourcesList) {
            List<ResourceLookup> resourceLookups = new ArrayList<ResourceLookup>();
            for (ResourceDescriptor descriptor : resourcesList.getResourceDescriptors()) {
                switch (descriptor.getWsType()) {
                    case folder:
                    case dashboard:
                    case reportUnit:
                        ResourceLookup lookup = new ResourceLookup();
                        lookup.setResourceType(descriptor.getWsType().toString());
                        lookup.setLabel(descriptor.getLabel());
                        lookup.setDescription(descriptor.getDescription());
                        lookup.setUri(descriptor.getUriString());
                        resourceLookups.add(lookup);
                        break;
                }
            }

            if (resourceLookups.isEmpty()) {
                nothingToDisplayText.setText(getNothingToDisplayString());
            } else {
                ResourceLookupArrayAdapter arrayAdapter =
                        new ResourceLookupArrayAdapter(BaseBrowserSearchActivity.this, resourceLookups);
                arrayAdapter.sort(new Comparator<ResourceLookup>() {
                    @Override
                    public int compare(ResourceLookup object1, ResourceLookup object2) {
                        if (object1.getResourceType() == ResourceLookup.ResourceType.folder) {
                            if (object2.getResourceType() != ResourceLookup.ResourceType.folder) {
                                return -1;
                            }
                        } else {
                            if (object2.getResourceType() == ResourceLookup.ResourceType.folder) {
                                return 1;
                            }
                        }
                        return object1.getLabel().compareToIgnoreCase(object2.getLabel());
                    }
                });
                setListAdapter(arrayAdapter);
            }

            setRefreshActionButtonState(false);
        }

        protected int getNothingToDisplayString() {
            return R.string.r_browser_nothing_to_display;
        }
    }

    protected class SearchResourcesListener extends GetResourcesListener {
        @Override
        protected int getNothingToDisplayString() {
            return R.string.r_search_nothing_to_display;
        }
    }

    protected class GetResourceLookupsListener implements RequestListener<ResourceLookupsList> {
        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, BaseBrowserSearchActivity.this, true);
        }

        @Override
        public void onRequestSuccess(ResourceLookupsList resourceLookupsList) {
            List<ResourceLookup> resourceLookups = resourceLookupsList.getResourceLookups();

            if (resourceLookups.isEmpty()) {
                nothingToDisplayText.setText(getNothingToDisplayString());
            } else {
                ResourceLookupArrayAdapter arrayAdapter = (ResourceLookupArrayAdapter) getListAdapter();
                if (arrayAdapter == null) {
                        getListView().addFooterView(progressView);
                    arrayAdapter = new ResourceLookupArrayAdapter(BaseBrowserSearchActivity.this, new ArrayList<ResourceLookup>());
                    setListAdapter(arrayAdapter);
                }

                for (ResourceLookup lookup : resourceLookups) {
                    arrayAdapter.add(lookup);
                }

                total = Math.max(resourceLookupsList.getTotalCount(), total);
            }

            setRefreshActionButtonState(false);
            if (offset + LIMIT > total) {
                getListView().removeFooterView(progressView);
            }
        }

        protected int getNothingToDisplayString() {
            return R.string.r_browser_nothing_to_display;
        }
    }

    protected class SearchResourceLookupsListener extends GetResourceLookupsListener {
        @Override
        protected int getNothingToDisplayString() {
            return R.string.r_search_nothing_to_display;
        }
    }

}
