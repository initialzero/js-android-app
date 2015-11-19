package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;

import java.util.ArrayList;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public abstract class SelectionModeHelper<T> implements ActionMode.Callback, ResourceSelector {

    private ArrayList<Integer> mSelectedItems;

    private ActionBarActivity mActivity;
    private SelectableAdapter<T> mSelectableAdapter;
    private ActionMode mActionMode;

    public SelectionModeHelper(ActionBarActivity activity, SelectableAdapter<T> selectableAdapter) {
        this.mActivity = activity;
        this.mSelectableAdapter = selectableAdapter;
        mSelectedItems = new ArrayList<>();

        setSelectorChangeListener();
    }

    @Override
    public boolean isSelected(int position) {
        return mSelectedItems.contains(position);
    }

    @Override
    public void changeSelectedState(int position) {
        boolean newState = !isSelected(position);

        if (newState) {
            mSelectedItems.add(position);
        } else {
            if (isSelected(position)) {
                mSelectedItems.remove(Integer.valueOf(position));
            }
        }

        handleSelectionState();

        if (mSelectableAdapter != null) {
            mSelectableAdapter.notifyItemChanged(position);
        }
    }

    public void invalidateSelectionMode(){
        if (mActionMode != null) {
            mActionMode.invalidate();
        }
    }

    public void finishSelectionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    public int getSelectedItemCount(){
        return mSelectedItems.size();
    }

    public ArrayList<T> getSelectedItemsKey() {
        ArrayList<T> selectedItems = new ArrayList<>();

        for (Integer selectedItemPos : mSelectedItems) {
            T selectedItem = mSelectableAdapter.getItemKey(selectedItemPos);
            selectedItems.add(selectedItem);
        }

        return selectedItems;
    }

    private void startActionMode() {
        if (mActionMode == null) {
            mActionMode = mActivity.startSupportActionMode(this);
        } else {
            mActionMode.invalidate();
        }
    }

    private void setSelectionTitle(int count) {
        if (mActionMode != null) {
            mActionMode.setTitle("" + count);
        }
    }

    private void handleSelectionState() {
        int selectedItemCount = mSelectedItems.size();

        if (selectedItemCount > 0) {
            startActionMode();
            setSelectionTitle(selectedItemCount);
        } else {
            finishSelectionMode();
        }
    }

    private void clearSelection() {
        if (mSelectableAdapter != null) {
            for (Integer mSelectedItem : mSelectedItems) {
                mSelectableAdapter.notifyItemChanged(mSelectedItem);
            }
        }
        mSelectedItems = new ArrayList<>();
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mActionMode = null;
        clearSelection();
    }

    private void setSelectorChangeListener() {
        if (mSelectableAdapter != null) {
            mSelectableAdapter.setResourceSelector(this);
        }
    }


}
