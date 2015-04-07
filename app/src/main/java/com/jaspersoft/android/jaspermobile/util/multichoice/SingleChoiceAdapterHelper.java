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

package com.jaspersoft.android.jaspermobile.util.multichoice;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.navigation.NavigationPanelController;
import com.manuelpeinado.multichoiceadapter.MultiChoiceAdapter;
import com.manuelpeinado.multichoiceadapter.extras.actionbarcompat.MultiChoiceAdapterHelper;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class SingleChoiceAdapterHelper extends MultiChoiceAdapterHelper {

    public static final int NO_POSITION = -1;
    private static String CURRENT_POSITION_KEY = "CURRENT_POSITION";
    private int currentPosition;

    public SingleChoiceAdapterHelper(BaseAdapter owner) {
        super(owner);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        MultiChoiceAdapter adapter = (MultiChoiceAdapter) owner;
        if (!adapter.isItemCheckable(position)) {
            return false;
        } else {
            for (Long item : getCheckedItems()) {
                uncheckItem(item);
            }
            int correctedPosition = correctPositionAccountingForHeader(adapterView, position);
            long handle = positionToSelectionHandle(correctedPosition);
            boolean wasChecked = isChecked(handle);
            currentPosition = position;
            setItemChecked(handle, !wasChecked);
        }
        return true;
    }

    private int correctPositionAccountingForHeader(AdapterView<?> adapterView, int position) {
        ListView listView = (adapterView instanceof ListView) ? (ListView) adapterView : null;
        int headersCount = listView == null ? 0 : listView.getHeaderViewsCount();
        if (headersCount > 0) {
            position -= listView.getHeaderViewsCount();
        }
        return position;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void resetCurrentPosition() {
        currentPosition = NO_POSITION;
    }

    @Override
    public void restoreSelectionFromSavedInstanceState(Bundle savedInstanceState) {
        super.restoreSelectionFromSavedInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(CURRENT_POSITION_KEY);
        }
    }

    @Override
    public void save(Bundle outState) {
        super.save(outState);
        if (outState != null) {
            outState.putInt(CURRENT_POSITION_KEY, currentPosition);
        }
    }

    @Override
    public String getActionModeTitle(int count) {
        return getContext().getString(R.string.action_mode_title);
    }

    @Override
    protected void startActionMode() {
        super.startActionMode();
        ActionBarActivity activity = (ActionBarActivity) adapterView.getContext();
        ((NavigationPanelController) activity).onActionModeEnabled(true);
    }

    @Override
    public void finishActionMode() {
        super.finishActionMode();
    }

    @Override
    public void onDestroyActionMode() {
        super.onDestroyActionMode();
        ActionBarActivity activity = (ActionBarActivity) adapterView.getContext();
        ((NavigationPanelController) activity).onActionModeEnabled(false);
    }
}
