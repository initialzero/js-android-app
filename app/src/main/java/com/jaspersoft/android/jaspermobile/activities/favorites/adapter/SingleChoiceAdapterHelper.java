package com.jaspersoft.android.jaspermobile.activities.favorites.adapter;

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
    protected SingleChoiceAdapterHelper(BaseAdapter owner) {
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
}
