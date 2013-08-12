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
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.sdk.client.async.request.DeleteResourceRequest;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.sdk.client.oxm.ResourcesList;
import com.jaspersoft.android.sdk.ui.adapters.ResourceDescriptorArrayAdapter;
import com.jaspersoft.android.sdk.ui.adapters.ResourceDescriptorComparator;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.List;

/**
 * @author Ivan Gadzhega
 * @since 1.5
 */
public abstract class BaseBrowserSearchActivity extends  BaseRepositoryActivity {

    // Action Bar IDs
    protected static final int ID_AB_FAVORITES = 31;
    protected static final int ID_AB_REFRESH = 32;
    protected static final int ID_AB_SEARCH = 33;

    protected Menu optionsMenu;

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

    @Override
    public void onCreateContextMenu(ContextMenu menu,View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        menu.add(Menu.NONE, ID_CM_DELETE, Menu.FIRST, R.string.r_cm_delete);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        boolean result = super.onContextItemSelected(item);
        if (!result && item.getItemId() == ID_CM_DELETE) {
            showDialog(ID_CM_DELETE);
            result = true;
        }
        return result;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {
            case ID_CM_DELETE:
                // Define the delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.r_ad_delete_resource_msg)
                        // the delete button handler
                        .setPositiveButton(R.string.r_delete_btn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DeleteResourceRequest request = new DeleteResourceRequest(jsRestClient, resourceDescriptor.getUriString());
                                serviceManager.execute(request, new DeleteResourceListener());
                                setRefreshActionButtonState(true);

                            }
                        })
                        // the cancel button handler
                        .setNegativeButton(R.string.r_cancel_btn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                dialog = builder.create();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    protected void setRefreshActionButtonState(boolean refreshing) {
        if (optionsMenu == null) return;

        final MenuItem refreshItem = optionsMenu.findItem(ID_AB_REFRESH);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    protected abstract void handleIntent(Intent intent, boolean forceUpdate);

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    protected class GetResourcesListener implements RequestListener<ResourcesList> {

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, BaseBrowserSearchActivity.this, true);
        }

        @Override
        public void onRequestSuccess(ResourcesList resourcesList) {
            List<ResourceDescriptor> resourceDescriptors = resourcesList.getResourceDescriptors();
            if (resourceDescriptors.isEmpty()) {
                nothingToDisplayText.setText(getNothingToDisplayString());
            } else {
                ResourceDescriptorArrayAdapter arrayAdapter = new ResourceDescriptorArrayAdapter(BaseBrowserSearchActivity.this, resourceDescriptors);
                arrayAdapter.sort(new ResourceDescriptorComparator());
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

    private class DeleteResourceListener implements RequestListener<Void> {
        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, BaseBrowserSearchActivity.this, true);
        }

        @Override
        public void onRequestSuccess(Void result) {
            //Refresh repository resources
            ((ResourceDescriptorArrayAdapter) getListAdapter()).remove(resourceDescriptor);
            ((ResourceDescriptorArrayAdapter) getListAdapter()).notifyDataSetChanged();
            // the feedback about an operation in a small popup
            Toast.makeText(getApplicationContext(), R.string.r_resource_deleted_toast, Toast.LENGTH_SHORT).show();
            setRefreshActionButtonState(false);
        }
    }

}
