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

package com.jaspersoft.android.jaspermobile.activities.profile.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.profile.ServerProfileActivity_;
import com.jaspersoft.android.jaspermobile.activities.profile.adapter.ServersAdapter;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.dialog.AlertDialogFragment;
import com.jaspersoft.android.jaspermobile.network.CommonRequestListener;
import com.jaspersoft.android.jaspermobile.network.ExceptionRule;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetServerInfoRequest;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.springframework.http.HttpStatus;

import eu.inmite.android.lib.dialogs.ISimpleDialogListener;
import roboguice.inject.InjectView;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
@OptionsMenu(R.menu.servers_menu)
public class ServersFragment extends RoboSpiceFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SimpleCursorAdapter.ViewBinder, AdapterView.OnItemClickListener, ISimpleDialogListener, ServersAdapter.ServersInteractionListener {
    public static final String EXTRA_SERVER_PROFILE_ID = "ServersFragment.EXTRA_SERVER_PROFILE_ID";
    public static final String TAG = ServersFragment.class.getSimpleName();

    @FragmentArg
    ViewType viewType;

    @InjectView(android.R.id.list)
    AbsListView listView;

    @Inject
    JsRestClient jsRestClient;

    @Bean
    ProfileHelper profileHelper;
    @Bean
    DefaultPrefHelper prefHelper;

    @OptionsMenuItem
    MenuItem addProfile;

    private ServersAdapter mAdapter;
    private JsServerProfile mServerProfile;
    private long mServerProfileId;

    @OptionsItem
    final void addProfile() {
        ServerProfileActivity_.intent(this).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                (viewType == ViewType.LIST) ? R.layout.common_list_layout : R.layout.common_grid_layout,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mServerProfile = jsRestClient.getServerProfile();
        if (mServerProfile != null) {
            mServerProfileId = mServerProfile.getId();
        }


        int layout = (viewType == ViewType.LIST) ? R.layout.common_list_item : R.layout.common_grid_item;
        mAdapter = new ServersAdapter(getActivity(), savedInstanceState, layout);
        mAdapter.setServersInteractionListener(this);
        mAdapter.setAdapterView(listView);
        mAdapter.setViewBinder(this);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        getActivity().getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.save(outState);
    }

    @Override
    public void onEdit(int position) {
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);

        long profileId = cursor.getLong(cursor.getColumnIndex(ServerProfilesTable._ID));

        ServerProfileActivity_.intent(getActivity())
                .profileId(profileId)
                .inEditMode(true)
                .start();
        mAdapter.finishActionMode();
    }

    @Override
    public void onDelete(int position) {
        AlertDialogFragment.createBuilder(getActivity(), getFragmentManager())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTargetFragment(this, position)
                .setTitle(R.string.warning_msg)
                .setMessage(R.string.spm_ad_delete_profile_msg)
                .setPositiveButtonText(R.string.spm_delete_btn)
                .setNegativeButtonText(R.string.spm_cancel_btn)
                .show();
    }

    @Override
    public void onClone(int position) {
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);

        long profileId = cursor.getLong(cursor.getColumnIndex(ServerProfilesTable._ID));

        ServerProfileActivity_.intent(getActivity())
                .profileId(profileId)
                .inEditMode(false)
                .start();
        mAdapter.finishActionMode();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                ServerProfilesTable.ALL_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (columnIndex == cursor.getColumnIndex(ServerProfilesTable._ID)) {
            ImageView imageView = (ImageView) view;
            if (mServerProfile == null) {
                imageView.setImageResource(R.drawable.ic_composed_server);
            } else {
                long entryId = cursor.getLong(cursor.getColumnIndex(ServerProfilesTable._ID));

                imageView.setImageResource((mServerProfileId == entryId) ?
                        R.drawable.ic_composed_active_server : R.drawable.ic_composed_server);
            }
            return true;
        }
        if (columnIndex == cursor.getColumnIndex(ServerProfilesTable.ALIAS)) {
            TextView textView = (TextView) view;
            String alias = cursor.getString(columnIndex);
            if (mServerProfile == null) {
                textView.setText(alias);
            } else {
                long entryId = cursor.getLong(cursor.getColumnIndex(ServerProfilesTable._ID));
                boolean isItemActive = (mServerProfileId == entryId);
                textView.setText(isItemActive ?
                        getString(R.string.sp_active_item, alias) : alias);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        JasperMobileApplication.removeAllCookies();

        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);

        JsServerProfile oldProfile = jsRestClient.getServerProfile();
        long profileId = cursor.getLong(cursor.getColumnIndex(ServerProfilesTable._ID));

        boolean isSameCurrentProfileSelected =
                (oldProfile != null && oldProfile.getId() == profileId);

        if (isSameCurrentProfileSelected) {
            setResultOk(profileId);
        } else {
            JsServerProfile newProfile = profileHelper.createProfileFromCursor(cursor);
            String password = newProfile.getPassword();

            boolean alwaysAskPassword = TextUtils.isEmpty(password);
            if (alwaysAskPassword) {
                setResultOk(profileId);
            } else {
                JsRestClient tmpRestClient = new JsRestClient();
                tmpRestClient.setConnectTimeout(prefHelper.getConnectTimeoutValue());
                tmpRestClient.setReadTimeout(prefHelper.getReadTimeoutValue());
                tmpRestClient.setServerProfile(newProfile);

                GetServerInfoRequest request = new GetServerInfoRequest(tmpRestClient);
                request.setRetryPolicy(null);

                setRefreshActionState(true);
                getSpiceManager().execute(
                        new GetServerInfoRequest(tmpRestClient),
                        new ValidateServerInfoListener(newProfile));
            }
        }
    }

    private void setResultOk(long profileId) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SERVER_PROFILE_ID, profileId);
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        getActivity().finish();
    }

    @Override
    public void onPositiveButtonClicked(int position) {
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);
        long id = cursor.getLong(cursor.getColumnIndex(ServerProfilesTable._ID));
        if (mServerProfileId == id) {
            Toast.makeText(getActivity(), "Can`t delete active profile", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = Uri.withAppendedPath(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI, String.valueOf(id));
        int deleteCount = getActivity().getContentResolver().delete(uri, null, null);
        if (deleteCount > 0) {
            Toast.makeText(getActivity(), R.string.spm_profile_deleted_toast, Toast.LENGTH_SHORT).show();
        }
        mAdapter.finishActionMode();
    }

    @Override
    public void onNegativeButtonClicked(int i) {
    }

    @Override
    public void onNeutralButtonClicked(int i) {
    }

    private void setRefreshActionState(boolean show) {
        // Ignore flag if we have another request to launch
        // This usually happens when user quickly switches between profiles
        if (getSpiceManager().getRequestToLaunchCount() > 0) {
            addProfile.setActionView(R.layout.actionbar_indeterminate_progress);
            return;
        }

        if (show) {
            addProfile.setActionView(R.layout.actionbar_indeterminate_progress);
        } else {
            addProfile.setActionView(null);
        }
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class ValidateServerInfoListener extends CommonRequestListener<ServerInfo> {
        private final JsServerProfile mNewProfile;

        ValidateServerInfoListener(JsServerProfile newProfile) {
            super();
            // We will handle this rule manually
            removeRule(ExceptionRule.UNAUTHORIZED);
            mNewProfile = newProfile;
        }

        @Override
        public void onSemanticFailure(SpiceException spiceException) {
            setRefreshActionState(false);

            HttpStatus statusCode = extractStatusCode(spiceException);
            if (statusCode != null && statusCode == HttpStatus.UNAUTHORIZED) {
                AlertDialogFragment
                        .createBuilder(getActivity(), getFragmentManager())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.error_msg)
                        .setMessage(ExceptionRule.UNAUTHORIZED.getMessage())
                        .setNegativeButtonText(android.R.string.ok)
                        .show();
            }
        }

        @Override
        public void onSemanticSuccess(ServerInfo serverInfo) {
            setRefreshActionState(false);

            Context context = getActivity();
            double currentVersion = serverInfo.getVersionCode();

            if (currentVersion < ServerInfo.VERSION_CODES.EMERALD_TWO) {
                AlertDialogFragment.createBuilder(context, getFragmentManager())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.error_msg)
                        .setMessage(R.string.r_error_server_not_supported)
                        .show();
            } else {
                long newProfileId = mNewProfile.getId();
                // Lets update ServerInfo snapshot for later use
                profileHelper.updateCurrentInfoSnapshot(newProfileId, serverInfo);

                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SERVER_PROFILE_ID, newProfileId);
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            }
        }

        @Override
        public Activity getCurrentActivity() {
            return getActivity();
        }
    }

}
