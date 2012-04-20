/*
 * Copyright (C) 2005 - 2012 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of  the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public  License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaspersoft.android.jaspermobile.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;
import com.google.inject.Inject;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.jaspermobile.db.tables.ServerProfiles;
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

    private DatabaseProvider dbProvider;

    @Inject
    private JsRestClient jsRestClient;

    @InjectView(R.id.breadcrumbs_title_small)   private TextView breadCrumbsTitleSmall;
    @InjectView(R.id.breadcrumbs_title_large)   private TextView breadCrumbsTitleLarge;
    
    @InjectView(R.id.aliasEdit)         private EditText aliasEdit;
    @InjectView(R.id.serverUrlEdit)     private EditText serverUrlEdit;
    @InjectView(R.id.organizationEdit)  private EditText organizationEdit;
    @InjectView(R.id.usernameEdit)      private EditText usernameEdit;
    @InjectView(R.id.passwordEdit)      private EditText passwordEdit;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_profile_layout);

        Intent intent = getIntent();

        // Get the database provider
        dbProvider = new DatabaseProvider(this);

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
            
            //update bread crumbs
            breadCrumbsTitleSmall.setText(cursor.getString(aliasId));
            breadCrumbsTitleSmall.setVisibility(View.VISIBLE);
            breadCrumbsTitleLarge.setText(getString(R.string.sp_bc_edit_profile));
            
            // Set the server profile values to edits
            aliasEdit.setText(cursor.getString(aliasId));
            serverUrlEdit.setText(cursor.getString(urlId));
            organizationEdit.setText(cursor.getString(orgId));
            usernameEdit.setText(cursor.getString(usrId));
            passwordEdit.setText(cursor.getString(pwdId));
        } else {
            // just update bread crumbs
            breadCrumbsTitleLarge.setText(getString(R.string.sp_bc_add_profile));
        }
    }

    public void SaveButtonClickHandler(View view) {
        // Get the server profile values from edits
        String alias = aliasEdit.getText().toString();
        String url = serverUrlEdit.getText().toString();
        String org = organizationEdit.getText().toString();
        String usr = usernameEdit.getText().toString();
        String pwd = passwordEdit.getText().toString();

        // validate edits
        if(alias.length() == 0) aliasEdit.setError(getString(R.string.sp_error_field_required));
        if(!URLUtil.isValidUrl(url)) serverUrlEdit.setError(getString(R.string.sp_error_url_not_valid));
        if(usr.length() == 0) usernameEdit.setError(getString(R.string.sp_error_field_required));
        if(pwd.length() == 0) passwordEdit.setError(getString(R.string.sp_error_field_required));
        
        if (aliasEdit.getError() != null || serverUrlEdit.getError() != null
                || usernameEdit.getError() != null || passwordEdit.getError() != null) {
            return;
        }

        // add or update server profile according to the activity action
        if (EDIT_SERVER_PROFILE_ACTION.equals(getIntent().getAction())) {
            long rowId = getIntent().getLongExtra(EXTRA_SERVER_PROFILE_ID, 0);
            dbProvider.updateServerProfile(rowId, alias, url, org, usr, pwd);
        } else {
            dbProvider.insertServerProfile(alias, url, org, usr, pwd);
        }

        // result code to propagate back to the originating activity
        setResult(RESULT_OK);
        // activity is done and should be closed
        finish();

    }

    public void actionButtonOnClickListener(View view) {
        switch (view.getId()) {
            case R.id.app_icon_button:
                HomeActivity.goHome(this);
        }
    }

    @Override
    public void onDestroy() {
        // close any open database object
        if (dbProvider != null) dbProvider.close();
        super.onDestroy();
    }

}

