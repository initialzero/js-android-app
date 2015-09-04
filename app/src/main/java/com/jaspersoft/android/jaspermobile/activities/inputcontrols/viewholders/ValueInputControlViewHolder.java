package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class ValueInputControlViewHolder extends BaseInputControlViewHolder {

    private TextView label;
    private EditText singleValue;
    private TextView errorText;

    public ValueInputControlViewHolder(View itemView) {
        super(itemView);
        singleValue = (EditText) itemView.findViewById(R.id.ic_edit_text);
        errorText = (TextView) itemView.findViewById(R.id.ic_error_text);
        label = (TextView) itemView.findViewById(R.id.ic_text_label);

        singleValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void populateView(InputControl inputControl, boolean enabled) {
        singleValue.setEnabled(enabled && !inputControl.isReadOnly());
        singleValue.setText(inputControl.getState().getValue());
        label.setText(getUpdatedLabelText(inputControl));

        String error = (inputControl.getState().getError());
        errorText.setText(error);
        errorText.setVisibility(error == null ? View.GONE : View.VISIBLE);

        // allow only numbers if data type is numeric
        if (inputControl.getType() == InputControl.Type.singleValueNumber) {
            singleValue.setInputType(InputType.TYPE_CLASS_NUMBER
                    | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
    }
}
