package com.jaspersoft.android.jaspermobile.activities.repository.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.jaspersoft.android.jaspermobile.activities.favorites.adapter.SingleChoiceAdapterHelper;
import com.manuelpeinado.multichoiceadapter.ItemClickInActionModePolicy;
import com.manuelpeinado.multichoiceadapter.MultiChoiceAdapter;

import java.util.List;
import java.util.Set;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public abstract class SingleChoiceArrayAdapter<T> extends ArrayAdapter<T> implements ActionMode.Callback,
        MultiChoiceAdapter {
    private SingleChoiceAdapterHelper helper = new SingleChoiceAdapterHelper(this);

    public SingleChoiceArrayAdapter(Bundle savedInstanceState, Context context, int resource, int textViewResourceId,
                                   List<T> objects) {
        super(context, resource, textViewResourceId, objects);
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public SingleChoiceArrayAdapter(Bundle savedInstanceState, Context context, int resource, int textViewResourceId,
                                   T[] objects) {
        super(context, resource, textViewResourceId, objects);
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public SingleChoiceArrayAdapter(Bundle savedInstanceState, Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public SingleChoiceArrayAdapter(Bundle savedInstanceState, Context context, int textViewResourceId, List<T> objects) {
        super(context, textViewResourceId, objects);
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public SingleChoiceArrayAdapter(Bundle savedInstanceState, Context context, int textViewResourceId, T[] objects) {
        super(context, textViewResourceId, objects);
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public SingleChoiceArrayAdapter(Bundle savedInstanceState, Context context, int textViewResourceId) {
        super(context, textViewResourceId);
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

    public void setItemChecked(long position, boolean checked) {
        helper.setItemChecked(position, checked);
    }

    public Set<Long> getCheckedItems() {
        return helper.getCheckedItems();
    }

    public int getCheckedItemCount() {
        return helper.getCheckedItemCount();
    }

    public boolean isChecked(long position) {
        return helper.isChecked(position);
    }

    public void setItemClickInActionModePolicy(ItemClickInActionModePolicy policy) {
        helper.setItemClickInActionModePolicy(policy);
    }

    public ItemClickInActionModePolicy getItemClickInActionModePolicy() {
        return helper.getItemClickInActionModePolicy();
    }

    protected void finishActionMode() {
        helper.finishActionMode();
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

    protected View getViewImpl(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    @Override
    public boolean isItemCheckable(int position) {
        return true;
    }

    @Override
    public String getActionModeTitle(int count) {
        return helper.getActionModeTitle(count);
    }
}