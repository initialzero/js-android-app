package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.view.View;

import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class TextInputControlViewHolder extends ValueInputControlViewHolder {
    public TextInputControlViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected String getCurrentValue(InputControl inputControl) {
        String icValue = inputControl.getState().getValue();
        if (!icValue.isEmpty()) return icValue;

        return InputControlWrapper.NOTHING_SUBSTITUTE_LABEL;

    }
}
