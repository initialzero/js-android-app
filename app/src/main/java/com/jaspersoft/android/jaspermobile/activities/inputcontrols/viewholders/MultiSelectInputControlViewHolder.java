package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.text.TextUtils;
import android.view.View;

import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class MultiSelectInputControlViewHolder extends ValueInputControlViewHolder {

    private final static int ITEM_TO_SHOW_MAX_COUNT = 30;

    public MultiSelectInputControlViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected String getCurrentValue(InputControl inputControl) {
        List<String> selectionList = new ArrayList<>();
        for (InputControlOption option : inputControl.getState().getOptions()) {
            if (option.isSelected()) {
                selectionList.add(option.getLabel());
                if (selectionList.size() > ITEM_TO_SHOW_MAX_COUNT) break;
            }
        }
        return selectionList.isEmpty() ? InputControlWrapper.NOTHING_SUBSTITUTE_LABEL : TextUtils.join(", ", selectionList);
    }
}
