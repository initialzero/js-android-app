package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.text.InputType;
import android.view.View;

import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class NumberValueInputControlViewHolder extends ValueInputControlViewHolder {

    public NumberValueInputControlViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void populateView(InputControl inputControl, boolean enabled) {
        super.populateView(inputControl, enabled);

        // allow only numbers if data type is numeric
        if (inputControl.getType() == InputControl.Type.singleValueNumber) {
            singleValue.setInputType(InputType.TYPE_CLASS_NUMBER
                    | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
    }
}
