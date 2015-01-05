/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
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
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragmentActivity;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;
import java.util.Map;

/**
 * @author Tom Koptel
 * @author Ivan Gadzhega
 * @since 1.0
 */
@EActivity(R.layout.server_create_form)
@OptionsMenu(R.menu.profile_menu)
public class ServerProfileActivity extends RoboSpiceFragmentActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOAD_PROFILE = 100;
    private static final int QUERY_UNIQUENESS = 110;

    @Extra
    long profileId;
    @Extra
    boolean inEditMode;

    @ViewById
    EditText aliasEdit;
    @ViewById
    EditText serverUrlEdit;
    @ViewById
    EditText organizationEdit;

    @OptionsMenuItem
    MenuItem saveAction;

    @InstanceState
    ServerProfiles mServerProfile;

    @SystemService
    InputMethodManager inputMethodManager;

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
    final void goBack() {
        super.onBackPressed();
    }

    @OptionsItem
    final void saveAction() {
        if (isFormValid()) {
            if (mServerProfile == null || !inEditMode) {
                mServerProfile = new ServerProfiles();
            }
            String alias = aliasEdit.getText().toString();
            String serverUrl = serverUrlEdit.getText().toString();
            String organization = organizationEdit.getText().toString().trim();

            mServerProfile.setAlias(alias);
            mServerProfile.setServerUrl(serverUrl);
            mServerProfile.setOrganization(organization);

            getSupportLoaderManager().initLoader(QUERY_UNIQUENESS, null, this);
        }
    }

    @TextChange(R.id.aliasEdit)
    void onAliasTextChanges() {
        aliasEdit.setError(null);
        updateSaveActionState();
    }

    @TextChange(R.id.serverUrlEdit)
    void onServerUrlTextChanges() {
        serverUrlEdit.setError(null);
        updateSaveActionState();
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
                selectionArgs = new String[]{String.valueOf(profileId)};
                return new CursorLoader(this, JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                        ServerProfilesTable.ALL_COLUMNS, selection, selectionArgs, null);
            case QUERY_UNIQUENESS:
                String alias = aliasEdit.getText().toString();
                selection = ServerProfilesTable.ALIAS + " =?";
                selectionArgs = new String[]{alias};
                if (profileId != 0) {
                    selection += " AND " + ServerProfilesTable._ID + " !=?";
                    selectionArgs = new String[]{alias, String.valueOf(profileId)};
                }
                return new CursorLoader(this, JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                        new String[]{ServerProfilesTable._ID}, selection, selectionArgs, null);
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

                    mServerProfile = new ServerProfiles(cursor);
                    updateProfileFields(cursor);
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
        String alias = aliasEdit.getText().toString();
        String serverUrl = serverUrlEdit.getText().toString();

        Map<EditText, String> valueMap = Maps.newHashMap();
        valueMap.put(aliasEdit, alias);
        valueMap.put(serverUrlEdit, serverUrl);

        boolean isFieldValid;
        boolean formValid = true;
        for (Map.Entry<EditText, String> entry : valueMap.entrySet()) {
            isFieldValid = !TextUtils.isEmpty(entry.getValue()) && !TextUtils.isEmpty(entry.getValue().trim());
            if (!isFieldValid) {
                entry.getKey().setError(getString(R.string.sp_error_field_required));
                entry.getKey().requestFocus();
            }
            formValid &= isFieldValid;
        }

        if (!TextUtils.isEmpty(serverUrl)) {
            String url = trimUrl(serverUrl);
            if (!URLUtil.isNetworkUrl(url)) {
                serverUrlEdit.setError(getString(R.string.sp_error_url_not_valid));
                serverUrlEdit.requestFocus();
                formValid &= false;
            }
        }

        return formValid;
    }

    private void updateSaveActionState() {
        String alias = aliasEdit.getText().toString();
        String serverUrl = serverUrlEdit.getText().toString();

        List<String> values = Lists.newArrayList(alias, serverUrl);

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

    @UiThread
    protected void updateProfileFields(Cursor cursor) {
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(R.string.sp_bc_edit_profile));
            getActionBar().setSubtitle(mServerProfile.getAlias());
        }

        String aliasSuffix = (inEditMode ? "" : getString(R.string.sp_label_alias_clone));
        aliasEdit.setText(mServerProfile.getAlias() + aliasSuffix);
        serverUrlEdit.setText(mServerProfile.getServerUrl());
        organizationEdit.setText(mServerProfile.getOrganization());
    }

    // TODO: Dirty way to check unique value. Need provide pull request to RoboCop.
    @UiThread
    protected void checkUniqueConstraintFulfilled(Cursor cursor) {
        boolean entryExists = cursor.getCount() > 0;
        getSupportLoaderManager().destroyLoader(QUERY_UNIQUENESS);

        if (entryExists) {
            aliasEdit.setError(getString(R.string.sp_error_duplicate_alias));
            aliasEdit.requestFocus();
            hideKeyboard();
            return;
        }

        if (inEditMode) {
            updateServerProfile();
        } else {
            createServerProfile();
        }
        finish();
    }

    private void createServerProfile() {
        getContentResolver().insert(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI, mServerProfile.getContentValues());
        getContentResolver().notifyChange(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI, null);

        boolean isNewProfile = (profileId == 0);
        int toastMessage = isNewProfile ? R.string.spm_profile_created_toast : R.string.spm_profile_cloned_toast;
        Toast.makeText(this, getString(toastMessage, mServerProfile.getAlias()), Toast.LENGTH_LONG).show();
    }

    private void updateServerProfile() {
        String selection = ServerProfilesTable._ID + " =?";
        String[] selectionArgs = {String.valueOf(profileId)};
        getContentResolver().update(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                mServerProfile.getContentValues(), selection, selectionArgs);

        Toast.makeText(this, getString(R.string.spm_profile_updated_toast, mServerProfile.getAlias()), Toast.LENGTH_LONG).show();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}