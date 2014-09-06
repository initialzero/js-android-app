/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.SearchView;

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
import com.jaspersoft.android.sdk.ui.adapters.ResourceLookupComparator;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Gadzhega
 * @since 1.5
 */
public abstract class BaseBrowserSearchActivity extends BaseRepositoryActivity implements AbsListView.OnScrollListener {

    // Action Bar IDs
    protected static final int ID_AB_SWITCH = 31;
    protected static final int ID_AB_FAVORITES = 32;
    protected static final int ID_AB_REFRESH = 33;
    protected static final int ID_AB_SEARCH = 34;

    protected static final int LIMIT = 40;

    protected int offset, total;
    protected boolean forceUpdate;

    private Menu optionsMenu;
    private MenuItem searchItem;
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

        menu.add(Menu.NONE, ID_AB_SWITCH, 2, R.string.r_ab_switch)
                .setIcon(R.drawable.ic_collections_view_as_grid)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        // Refresh
        MenuItem item = menu.add(Menu.NONE, ID_AB_REFRESH, 3, R.string.r_ab_refresh);
        item.setIcon(R.drawable.ic_action_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setActionView(R.layout.actionbar_indeterminate_progress);

        // Search
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = new SearchView(actionBar.getThemedContext());
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchItem.collapseActionView();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });

            searchItem = menu.add(Menu.NONE, ID_AB_SEARCH, 1, R.string.r_ab_search);
            searchItem.setIcon(R.drawable.ic_action_search);
            searchItem.setActionView(searchView);
            searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }

        // Favorites
        menu.add(Menu.NONE, ID_AB_FAVORITES, 5, R.string.r_ab_favorites)
                .setIcon(R.drawable.ic_action_favorites)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case ID_AB_REFRESH:
                handleIntent(getIntent(), true);
                return true;
            case ID_AB_FAVORITES:
                Intent favoritesIntent = new Intent();
                favoritesIntent.setClass(this, FavoritesActivity.class);
                favoritesIntent.putExtra(FavoritesActivity.EXTRA_BC_TITLE_LARGE, getString(R.string.f_title));
                startActivity(favoritesIntent);
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        searchItem.collapseActionView();
        super.onStop();
    }

    protected void handleIntent(Intent intent, boolean forceUpdate) {
        this.forceUpdate = forceUpdate;

        nothingToDisplayText.setText(R.string.loading_msg);
        setListAdapter(null);

        setRefreshActionButtonState(true);

        GetServerInfoRequest request = new GetServerInfoRequest(jsRestClient);
        long cacheExpiryDuration = SettingsActivity.getRepoCacheExpirationValue(this);
        getSpiceManager().execute(request, request.createCacheKey(), cacheExpiryDuration, new GetServerInfoListener());
    }

    protected void updateTitles(String title, String subtitle) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (subtitle != null && subtitle.length() > 0) {
                actionBar.setSubtitle(subtitle);
            }
            actionBar.setTitle(title);
        }
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

    @Deprecated
    protected abstract void getResources(boolean ignoreCache);

    protected abstract void getResourceLookups(boolean ignoreCache);

    //---------------------------------------------------------------------
    // OnScrollListener
    //---------------------------------------------------------------------

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

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
            int currentVersion = serverInfo.getVersionCode();
            if (currentVersion >= ServerInfo.VERSION_CODES.EMERALD_TWO) {
                // REST v2
                offset = 0;
                getResourceLookups(forceUpdate);
                getListView().setOnScrollListener(BaseBrowserSearchActivity.this);
            } else if (currentVersion >= ServerInfo.VERSION_CODES.EMERALD) {
                // REST v1
                getResources(forceUpdate);
            } else {
                // Unsupported
                showErrorDialog();
            }

        }

        private void showErrorDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(BaseBrowserSearchActivity.this);
            builder.setTitle(R.string.error_msg);
            builder.setMessage(R.string.r_error_server_not_supported);
            builder.setNeutralButton(android.R.string.ok, null);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                builder.setIconAttribute(android.R.attr.alertDialogIcon);
            } else {
                builder.setIcon(android.R.drawable.ic_dialog_alert);
            }

            Dialog dialog = builder.create();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });

            dialog.show();
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
                arrayAdapter.sort(new ResourceLookupComparator());
                setListAdapter(arrayAdapter);
            }

            setRefreshActionButtonState(false);
        }

        protected int getNothingToDisplayString() {
            return R.string.r_browser_nothing_to_display;
        }
    }

    @Deprecated
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
                    if (getListView().getFooterViewsCount() == 0) {
                        getListView().addFooterView(progressView);
                    }
                    arrayAdapter = new ResourceLookupArrayAdapter(BaseBrowserSearchActivity.this, new ArrayList<ResourceLookup>());
                    setListAdapter(arrayAdapter);
                }

                for (ResourceLookup lookup : resourceLookups) {
                    arrayAdapter.add(lookup);
                }
            }

            setRefreshActionButtonState(false);

            if (offset == 0) {
                total = resourceLookupsList.getTotalCount();
            }

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