/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
 * http://www.jasperforge.org/projects/androidmobile
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.inject.Inject;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.jaspermobile.db.tables.ServerProfiles;
import roboguice.activity.RoboListActivity;
import roboguice.inject.InjectView;

/**
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public class ServerProfilesManagerActivity extends RoboListActivity {

    // Options Menu IDs
    public static final int ID_OM_ADD_SERVER_PROFILE = 10;
    // Context menu IDs
    public static final int ID_CM_SWITCH = 20;
    public static final int ID_CM_EDIT = 21;
    public static final int ID_CM_DELETE = 22;

    private DatabaseProvider dbProvider;

    private long selectedItemId;

    @InjectView(R.id.breadcrumbs_title_large)   private TextView breadCrumbsTitleLarge;
    @InjectView(R.id.app_icon_button)           private ImageButton appIconButton;
    @InjectView(R.id.action_add_button)         private ImageButton addButton;
    @InjectView(android.R.id.list)              private ListView listView;

    @Inject private JsRestClient jsRestClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_profiles_manager_layout);

        // Enable action buttons
        addButton.setVisibility(View.VISIBLE);

        //update bread crumbs
        breadCrumbsTitleLarge.setText(getString(R.string.spm_list_title));
        // Register a context menu to be shown for the given view
        registerForContextMenu(listView);

        // Get the database provider
        dbProvider = new DatabaseProvider(this);

        // Get a cursor with all server profiles
        Cursor cursor = dbProvider.fetchAllServerProfiles();
        // allow the activity to take care of managing the given Cursor's lifecycle
        startManagingCursor(cursor);

        // Show ServerProfiles list
        String[] from = new String[] { ServerProfiles.KEY_ALIAS, ServerProfiles.KEY_SERVER_URL };
        int[] to = new int[] {R.id.server_profiles_list_item_label, R.id.server_profiles_list_item_uri};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.server_profile_list_item, cursor, from, to);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        // return result with specified server profile id to home activity
        returnSelectedServerProfileId(id);
    }

    /* Options Menu */

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Add the menu options
//        menu.add(Menu.NONE, ID_OM_ADD_SERVER_PROFILE, Menu.NONE, R.string.spm_om_add_profile)
//                .setIcon(R.drawable.ic_menu_add);
//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//            case ID_OM_ADD_SERVER_PROFILE:
//                // Launch activity to add a new server profile
//                Intent intent = new Intent();
//                intent.setClass(this, ServerProfileActivity.class);
//                intent.setAction(ServerProfileActivity.ADD_SERVER_PROFILE_ACTION);
//                startActivityForResult(intent, ID_OM_ADD_SERVER_PROFILE);
//                return true;
//            default:
//                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
//                return super.onOptionsItemSelected(item);
//        }
//    }

    /* Context Menu */

    @Override
    public void onCreateContextMenu(ContextMenu menu,View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        // Determine on which item in the ListView the user long-clicked
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Cursor cursor = ((SimpleCursorAdapter) getListView().getAdapter()).getCursor();
        startManagingCursor(cursor);
        cursor.moveToPosition(info.position);

        // Retrieve the label for that particular item and use it as title for the menu
        int aliasId = cursor.getColumnIndex(ServerProfiles.KEY_ALIAS);
        menu.setHeaderTitle(cursor.getString(aliasId));

        // Add all the menu options
        menu.add(Menu.NONE, ID_CM_SWITCH, Menu.NONE, R.string.spm_cm_switch);
        menu.add(Menu.NONE, ID_CM_EDIT, Menu.NONE, R.string.spm_cm_edit);
        menu.add(Menu.NONE, ID_CM_DELETE, Menu.NONE, R.string.spm_cm_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Determine on which item in the ListView the user long-clicked and get it from Cursor
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // Handle item selection
        switch (item.getItemId()) {
            case ID_CM_SWITCH:
                // return result with specified server profile id to home activity
                returnSelectedServerProfileId(info.id);
                return true;
            case ID_CM_EDIT:
                // Launch activity to edit the server profile
                Intent editIntent = new Intent();
                editIntent.setClass(this, ServerProfileActivity.class);
                editIntent.setAction(ServerProfileActivity.EDIT_SERVER_PROFILE_ACTION);
                editIntent.putExtra(ServerProfileActivity.EXTRA_SERVER_PROFILE_ID, info.id);
                startActivityForResult(editIntent, ID_CM_EDIT);
                return true;
            case ID_CM_DELETE:
                selectedItemId = info.id; // workaround for API-7 that does not support args for showDialog()
                showDialog(ID_CM_DELETE);
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // the feedback about an operation in a small popup
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ID_CM_EDIT:
                    Toast.makeText(getApplicationContext(), R.string.spm_profile_updated_toast, Toast.LENGTH_SHORT).show();
                    break;
                case ID_OM_ADD_SERVER_PROFILE:
                    Toast.makeText(getApplicationContext(), R.string.spm_profile_created_toast, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {
            case ID_CM_DELETE:
                // Define the delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.spm_ad_delete_profile_msg)
                        // the delete button handler
                        .setPositiveButton(R.string.spm_delete_btn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dbProvider.deleteServerProfile(selectedItemId);
                                ((SimpleCursorAdapter)getListAdapter()).getCursor().requery();
                                // the feedback about an operation in a small popup
                                Toast.makeText(getApplicationContext(), R.string.spm_profile_deleted_toast, Toast.LENGTH_SHORT).show();
                            }
                        })
                        // the cancel button handler
                        .setNegativeButton(R.string.spm_cancel_btn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                dialog = builder.create();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    public void actionButtonOnClickListener(View view) {
        switch (view.getId()) {
            case R.id.app_icon_button:
                HomeActivity.goHome(this);
                break;
            case R.id.action_add_button:
                Intent intent = new Intent();
                intent.setClass(this, ServerProfileActivity.class);
                intent.setAction(ServerProfileActivity.ADD_SERVER_PROFILE_ACTION);
                startActivityForResult(intent, ID_OM_ADD_SERVER_PROFILE);
                break;
        }
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

    private void returnSelectedServerProfileId(long id) {
        // return result with specified server profile id to home activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(HomeActivity.EXTRA_SERVER_PROFILE_ID, id);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

}

