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

package com.jaspersoft.android.jaspermobile.activities.favorites.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.SimpleCursorAdapter;

import com.manuelpeinado.multichoiceadapter.ItemClickInActionModePolicy;
import com.manuelpeinado.multichoiceadapter.MultiChoiceAdapter;

import java.util.Set;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public abstract class SingleChoiceSimpleCursorAdapter extends SimpleCursorAdapter implements ActionMode.Callback,
        MultiChoiceAdapter {
    private SingleChoiceAdapterHelper helper = new SingleChoiceAdapterHelper(this) {
        @Override
        protected long positionToSelectionHandle(int position) {
            return getItemId(position);
        }
    };

    public SingleChoiceSimpleCursorAdapter(Bundle savedInstanceState, Context context, int layout, Cursor cursor,
                                           String[] from, int[] to, int flags) {
        super(context, layout, cursor, from, to, flags);
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public void setAdapterView(AdapterView<? super BaseAdapter> adapterView) {
        helper.setAdapterView(adapterView);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        helper.setOnItemClickListener(listener);
    }

    public void save(Bundle outState) {
        helper.save(outState);
    }

    public void setItemChecked(long itemId, boolean checked) {
        helper.setItemChecked(itemId, checked);
    }

    public Set<Long> getCheckedItems() {
        return helper.getCheckedItems();
    }

    public int getCheckedItemCount() {
        return helper.getCheckedItemCount();
    }

    public boolean isChecked(long itemId) {
        return helper.isChecked(itemId);
    }

    public void setItemChecked(int position, boolean checked) {
        helper.setItemChecked(position, checked);
    }

    public void setItemClickInActionModePolicy(ItemClickInActionModePolicy policy) {
        helper.setItemClickInActionModePolicy(policy);
    }

    public ItemClickInActionModePolicy getItemClickInActionModePolicy() {
        return helper.getItemClickInActionModePolicy();
    }

    public void finishActionMode() {
        helper.finishActionMode();
    }

    protected Context getContext() {
        return helper.getContext();
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        helper.onDestroyActionMode();
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        View viewWithoutSelection = getViewImpl(position, convertView, parent);
        return helper.getView(position, viewWithoutSelection);
    }

    @Override
    public boolean isItemCheckable(int position) {
        return true;
    }

    @Override
    public String getActionModeTitle(int count) {
        return helper.getActionModeTitle(count);
    }

    /**
     * Override this method if you need to customize the model-to-view mapping performed by SimpleCursorAdapter (for
     * instance, to populate an image view based on a URL stored in a DB column)
     */
    protected View getViewImpl(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    public int getCurrentPosition() {
        return helper.getCurrentPosition();
    }

}
