/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.profile;

import android.app.ActionBar;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileProvider;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;

import roboguice.activity.RoboFragmentActivity;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EActivity(R.layout.server_create_form)
@OptionsMenu(R.menu.profile_menu)
public class ServerProfileActivity extends RoboFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    @Extra
    long profileId;

    @ViewById
    EditText aliasEdit;
    @ViewById
    EditText serverUrlEdit;
    @ViewById
    EditText organizationEdit;
    @ViewById
    EditText usernameEdit;
    @ViewById
    EditText passwordEdit;
    @OptionsMenuItem
    MenuItem saveAction;

    @InstanceState
    String alias;
    @InstanceState
    String serverUrl;
    @InstanceState
    String organization;
    @InstanceState
    String username;
    @InstanceState
    String password;

    private ServerProfiles mServerProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        invalidateOptionsMenu();

        if (profileId != 0 && savedInstanceState == null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        setSubmitActionState();
        return result;
    }

    @OptionsItem(android.R.id.home)
    final void showHome() {
        super.onBackPressed();
    }

    @OptionsItem
    final void saveAction() {
        Calendar calendar = Calendar.getInstance();
        if (mServerProfile == null) {
            mServerProfile = new ServerProfiles();
            mServerProfile.setCreatedAt(calendar.getTime().getTime());
        }
        mServerProfile.setAlias(alias);
        mServerProfile.setServerUrl(serverUrl);
        mServerProfile.setOrganization(organization);
        mServerProfile.setUsername(username);
        mServerProfile.setPassword(password);
        mServerProfile.setUpdatedAt(calendar.getTime().getTime());

        // We should create new instance
        if (profileId == 0) {
            getContentResolver().insert(JasperMobileProvider.SERVER_PROFILES_CONTENT_URI, mServerProfile.getContentValues());
            Toast.makeText(this, getString(R.string.spm_profile_created_toast, alias), Toast.LENGTH_LONG).show();
        } else {
            String selection = ServerProfilesTable._ID + " =?";
            String[] selectionArgs = {String.valueOf(profileId)};
            getContentResolver().update(JasperMobileProvider.SERVER_PROFILES_CONTENT_URI,
                    mServerProfile.getContentValues(), selection, selectionArgs);
            Toast.makeText(this, getString(R.string.spm_profile_updated_toast, alias), Toast.LENGTH_LONG).show();
        }
        finish();
    }

    @TextChange(R.id.aliasEdit)
    void onAliasTextChanges(CharSequence text) {
        alias = text.toString();
        setSubmitActionState();
    }

    @TextChange(R.id.serverUrlEdit)
    void onServerUrlTextChanges(CharSequence text) {
        serverUrl = text.toString();
        setSubmitActionState();
    }

    @TextChange(R.id.organizationEdit)
    void onOrganizationTextChanges(CharSequence text) {
        organization = text.toString();
    }

    @TextChange(R.id.usernameEdit)
    void onUsernameUrlTextChanges(CharSequence text) {
        username = text.toString();
        setSubmitActionState();
    }

    @TextChange(R.id.passwordEdit)
    void onPasswordUrlTextChanges(CharSequence text) {
        password = text.toString();
        setSubmitActionState();
    }

    private void setSubmitActionState() {
        String[] values = {alias, serverUrl, organization, username, password};
        boolean enabled = true;
        for (String value : values) {
            enabled &= !TextUtils.isEmpty(value) && !TextUtils.isEmpty(value.trim());
        }
        if (saveAction != null) {
            saveAction.setEnabled(enabled);
            saveAction.setIcon(enabled ? R.drawable.ic_action_submit : R.drawable.ic_action_submit_disabled);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection = ServerProfilesTable._ID + " =?";
        String[] selectionArgs = {String.valueOf(profileId)};
        return new CursorLoader(this, JasperMobileProvider.SERVER_PROFILES_CONTENT_URI,
                ServerProfilesTable.ALL_COLUMNS, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            updateProfileFields(new ServerProfiles(cursor));
        }
    }

    @SupposeUiThread
    protected void updateProfileFields(ServerProfiles serverProfile) {
        mServerProfile = serverProfile;
        aliasEdit.setText(serverProfile.getAlias());
        serverUrlEdit.setText(serverProfile.getServerUrl());
        organizationEdit.setText(serverProfile.getOrganization());
        usernameEdit.setText(serverProfile.getUsername());
        passwordEdit.setText(serverProfile.getPassword());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }
}
