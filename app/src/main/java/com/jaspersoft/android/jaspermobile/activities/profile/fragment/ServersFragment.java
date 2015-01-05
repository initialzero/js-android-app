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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.profile.ServerProfileActivity_;
import com.jaspersoft.android.jaspermobile.activities.profile.adapter.ServersAdapter;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.dialog.AlertDialogFragment;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper;
import com.jaspersoft.android.sdk.client.JsServerProfile;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

import roboguice.inject.InjectView;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
@OptionsMenu(R.menu.servers_menu)
public class ServersFragment extends RoboSpiceFragment {

    public static final String EXTRA_SERVER_PROFILE = "ServersFragment.EXTRA_SERVER_PROFILE";
    public static final String TAG = ServersFragment.class.getSimpleName();

    @Bean
    ProfileHelper profileHelper;

    @FragmentArg
    @InstanceState
    boolean whileLogin;

    @FragmentArg
    @InstanceState
    long selectedServerId;

    private ServersAdapter mAdapter;

    @InjectView(android.R.id.list)
    AbsListView listView;

    @OptionsMenuItem
    MenuItem addProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.common_list_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new ServersAdapter(getActivity(), savedInstanceState, selectedServerId);
        mAdapter.setServersInteractionListener(serverItemInteractionCallback);
        mAdapter.setAdapterView(listView);

        listView.setAdapter(mAdapter);

        getActivity().getSupportLoaderManager().initLoader(0, null, serverLoaderCallback);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.save(outState);
    }

    @OptionsItem
    final void addProfile() {
        ServerProfileActivity_.intent(this).start();
    }

    //---------------------------------------------------------------------
    // Callbacks
    //---------------------------------------------------------------------

    private LoaderManager.LoaderCallbacks<Cursor> serverLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                    ServerProfilesTable.ALL_COLUMNS, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private ServersAdapter.ServersInteractionListener serverItemInteractionCallback = new ServersAdapter.ServersInteractionListener() {
        @Override
        public void onSelect(int position) {
            Cursor cursor = mAdapter.getCursor();
            cursor.moveToPosition(position);
            long profileId = cursor.getLong(cursor.getColumnIndex(ServerProfilesTable._ID));

            if (whileLogin) {
                JsServerProfile newProfile = profileHelper.createProfileFromCursor(cursor);
                selectServer(newProfile);
            } else {
                editServer(profileId);
            }
        }

        @Override
        public void onEdit(long profileId) {
            editServer(profileId);
            mAdapter.finishActionMode();
        }

        @Override
        public void onDelete(final long profileId) {
            AlertDialogFragment.createBuilder(getActivity(), getFragmentManager())
                    .setPositiveButton(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteServer(profileId);
                            mAdapter.finishActionMode();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTargetFragment(ServersFragment.this, 0)
                    .setTitle(R.string.warning_msg)
                    .setMessage(R.string.spm_ad_delete_profile_msg)
                    .setPositiveButtonText(R.string.spm_delete_btn)
                    .setNegativeButtonText(R.string.spm_cancel_btn)
                    .show();
        }

        @Override
        public void onClone(long profileId) {
            ServerProfileActivity_.intent(getActivity())
                    .profileId(profileId)
                    .inEditMode(false)
                    .start();
            mAdapter.finishActionMode();
        }
    };

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void deleteServer(long profileId) {
        boolean activeServer = profileId == -1;
        if (activeServer) {
            Toast.makeText(getActivity(), "Can`t delete active profile", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = Uri.withAppendedPath(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI, String.valueOf(profileId));
        int deleteCount = getActivity().getContentResolver().delete(uri, null, null);
        if (deleteCount > 0) {
            Toast.makeText(getActivity(), R.string.spm_profile_deleted_toast, Toast.LENGTH_SHORT).show();
        }

    }

    private void editServer(long profileId) {
        boolean activeServer = profileId == -1;
        if (activeServer) {
            Toast.makeText(getActivity(), "Can`t edit active server profile", Toast.LENGTH_SHORT).show();
            return;
        }

        ServerProfileActivity_.intent(getActivity())
                .profileId(profileId)
                .inEditMode(true)
                .start();
    }

    private void selectServer(JsServerProfile serverProfile) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SERVER_PROFILE, serverProfile);

        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        getActivity().finish();
    }

}