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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.profile.ServerProfileActivity_;
import com.jaspersoft.android.jaspermobile.activities.profile.adapter.ServersAdapter;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileProvider;
import com.jaspersoft.android.jaspermobile.dialog.AlertDialogFragment;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import eu.inmite.android.lib.dialogs.ISimpleDialogListener;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class ServersFragment extends RoboFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SimpleCursorAdapter.ViewBinder, AdapterView.OnItemClickListener, ISimpleDialogListener, ServersAdapter.ServersInteractionListener {
    public static final String EXTRA_SERVER_PROFILE_ID = "ServersFragment.EXTRA_SERVER_PROFILE_ID";

    // Context menu IDs
    private static final int ID_CM_SWITCH = 20;
    private static final int ID_CM_EDIT = 21;
    private static final int ID_CM_DELETE = 22;

    @FragmentArg
    ViewType viewType;

    @InjectView(android.R.id.list)
    AbsListView listView;

    @Inject
    JsRestClient jsRestClient;

    private ServersAdapter mAdapter;
    private JsServerProfile mServerProfile;
    private long mServerProfileId;
    private AdapterView.AdapterContextMenuInfo currentInfo;

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
    public void onConnect(int position) {
        mAdapter.finishActionMode();
        onItemClick(listView, null, position, 0);
    }

    @Override
    public void onEdit(int position) {
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);

        long profileId = cursor.getLong(cursor.getColumnIndex(ServerProfilesTable._ID));

        ServerProfileActivity_.intent(getActivity()).profileId(profileId).start();
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), JasperMobileProvider.SERVER_PROFILES_CONTENT_URI,
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
                imageView.setImageResource(R.drawable.ic_composed_config_home);
            } else {
                long entryId = cursor.getLong(cursor.getColumnIndex(ServerProfilesTable._ID));

                imageView.setImageResource((mServerProfileId == entryId) ?
                        R.drawable.ic_composed_active_server : R.drawable.ic_composed_config_home);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);

        long profileId = cursor.getLong(cursor.getColumnIndex(ServerProfilesTable._ID));

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
        Uri uri = Uri.withAppendedPath(JasperMobileProvider.SERVER_PROFILES_CONTENT_URI, String.valueOf(id));
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


}
