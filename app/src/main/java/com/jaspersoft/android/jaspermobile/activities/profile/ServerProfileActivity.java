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
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.profile.fragment.ServersFragment;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileProvider;

import org.androidannotations.annotations.CheckedChange;
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
import java.util.List;
import java.util.Map;

import roboguice.activity.RoboFragmentActivity;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EActivity(R.layout.server_create_form)
@OptionsMenu(R.menu.profile_menu)
public class ServerProfileActivity extends RoboFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOAD_PROFILE = 100;
    private static final int QUERY_UNIQUENESS = 110;

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
    @ViewById
    CheckBox askPasswordCheckBox;

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
            getSupportLoaderManager().initLoader(LOAD_PROFILE, null, this);
        }
    }

    @OptionsItem(android.R.id.home)
    final void showHome() {
        super.onBackPressed();
    }

    @OptionsItem
    final void saveAction() {
        if (isFormValid()) {
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
                getSupportLoaderManager().initLoader(QUERY_UNIQUENESS, null, this);
            } else {
                String selection = ServerProfilesTable._ID + " =?";
                String[] selectionArgs = {String.valueOf(profileId)};
                getContentResolver().update(JasperMobileProvider.SERVER_PROFILES_CONTENT_URI,
                        mServerProfile.getContentValues(), selection, selectionArgs);
                Toast.makeText(this, getString(R.string.spm_profile_updated_toast, alias), Toast.LENGTH_LONG).show();
                setOkResult();
                finish();
            }
        }
    }

    @TextChange(R.id.aliasEdit)
    void onAliasTextChanges(CharSequence text) {
        alias = text.toString();
        aliasEdit.setError(null);
        setSubmitActionState();
    }

    @TextChange(R.id.serverUrlEdit)
    void onServerUrlTextChanges(CharSequence text) {
        serverUrl = text.toString();
        serverUrlEdit.setError(null);
        setSubmitActionState();
    }

    @TextChange(R.id.organizationEdit)
    void onOrganizationTextChanges(CharSequence text) {
        organization = text.toString();
    }

    @TextChange(R.id.usernameEdit)
    void onUsernameUrlTextChanges(CharSequence text) {
        username = text.toString();
        usernameEdit.setError(null);
        setSubmitActionState();
    }

    @TextChange(R.id.passwordEdit)
    void onPasswordUrlTextChanges(CharSequence text) {
        password = text.toString();
        passwordEdit.setError(null);
        setSubmitActionState();
    }

    @CheckedChange(R.id.askPasswordCheckBox)
    void checkedChangedOnAskPassword(boolean checked) {
        passwordEdit.setEnabled(!checked);
        passwordEdit.setFocusable(!checked);
        passwordEdit.setFocusableInTouchMode(!checked);
        passwordEdit.setError(null);
        passwordEdit.setText("");
        setSubmitActionState();
    }

    //---------------------------------------------------------------------
    // Implements LoaderManager.LoaderCallbacks<Cursor>
    //---------------------------------------------------------------------

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int code, Bundle bundle) {
        String selection;
        String[] selectionArgs;

        switch (code) {
            case LOAD_PROFILE:
                selection = ServerProfilesTable._ID + " =?";
                selectionArgs = new String[] {String.valueOf(profileId)};
                return new CursorLoader(this, JasperMobileProvider.SERVER_PROFILES_CONTENT_URI,
                        ServerProfilesTable.ALL_COLUMNS, selection, selectionArgs, null);
            case QUERY_UNIQUENESS:
                selection = ServerProfilesTable.ALIAS + " =?";
                selectionArgs = new String[] {alias};
                return new CursorLoader(this, JasperMobileProvider.SERVER_PROFILES_CONTENT_URI,
                        new String[] {ServerProfilesTable._ID}, selection, selectionArgs, null);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case LOAD_PROFILE:
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    updateProfileFields(new ServerProfiles(cursor));
                }
                break;
            case QUERY_UNIQUENESS:
                checkUniqueConstraintFulfilled(cursor);
                break;
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private boolean isFormValid() {
        Map<EditText, String> valueMap = Maps.newHashMap();
        valueMap.put(aliasEdit, alias);
        valueMap.put(serverUrlEdit, serverUrl);
        valueMap.put(usernameEdit, username);

        if (!askPasswordCheckBox.isChecked()) {
            valueMap.put(passwordEdit, password);
        }

        boolean isFieldValid;
        boolean formValid = true;
        for (Map.Entry<EditText, String> entry : valueMap.entrySet()) {
            isFieldValid = !TextUtils.isEmpty(entry.getValue()) && !TextUtils.isEmpty(entry.getValue().trim());
            if (!isFieldValid) {
                entry.getKey().setError(getString(R.string.sp_error_field_required));
            }
            formValid &= isFieldValid;
        }

        if (!TextUtils.isEmpty(serverUrl)) {
            String url = trimUrl(serverUrl);
            if (!URLUtil.isNetworkUrl(url)) {
                serverUrlEdit.setError(getString(R.string.sp_error_url_not_valid));
                formValid &= false;
            }
        }

        return formValid;
    }

    private void setSubmitActionState() {
        List<String> values = Lists.newArrayList(alias, serverUrl, username);
        if (!askPasswordCheckBox.isChecked()) {
            values.add(password);
        }
        boolean enabled = true;
        for (String value : values) {
            enabled &= !TextUtils.isEmpty(value) && !TextUtils.isEmpty(value.trim());
        }

        if (!TextUtils.isEmpty(serverUrl)) {
            String url = trimUrl(serverUrl);
            enabled &= URLUtil.isNetworkUrl(url);
        }

        if (saveAction != null) {
            saveAction.setIcon(enabled ? R.drawable.ic_action_submit : R.drawable.ic_action_submit_disabled);
        }
    }

    private String trimUrl(String url) {
        if (!TextUtils.isEmpty(url) && url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    @SupposeUiThread
    protected void updateProfileFields(ServerProfiles serverProfile) {
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(R.string.sp_bc_edit_profile));
            getActionBar().setSubtitle(serverProfile.getAlias());
        }

        mServerProfile = serverProfile;
        aliasEdit.setText(serverProfile.getAlias());
        serverUrlEdit.setText(serverProfile.getServerUrl());
        organizationEdit.setText(serverProfile.getOrganization());
        usernameEdit.setText(serverProfile.getUsername());

        String password = serverProfile.getPassword();
        boolean hasPassword = !TextUtils.isEmpty(password);
        passwordEdit.setEnabled(hasPassword);
        passwordEdit.setFocusable(hasPassword);
        passwordEdit.setFocusableInTouchMode(hasPassword);
        passwordEdit.setText(serverProfile.getPassword());

        askPasswordCheckBox.setChecked(!hasPassword);
    }

    // TODO: Dirty way to check unique value. Need provide pull request to RoboCop.
    @SupposeUiThread
    protected void checkUniqueConstraintFulfilled(Cursor cursor) {
        boolean entryExists = cursor.getCount() > 0;
        getSupportLoaderManager().destroyLoader(QUERY_UNIQUENESS);

        if (entryExists) {
            aliasEdit.setError(getString(R.string.sp_error_duplicate_alias));
            Toast.makeText(this, getString(R.string.sp_error_unique_alias, alias),
                    Toast.LENGTH_SHORT).show();
        } else {
            Uri uri = getContentResolver().insert(JasperMobileProvider.SERVER_PROFILES_CONTENT_URI, mServerProfile.getContentValues());
            profileId = Long.valueOf(uri.getLastPathSegment());
            Toast.makeText(this, getString(R.string.spm_profile_created_toast, alias), Toast.LENGTH_LONG).show();
            setOkResult();
            finish();
        }
    }

    private void setOkResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(ServersFragment.EXTRA_SERVER_PROFILE_ID, profileId);
        setResult(Activity.RESULT_OK, resultIntent);
    }
}
