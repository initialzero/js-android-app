package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.view.View;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class SelectInputControlViewHolder extends BaseInputControlViewHolder {

    private TextView label;
    private TextView selectedValue;
    private TextView errorText;

    public SelectInputControlViewHolder(View itemView) {
        super(itemView);
        selectedValue = (TextView) itemView.findViewById(R.id.ic_selected_value);
        errorText = (TextView) itemView.findViewById(R.id.ic_error_text);
        label = (TextView) itemView.findViewById(R.id.ic_text_label);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void populateView(InputControl inputControl, boolean enabled) {
        selectedValue.setEnabled(enabled && !inputControl.isReadOnly());
        selectedValue.setText(getCurrentSelection(inputControl));
        label.setText(getUpdatedLabelText(inputControl));

        String error = (inputControl.getState().getError());
        errorText.setText(error);
        errorText.setVisibility(error == null ? View.GONE : View.VISIBLE);
    }

    private String getCurrentSelection(InputControl inputControl){
        // set initial value for spinner
        for (InputControlOption option : inputControl.getState().getOptions()) {
            if (option.isSelected()) {
                return option.getLabel();
            }
        }
        return InputControlWrapper.NOTHING_SUBSTITUTE_LABEL;
    }
}
