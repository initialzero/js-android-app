package com.jaspersoft.android.jaspermobile.activities.favorites.adapter;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.manuelpeinado.multichoiceadapter.MultiChoiceAdapter;
import com.manuelpeinado.multichoiceadapter.MultiChoiceAdapterHelper;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class SingleChoiceAdapterHelper extends MultiChoiceAdapterHelper {

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

    @Override
    public void finishActionMode() {
        super.finishActionMode();
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

}
