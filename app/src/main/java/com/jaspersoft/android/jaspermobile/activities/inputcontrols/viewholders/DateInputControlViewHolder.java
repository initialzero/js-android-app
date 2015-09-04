package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class DateInputControlViewHolder extends BaseInputControlViewHolder {

    private TextView label;
    private TextView selectedDateTime;
    private TextView errorText;
    private ImageButton date;
    private ImageButton time;
    private ImageButton clear;
    private View clearDivider;
    private View dateTimeDivider;

    public DateInputControlViewHolder(View itemView) {
        super(itemView);

        selectedDateTime = (TextView) itemView.findViewById(R.id.ic_datetime_text);
        errorText = (TextView) itemView.findViewById(R.id.ic_error_text);
        label = (TextView) itemView.findViewById(R.id.ic_text_label);
        date = (ImageButton) itemView.findViewById(R.id.ic_date);
        time = (ImageButton) itemView.findViewById(R.id.ic_time);
        clearDivider = itemView.findViewById(R.id.ic_clear_divider);
        dateTimeDivider = itemView.findViewById(R.id.ic_datetime_divider);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void populateView(InputControl inputControl, boolean enabled) {
        enableViews(inputControl, enabled);
        //selectedValue.setText(getCurrentSelection(inputControl));
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
        return "";
    }

    private void enableViews(InputControl inputControl, boolean enabled){
        selectedDateTime.setEnabled(enabled && !inputControl.isReadOnly());
        date.setEnabled(enabled && !inputControl.isReadOnly());
        time.setEnabled(enabled && !inputControl.isReadOnly());
        clear.setEnabled(enabled && !inputControl.isReadOnly());
    }
}
