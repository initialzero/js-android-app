package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.view.View;

import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class SingleSelectInputControlViewHolder extends ValueInputControlViewHolder {
    public SingleSelectInputControlViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected String getCurrentValue(InputControl inputControl) {
        for (InputControlOption option : inputControl.getState().getOptions()) {
            if (option.isSelected()) {
                return option.getLabel();
            }
        }
        return InputControlWrapper.NOTHING_SUBSTITUTE_LABEL;
    }
}
