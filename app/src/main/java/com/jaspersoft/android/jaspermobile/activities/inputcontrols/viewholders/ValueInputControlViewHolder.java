package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.text.Editable;
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

    protected TextView label;
    protected EditText singleValue;
    protected TextView errorText;

    private ValueChangeListener mValueChangeListener;

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
            public void afterTextChanged(Editable editable) {
                if (mValueChangeListener != null) {
                    mValueChangeListener.onValueChanged(getAdapterPosition(), editable.toString());
                }
            }
        });
    }

    @Override
    public void populateView(InputControl inputControl) {
        String previousValue = singleValue.getText().toString();
        String currentValue = inputControl.getState().getValue();
        /**
         * Prevents focus reset on the start of EditText
         */
        if (!previousValue.equals(currentValue)) {
            singleValue.setText(currentValue);
        }
        label.setText(getUpdatedLabelText(inputControl));

        showError(errorText, inputControl);
    }

    public void setValueChangeListener(ValueChangeListener valueChangeListener) {
        this.mValueChangeListener = valueChangeListener;
    }

    public interface ValueChangeListener {
        void onValueChanged(int position, String value);
    }
}
