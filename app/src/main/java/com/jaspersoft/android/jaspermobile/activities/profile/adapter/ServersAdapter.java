package com.jaspersoft.android.jaspermobile.activities.profile.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.adapter.SingleChoiceSimpleCursorAdapter;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ServersAdapter extends SingleChoiceSimpleCursorAdapter {
    private static final String[] FROM = {ServerProfilesTable.ALIAS, ServerProfilesTable.SERVER_URL, ServerProfilesTable._ID};
    private static final int[] TO = {android.R.id.text1, android.R.id.text2, android.R.id.icon};

    private ServersInteractionListener serversInteractionListener;

    public ServersAdapter(Context context, Bundle savedInstanceState, int layout) {
        super(savedInstanceState, context, layout, null, FROM, TO, 0);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.am_servers_menu, menu);
        return true;
    }

    public void setServersInteractionListener(ServersInteractionListener serversInteractionListener) {
        this.serversInteractionListener = serversInteractionListener;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connectItem:
                if (serversInteractionListener != null) {
                    serversInteractionListener.onConnect(getCurrentPosition());
                }
                break;
            case R.id.editItem:
                if (serversInteractionListener != null) {
                    serversInteractionListener.onEdit(getCurrentPosition());
                }
                break;
            case R.id.deleteItem:
                if (serversInteractionListener != null) {
                    serversInteractionListener.onDelete(getCurrentPosition());
                }
                break;
            default:
                return false;
        }
        return false;
    }

    public static interface ServersInteractionListener {
        void onConnect(int position);

        void onEdit(int position);

        void onDelete(int position);
    }
}
