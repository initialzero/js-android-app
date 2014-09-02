/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.jaspermobile.db.tables.ServerProfiles;
import com.jaspersoft.android.sdk.client.JsRestClient;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public class ServerProfileActivity extends RoboActivity {

    // Special intent actions
    public static final String ADD_SERVER_PROFILE_ACTION = "com.jaspersoft.android.jaspermobile.action.ADD_SERVER_PROFILE";
    public static final String EDIT_SERVER_PROFILE_ACTION = "com.jaspersoft.android.jaspermobile.action.EDIT_SERVER_PROFILE";
    // Extras
    public static final String EXTRA_SERVER_PROFILE_ID = "ServerProfileActivity.EXTRA_SERVER_PROFILE_ID";

    // Action Bar IDs
    private static final int ID_AB_SETTINGS = 10;

    private DatabaseProvider dbProvider;

    @Inject
    private JsRestClient jsRestClient;
    
    @InjectView(R.id.aliasEdit)             private EditText aliasEdit;
    @InjectView(R.id.serverUrlEdit)         private EditText serverUrlEdit;
    @InjectView(R.id.organizationEdit)      private EditText organizationEdit;
    @InjectView(R.id.usernameEdit)          private EditText usernameEdit;
    @InjectView(R.id.passwordEdit)          private EditText passwordEdit;
    @InjectView(R.id.askPasswordCheckBox)   private CheckBox askPasswordCheckBox;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_profile_layout);

        Intent intent = getIntent();

        // Get the database provider
        dbProvider = new DatabaseProvider(this);
        ActionBar actionBar = getActionBar();

        if (EDIT_SERVER_PROFILE_ACTION.equals(intent.getAction())) {
            // Get a cursor with selected server profile
            long rowId = intent.getLongExtra(EXTRA_SERVER_PROFILE_ID, 0);
            Cursor cursor = dbProvider.fetchServerProfile(rowId);
            // allow the activity to take care of managing the given Cursor's lifecycle
            startManagingCursor(cursor);

            // Retrieve the column indexes for that particular server profile
            int aliasId = cursor.getColumnIndex(ServerProfiles.KEY_ALIAS);
            int urlId = cursor.getColumnIndex(ServerProfiles.KEY_SERVER_URL);
            int orgId = cursor.getColumnIndex(ServerProfiles.KEY_ORGANIZATION);
            int usrId = cursor.getColumnIndex(ServerProfiles.KEY_USERNAME);
            int pwdId = cursor.getColumnIndex(ServerProfiles.KEY_PASSWORD);

            // update action bar
            if (actionBar != null) {
                actionBar.setTitle(R.string.sp_bc_edit_profile);
                actionBar.setSubtitle(cursor.getString(aliasId));
            }

            // Set the server profile values to edits
            aliasEdit.setText(cursor.getString(aliasId));
            serverUrlEdit.setText(cursor.getString(urlId));
            organizationEdit.setText(cursor.getString(orgId));
            usernameEdit.setText(cursor.getString(usrId));

            String pwd = cursor.getString(pwdId);
            if (pwd.length() == 0) {
                passwordEdit.setEnabled(false);
                passwordEdit.setFocusable(false);
                passwordEdit.setFocusableInTouchMode(false);

                askPasswordCheckBox.setChecked(true);
            } else {
                passwordEdit.setText(cursor.getString(pwdId));
            }
        } else {
            // just update title
            if (actionBar != null) {
                actionBar.setTitle(R.string.sp_bc_add_profile);
            }
        }
    }

    public void onAskPasswordCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        passwordEdit.setEnabled(!checked);
        passwordEdit.setFocusable(!checked);
        passwordEdit.setFocusableInTouchMode(!checked);
        passwordEdit.setError(null);
    }

    public void saveButtonClickHandler(View view) {
        // Get the server profile values from edits
        String alias = aliasEdit.getText().toString();
        String url = serverUrlEdit.getText().toString();
        String org = organizationEdit.getText().toString();
        String usr = usernameEdit.getText().toString();
        String pwd = (askPasswordCheckBox.isChecked()) ? "" : passwordEdit.getText().toString() ;

        // validate edits
        if(alias.length() == 0) aliasEdit.setError(getString(R.string.sp_error_field_required));
        if(url.endsWith("/")) url = url.substring(0, url.length()-1);
        if(!URLUtil.isValidUrl(url)) serverUrlEdit.setError(getString(R.string.sp_error_url_not_valid));
        if(usr.length() == 0) usernameEdit.setError(getString(R.string.sp_error_field_required));

        if (!askPasswordCheckBox.isChecked() && pwd.length() == 0) {
            passwordEdit.setError(getString(R.string.sp_error_field_required));
        }
        
        if (aliasEdit.getError() != null || serverUrlEdit.getError() != null
                || usernameEdit.getError() != null || passwordEdit.getError() != null) {
            return;
        }

        // add or update server profile according to the activity action
        long rowId;
        if (EDIT_SERVER_PROFILE_ACTION.equals(getIntent().getAction())) {
            rowId = getIntent().getLongExtra(EXTRA_SERVER_PROFILE_ID, 0);
            dbProvider.updateServerProfile(rowId, alias, url, org, usr, pwd);
        } else {
            rowId = dbProvider.insertServerProfile(alias, url, org, usr, pwd);
        }

        // return result with specified server profile id to parent activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SERVER_PROFILE_ID, rowId);
        // result code to propagate back to the originating activity
        setResult(RESULT_OK, resultIntent);
        // activity is done and should be closed
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // use the App Icon for Navigation
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // Add actions to the action bar
        menu.add(Menu.NONE, ID_AB_SETTINGS, Menu.NONE, R.string.ab_settings)
                .setIcon(R.drawable.ic_action_settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case ID_AB_SETTINGS:
                // Launch the settings activity
                Intent settingsIntent = new Intent();
                settingsIntent.setClass(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case android.R.id.home:
                // go to the server profiles manager
                Intent intent = new Intent();
                intent.setClass(this, ServerProfilesManagerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        // close any open database object
        if (dbProvider != null) dbProvider.close();
        super.onDestroy();
    }

}

