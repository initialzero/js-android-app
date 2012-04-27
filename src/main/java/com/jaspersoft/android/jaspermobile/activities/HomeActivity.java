/*
 * Copyright (C) 2005 - 2012 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaspersoft.android.jaspermobile.activities;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.jaspermobile.db.tables.ServerProfiles;
import com.jaspersoft.android.jaspermobile.util.CacheUtils;
import com.jaspersoft.android.jaspermobile.util.FileUtils;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public class HomeActivity extends RoboActivity {

    // Preferences
    protected static final String PREFS_NAME = "RepositoryBrowser.SharedPreferences";
    protected static final String PREFS_CURRENT_SERVER_PROFILE_ID = "CURRENT_SERVER_PROFILE_ID";
    // Extras
    public static final String EXTRA_SERVER_PROFILE_ID = "HomeActivity.EXTRA_SERVER_PROFILE_ID";
    // Options Menu IDs
    public static final int ID_OM_SWITCH_SERVER = 10;

    @InjectView(R.id.profile_name_text)   private TextView profileNameText;

    @Inject
    private JsRestClient jsRestClient;

    private DatabaseProvider dbProvider;

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

        setContentView(R.layout.home_layout);

        // check if the server profile exists in db
        if (cursor.getCount() != 0) {
            setCurrentServerProfile(rowId);
            profileNameText.setText(jsRestClient.getServerProfile().getAlias());
        } else {
            // Launch activity to select the server profile
            Intent intent = new Intent();
            intent.setClass(this, ServerProfilesManagerActivity.class);
            startActivityForResult(intent, ID_OM_SWITCH_SERVER);
        }

        // Clear report output cache folders and, sure, do it asynchronously
        clearReportOutputCacheFolders();
    }

    public void dashButtonOnClickListener(View view) {
        final ConnectivityManager conMgr =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.getState() == NetworkInfo.State.CONNECTED) {
            // online
            switch (view.getId()) {
                case R.id.home_item_repository:
                    Intent loginIntent = new Intent();
                    loginIntent.setClass(this, RepositoryBrowserActivity.class);
                    loginIntent.putExtra(RepositoryBrowserActivity.EXTRA_BC_TITLE_LARGE, jsRestClient.getServerProfile().getAlias());
                    loginIntent.putExtra(RepositoryBrowserActivity.EXTRA_RESOURCE_URI, "/");
                    startActivity(loginIntent);
                    break;
                case R.id.home_item_reports:
                    Intent searchIntent = new Intent();
                    searchIntent.setClass(this, RepositorySearchActivity.class);
                    Bundle appData = new Bundle();
                    appData.putString(BaseRepositoryActivity.EXTRA_BC_TITLE_SMALL, getString(R.string.h_reports_label));
                    appData.putString(BaseRepositoryActivity.EXTRA_RESOURCE_URI, "/");
                    appData.putString(RepositorySearchActivity.EXTRA_RESOURCE_TYPE, ResourceDescriptor.WsType.reportUnit.toString());
                    searchIntent.putExtra(SearchManager.APP_DATA, appData);
                    startActivity(searchIntent);
                    break;
                case R.id.home_item_search:
                    onSearchRequested();
                    break;
                case R.id.home_item_favorites:
                    Intent favoritesIntent = new Intent();
                    favoritesIntent.setClass(this, RepositoryFavoritesActivity.class);
                    favoritesIntent.putExtra(RepositoryFavoritesActivity.EXTRA_BC_TITLE_LARGE, getString(R.string.f_title));
                    startActivity(favoritesIntent);
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

    /* Options Menu */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add the menu options
        menu.add(Menu.NONE, ID_OM_SWITCH_SERVER, Menu.NONE, R.string.h_om_switch_server)
                .setIcon(R.drawable.ic_menu_server_profile);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case ID_OM_SWITCH_SERVER:
                // Launch activity to switch the server profile
                Intent intent = new Intent();
                intent.setClass(this, ServerProfilesManagerActivity.class);
                startActivityForResult(intent, ID_OM_SWITCH_SERVER);
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ID_OM_SWITCH_SERVER:
                    // get selected server profile id from result data
                    Bundle extras = data.getExtras();
                    long rowId = extras.getLong(EXTRA_SERVER_PROFILE_ID);

                    // put new server profile id to shared prefs
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    // we need an Editor object to make preference changes.
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(PREFS_CURRENT_SERVER_PROFILE_ID, rowId);
                    editor.commit();

                    setCurrentServerProfile(rowId);

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
    public boolean onSearchRequested() {
        // Provide additional data in the intent that the system sends to the searchable activity
        Bundle appData = new Bundle();
        appData.putString(BaseRepositoryActivity.EXTRA_BC_TITLE_SMALL, jsRestClient.getServerProfile().getAlias());
        appData.putString(BaseRepositoryActivity.EXTRA_RESOURCE_URI, "/");
        // Passing search context data
        startSearch(null, false, appData, false);
        return true;
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

    private void clearReportOutputCacheFolders() {
        // Clear report output cache folders and, sure, do it asynchronously
        RoboAsyncTask clearCacheAsyncTask = new RoboAsyncTask<Void>() {
            @Override
            public Void call() {
                String outputDirName = JasperMobileApplication.REPORT_OUTPUT_DIR_NAME;
                Context context = contextProvider.get();
                // for internal cache
                FileUtils.deleteFilesInDirectory(new File(context.getCacheDir(), outputDirName));
                // for external cache if available
                FileUtils.deleteFilesInDirectory(new File(CacheUtils.getExternalCacheDir(context), outputDirName));
                return null;
            }

        };
        final Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(clearCacheAsyncTask.future());
    }
}
