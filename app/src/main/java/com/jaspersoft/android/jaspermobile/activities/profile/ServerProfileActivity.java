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
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.profile.fragment.ServersFragment;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragmentActivity;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;
import java.util.Map;

// TODO FIx Save, Update, Clone actions, handle SQLiteException
/**
 * @author Tom Koptel
 * @author Ivan Gadzhega
 * @since 1.0
 */
@EActivity(R.layout.add_account_form)
@OptionsMenu(R.menu.profile_menu)
public class ServerProfileActivity extends RoboSpiceFragmentActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOAD_PROFILE = 100;

    @Extra
    long profileId;
    @Extra
    boolean inEditMode;

    @ViewById
    EditText serverUrlEdit;
    @ViewById
    EditText organizationEdit;

    @OptionsMenuItem
    MenuItem saveAction;

    private String alias;
    private String serverUrl;
    private String organization;

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
            if (mServerProfile == null || !inEditMode) {
                mServerProfile = new ServerProfiles();
            }
            mServerProfile.setAlias(alias);
            mServerProfile.setServerUrl(serverUrl);
            mServerProfile.setOrganization(organization);
            persistProfileData();
        }
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
                    setFormFields();
                }
                break;
        }
    }

    @UiThread
    protected void setFormFields() {
        if (getActionBar() != null) {
            getActionBar().setTitle(getString(R.string.sp_bc_edit_profile));
            getActionBar().setSubtitle(mServerProfile.getAlias());
        }

        String aliasSuffix = (inEditMode ? "" : getString(R.string.sp_label_alias_clone));
        serverUrlEdit.setText(mServerProfile.getServerUrl());
        organizationEdit.setText(mServerProfile.getOrganization());
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private boolean isFormValid() {
        Map<EditText, String> valueMap = Maps.newHashMap();
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

    private void setSubmitActionState() {
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

    private void persistProfileData() {
        if (inEditMode) {
            String selection = ServerProfilesTable._ID + " =?";
            String[] selectionArgs = {String.valueOf(profileId)};
            getContentResolver().update(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                    mServerProfile.getContentValues(), selection, selectionArgs);
            Toast.makeText(this, getString(R.string.spm_profile_updated_toast, alias), Toast.LENGTH_LONG).show();
        } else {
            boolean isNewProfile = (profileId == 0);
            int toastMessage = isNewProfile ? R.string.spm_profile_created_toast
                    : R.string.spm_profile_cloned_toast;

            Uri uri = getContentResolver().insert(
                    JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                    mServerProfile.getContentValues());
            profileId = Long.valueOf(uri.getLastPathSegment());
            Toast.makeText(this, getString(toastMessage, alias), Toast.LENGTH_LONG).show();
        }
        getContentResolver().notifyChange(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI, null);
        setOkResult();
//        aliasEdit.setError(getString(R.string.sp_error_duplicate_alias));
//        aliasEdit.requestFocus();
//
//        Toast toast = Toast.makeText(this, getString(R.string.sp_error_unique_alias, alias),
//                Toast.LENGTH_SHORT);
    }

    private void setOkResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(ServersFragment.EXTRA_SERVER_PROFILE_ID, profileId);
        setResult(Activity.RESULT_OK, resultIntent);
    }



}
