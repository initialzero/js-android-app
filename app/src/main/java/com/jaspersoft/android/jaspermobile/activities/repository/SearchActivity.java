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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.SearchResourcesRequest;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.ArrayList;

import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType;

/**
 * @author Ivan Gadzhega
 * @since 1.0
 */
@Deprecated
public class SearchActivity extends BaseBrowserSearchActivity {

    public static final String EXTRA_RESOURCE_TYPES = "SearchActivity.EXTRA_RESOURCE_TYPES";

    // Action Bar IDs
    private static final int ID_AB_FILTER_BY = 35;
    // Dialog IDs
    private static final int ID_D_FILTER_OPTIONS = 50;
    // Dialog options
    private static final int FILTER_BY_REPORTS = 1;
    private static final int FILTER_BY_DASHBOARDS = 2;

    private MenuItem filterItem;

    private String uri;
    private String query;
    private ArrayList<String> types;

    @Override
    public void startActivity(Intent intent) {
        // check if search intent
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Intent prevIntent = getIntent();
            intent.putExtra(EXTRA_BC_TITLE_SMALL, prevIntent.getStringExtra(EXTRA_BC_TITLE_SMALL));
            intent.putExtra(EXTRA_RESOURCE_URI, prevIntent.getStringExtra(EXTRA_RESOURCE_URI));
            intent.putExtra(EXTRA_RESOURCE_TYPES, prevIntent.getStringArrayListExtra(EXTRA_RESOURCE_TYPES));
        }

        super.startActivity(intent);
    }

    //---------------------------------------------------------------------
    // Options Menu
    //---------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        filterItem = menu.add(Menu.NONE, ID_AB_FILTER_BY, 4, R.string.s_ab_filter_by);
        filterItem.setIcon(R.drawable.ic_action_filter);
        filterItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        filterItem.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ID_AB_FILTER_BY:
                showDialog(ID_D_FILTER_OPTIONS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //---------------------------------------------------------------------
    // Dialogs
    //---------------------------------------------------------------------

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case ID_D_FILTER_OPTIONS:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.s_fd_filter_by);

                CharSequence[] options = new CharSequence[] {
                    getString(R.string.s_fd_option_all),
                    getString(R.string.s_fd_option_reports),
                    getString(R.string.s_fd_option_dashboards)
                };

                builder.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case FILTER_BY_REPORTS:
                                types = new ArrayList<String>();
                                types.add(ResourceType.reportUnit.toString());
                                break;
                            case FILTER_BY_DASHBOARDS:
                                types = new ArrayList<String>();
                                types.add(ResourceType.dashboard.toString());
                                break;
                            default:
                                types = null;
                                break;
                        }

                        getIntent().putExtra(EXTRA_RESOURCE_TYPES, types);
                        handleIntent(getIntent(), false);

                        dialog.dismiss();
                    }
                });
                return builder.create();
            default:
                return super.onCreateDialog(id);
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case ID_D_FILTER_OPTIONS: {
                ListView listView = ((AlertDialog) dialog).getListView();
                if (types != null && types.size() == 1) {
                    String type = types.get(0);
                    // Reports
                    if (ResourceType.reportUnit.toString().equals(type)) {
                        listView.setItemChecked(1, true);
                        return;
                    }
                    // Dashboards
                    if (ResourceType.dashboard.toString().equals(type)) {
                        listView.setItemChecked(2, true);
                        return;
                    }
                }
                // All other
                listView.setItemChecked(0, true);
            }
        }
    }

    //---------------------------------------------------------------------
    // Intents
    //---------------------------------------------------------------------

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent, false);
    }

    @Override
    protected void handleIntent(Intent intent, boolean forceUpdate) {
        String title = getString(R.string.s_title);
        String subtitle = intent.getStringExtra(EXTRA_BC_TITLE_SMALL);
        updateTitles(title, subtitle);

        uri = intent.getStringExtra(EXTRA_RESOURCE_URI);
        query = intent.getStringExtra(SearchManager.QUERY);
        types = intent.getStringArrayListExtra(EXTRA_RESOURCE_TYPES);

        super.handleIntent(intent, forceUpdate);
    }

    //---------------------------------------------------------------------
    // Resources
    //---------------------------------------------------------------------

    @Override
    protected void getResources(boolean ignoreCache) {
        SearchResourcesRequest request = new SearchResourcesRequest(jsRestClient, uri, query, types, true, 0);
        long cacheExpiryDuration = (forceUpdate) ? DurationInMillis.ALWAYS_EXPIRED : SettingsActivity.getRepoCacheExpirationValue(this);
        getSpiceManager().execute(request, request.createCacheKey(), cacheExpiryDuration, new SearchResourcesListener());
    }

    @Override
    protected void getResourceLookups(boolean ignoreCache) {
        if (filterItem != null) {
            filterItem.setVisible(true);
        }

        if (types == null || types.isEmpty()) {
            types = new ArrayList<String>();
            types.add(ResourceType.reportUnit.toString());
            types.add(ResourceType.dashboard.toString());
        }

        GetResourceLookupsRequest request = new GetResourceLookupsRequest(jsRestClient, uri, query, types, true, offset, LIMIT);
        long cacheExpiryDuration = ignoreCache ? DurationInMillis.ALWAYS_EXPIRED : SettingsActivity.getRepoCacheExpirationValue(this);
        getSpiceManager().execute(request, request.createCacheKey(), cacheExpiryDuration, new SearchResourceLookupsListener());
    }

}
