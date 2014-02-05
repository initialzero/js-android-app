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

package com.jaspersoft.android.jaspermobile.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.repository.*;
import com.jaspersoft.android.jaspermobile.activities.storage.SavedReportsActivity;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.jaspermobile.db.tables.ServerProfiles;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetServerInfoRequest;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import roboguice.inject.InjectView;

import java.util.ArrayList;

import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType;

/**
 * @author Ivan Gadzhega
 * @since 1.0
 */
public class HomeActivity extends RoboSherlockActivity {

    // Special intent actions
    public static final String EDIT_SERVER_PROFILE_ACTION = "com.jaspersoft.android.jaspermobile.action.EDIT_SERVER_PROFILE";
    // Action Bar IDs
    private static final int ID_AB_SERVERS = 10;
    private static final int ID_AB_SETTINGS = 11;
    // Request Codes
    public static final int RC_UPDATE_SERVER_PROFILE = 20;
    public static final int RC_SWITCH_SERVER_PROFILE = 21;
    // Dialog IDs
    protected static final int ID_D_ASK_PASSWORD = 30;
    // Preferences
    protected static final String PREFS_NAME = "RepositoryBrowser.SharedPreferences";
    protected static final String PREFS_CURRENT_SERVER_PROFILE_ID = "CURRENT_SERVER_PROFILE_ID";

    @InjectView(R.id.profile_name_text)
    private TextView profileNameText;

    @Inject
    private JsRestClient jsRestClient;
    private DatabaseProvider dbProvider;
    private SpiceManager serviceManager;
    private MenuItem searchItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // restore server profile data
        long rowId = prefs.getLong(PREFS_CURRENT_SERVER_PROFILE_ID, -1);

        // Get the database provider
        dbProvider = new DatabaseProvider(this);
        // Get a cursor with current server profile
        Cursor cursor = dbProvider.fetchServerProfile(rowId);
        startManagingCursor(cursor);

        // bind to service
        serviceManager = new SpiceManager(JsXmlSpiceService.class);

        setContentView(R.layout.home_layout);

        getSupportActionBar().setCustomView(R.layout.home_header_logo);

        // set timeouts
        int connectTimeout = SettingsActivity.getConnectTimeoutValue(this);
        int readTimeout = SettingsActivity.getReadTimeoutValue(this);
        jsRestClient.setConnectTimeout(connectTimeout * 1000);
        jsRestClient.setReadTimeout(readTimeout * 1000);

        // check if the server profile exists in db
        if (cursor.getCount() != 0) {
            setCurrentServerProfile(rowId);
            profileNameText.setText(jsRestClient.getServerProfile().getAlias());

            //  savedInstanceState is null on first start, non-null on restart
            if (savedInstanceState == null && jsRestClient.getServerProfile().getPassword().length() == 0) {
                showDialog(ID_D_ASK_PASSWORD);
            }
        } else {
            // Launch activity to select the server profile
            Intent intent = new Intent();
            intent.setClass(this, ServerProfilesManagerActivity.class);
            startActivityForResult(intent, RC_SWITCH_SERVER_PROFILE);
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (EDIT_SERVER_PROFILE_ACTION.equals(intent.getAction())) {
            // Launch activity to edit current server profile
            Intent editIntent = new Intent();
            editIntent.setClass(this, ServerProfileActivity.class);
            editIntent.setAction(ServerProfileActivity.EDIT_SERVER_PROFILE_ACTION);
            editIntent.putExtra(ServerProfileActivity.EXTRA_SERVER_PROFILE_ID, jsRestClient.getServerProfile().getId());
            startActivityForResult(editIntent, RC_UPDATE_SERVER_PROFILE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Search
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchItem = menu.add(R.string.r_ab_search);
        searchItem.setActionView(searchView).setVisible(false);
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        // Servers
        menu.add(Menu.NONE, ID_AB_SERVERS, Menu.NONE, R.string.h_ab_servers)
                .setIcon(R.drawable.ic_action_servers)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        // Settings
        menu.add(Menu.NONE, ID_AB_SETTINGS, Menu.NONE, R.string.ab_settings)
                .setIcon(R.drawable.ic_action_settings)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case ID_AB_SERVERS:
                // Launch activity to switch the server profile
                Intent intent = new Intent();
                intent.setClass(this, ServerProfilesManagerActivity.class);
                startActivityForResult(intent, RC_SWITCH_SERVER_PROFILE);
                return true;
            case ID_AB_SETTINGS:
                // Launch the settings activity
                Intent settingsIntent = new Intent();
                settingsIntent.setClass(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void dashButtonOnClickListener(View view) {
        final ConnectivityManager conMgr =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.getState() == NetworkInfo.State.CONNECTED) {
            // online
            switch (view.getId()) {
                case R.id.home_item_repository:
                    Intent loginIntent = new Intent();
                    loginIntent.setClass(this, BrowserActivity.class);
                    loginIntent.putExtra(BrowserActivity.EXTRA_BC_TITLE_LARGE, jsRestClient.getServerProfile().getAlias());
                    loginIntent.putExtra(BrowserActivity.EXTRA_RESOURCE_URI, "/");
                    startActivity(loginIntent);
                    break;
                case R.id.home_item_library:
                    GetServerInfoRequest request = new GetServerInfoRequest(jsRestClient);
                    GetServerInfoListener listener = new GetServerInfoListener();
                    long cacheExpiryDuration = SettingsActivity.getRepoCacheExpirationValue(this);
                    serviceManager.execute(request, request.createCacheKey(), cacheExpiryDuration, listener);
                    break;
                case R.id.home_item_search:
                    searchItem.expandActionView();
                    break;
                case R.id.home_item_favorites:
                    Intent favoritesIntent = new Intent();
                    favoritesIntent.setClass(this, FavoritesActivity.class);
                    favoritesIntent.putExtra(FavoritesActivity.EXTRA_BC_TITLE_LARGE, getString(R.string.f_title));
                    startActivity(favoritesIntent);
                    break;
                case R.id.home_item_saved_reports:
                    Intent savedReportsIntent = new Intent();
                    savedReportsIntent.setClass(this, SavedReportsActivity.class);
                    startActivity(savedReportsIntent);
                    break;
            }
        } else {
            // prepare the alert box
            AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
            alertbox.setTitle(getString(R.string.h_ad_title_no_connection)).setIcon(android.R.drawable.ic_dialog_alert);
            // set the message to display
            alertbox.setMessage(getString(R.string.h_ad_msg_no_connection));
            // add a neutral button to the alert box and assign a click listener
            alertbox.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                // click listener on the alert box
                public void onClick(DialogInterface arg0, int arg1) { }
            });

            alertbox.show();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        // check if search intent
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            intent.putExtra(BaseRepositoryActivity.EXTRA_BC_TITLE_SMALL, jsRestClient.getServerProfile().getAlias());
            intent.putExtra(BaseRepositoryActivity.EXTRA_RESOURCE_URI, "/");
        }

        super.startActivity(intent);
    }

    public static void goHome(Context context) {
        // All of the other activities on top of it will be destroyed and this intent will be delivered
        // to the resumed instance of the activity (now on top), through onNewIntent()
        Intent intent = new Intent();
        intent.setClass(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle extras;
            long rowId;
            switch (requestCode) {
                case RC_UPDATE_SERVER_PROFILE:
                    // get updated server profile id from result data
                    extras = data.getExtras();
                    rowId = extras.getLong(ServerProfileActivity.EXTRA_SERVER_PROFILE_ID);

                    // update current profile
                    setCurrentServerProfile(rowId);

                    // check if the password is not specified
                    if (jsRestClient.getServerProfile().getPassword().length() == 0) {
                        showDialog(ID_D_ASK_PASSWORD);
                    }

                    // the feedback about an operation
                    Toast.makeText(this, R.string.spm_profile_updated_toast, Toast.LENGTH_SHORT).show();
                    break;

                case RC_SWITCH_SERVER_PROFILE:
                    // get selected server profile id from result data
                    extras = data.getExtras();
                    rowId = extras.getLong(ServerProfileActivity.EXTRA_SERVER_PROFILE_ID);

                    // put new server profile id to shared prefs
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    // we need an Editor object to make preference changes.
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(PREFS_CURRENT_SERVER_PROFILE_ID, rowId);
                    editor.commit();

                    setCurrentServerProfile(rowId);

                    // check if the password is not specified
                    if (jsRestClient.getServerProfile().getPassword().length() == 0) {
                        showDialog(ID_D_ASK_PASSWORD);
                    }

                    String profileName = jsRestClient.getServerProfile().getAlias();

                    // the feedback about an operation
                    String toastMsg = getString(R.string.h_server_switched_toast, profileName);
                    Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
                    // update footer
                    profileNameText.setText(profileName);
                    break;
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
            case ID_D_ASK_PASSWORD:
                // do the work to define the password Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.h_ad_title_enter_password);
                // inflate custom layout
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                final View layout = inflater.inflate(R.layout.ask_pwd_dialog_layout, (ViewGroup) findViewById(R.id.pwdDialogLayoutRoot));
                builder.setView(layout);
                // define actions
                builder.setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                JsServerProfile currentProfile = jsRestClient.getServerProfile();

                                EditText passwordEdit = (EditText) layout.findViewById(R.id.dialogPasswordEdit);
                                String password = passwordEdit.getText().toString();
                                currentProfile.setPassword(password);

                                jsRestClient.setServerProfile(currentProfile);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
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

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case ID_D_ASK_PASSWORD:
                String alias = jsRestClient.getServerProfile().getAlias();
                String org = jsRestClient.getServerProfile().getOrganization();
                String usr = jsRestClient.getServerProfile().getUsername();

                // Update username
                TextView profileNameText = (TextView) dialog.findViewById(R.id.dialogProfileNameText);
                profileNameText.setText(alias);

                // Update organization
                View organizationTableRow = dialog.findViewById(R.id.dialogOrganizationTableRow);
                TextView organizationEdit = (TextView) dialog.findViewById(R.id.dialogOrganizationText);

                if (TextUtils.isEmpty(org)) {
                    organizationTableRow.setVisibility(View.GONE);
                } else {
                    organizationEdit.setText(org);
                    organizationTableRow.setVisibility(View.VISIBLE);
                }

                // Update username
                TextView usernameEdit = (TextView) dialog.findViewById(R.id.dialogUsernameText);
                usernameEdit.setText(usr);

                // Clear password
                EditText passwordEdit = (EditText) dialog.findViewById(R.id.dialogPasswordEdit);
                passwordEdit.setText("");
                break;
        }
    }

    @Override
    protected void onStart() {
        serviceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        serviceManager.shouldStop();
        searchItem.collapseActionView();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        // close any open database object
        if (dbProvider != null) dbProvider.close();
        super.onDestroy();
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void setCurrentServerProfile(long rowId) {
        // Get a cursor with server profile
        Cursor cursor = dbProvider.fetchServerProfile(rowId);
        startManagingCursor(cursor);

        // Retrieve the column indexes for that particular server profile
        int aliasId = cursor.getColumnIndex(ServerProfiles.KEY_ALIAS);
        int urlId = cursor.getColumnIndex(ServerProfiles.KEY_SERVER_URL);
        int orgId = cursor.getColumnIndex(ServerProfiles.KEY_ORGANIZATION);
        int usrId = cursor.getColumnIndex(ServerProfiles.KEY_USERNAME);
        int pwdId = cursor.getColumnIndex(ServerProfiles.KEY_PASSWORD);

        JsServerProfile serverProfile = new JsServerProfile(rowId, cursor.getString(aliasId),
                cursor.getString(urlId), cursor.getString(orgId), cursor.getString(usrId), cursor.getString(pwdId));

        jsRestClient.setServerProfile(serverProfile);
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class GetServerInfoListener implements RequestListener<ServerInfo> {

        @Override
        public void onRequestFailure(SpiceException e) {
            RequestExceptionHandler.handle(e, HomeActivity.this, false);
        }

        @Override
        public void onRequestSuccess(ServerInfo serverInfo) {
            Intent searchIntent = new Intent();
            searchIntent.setClass(HomeActivity.this, SearchActivity.class);

            searchIntent.putExtra(BaseRepositoryActivity.EXTRA_BC_TITLE_SMALL, getString(R.string.h_library_label));
            searchIntent.putExtra(BaseRepositoryActivity.EXTRA_RESOURCE_URI, "/");

            ArrayList<String> types = new ArrayList<String>();
            types.add(ResourceType.reportUnit.toString());
            if (ServerInfo.EDITIONS.PRO.equals(serverInfo.getEdition())) {
                types.add(ResourceType.dashboard.toString());
            }
            searchIntent.putExtra(SearchActivity.EXTRA_RESOURCE_TYPES, types);

            startActivity(searchIntent);
        }
    }

}