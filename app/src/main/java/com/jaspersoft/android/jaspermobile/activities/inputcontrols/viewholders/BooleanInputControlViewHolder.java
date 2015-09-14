package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class BooleanInputControlViewHolder extends BaseInputControlViewHolder {

    private final static boolean DEFAULT_STATE = false;
    private CheckBox icBoolean;
    private StateChangeListener mStateChangeListener;

    public BooleanInputControlViewHolder(View itemView) {
        super(itemView);
        icBoolean = (CheckBox) itemView.findViewById(R.id.ic_boolean);
        icBoolean.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mStateChangeListener != null) {
                    mStateChangeListener.onStateChanged(getAdapterPosition(), isChecked);
                }
            }
        });
    }

    public void setStateChangeListener(StateChangeListener stateChangeListener) {
        this.mStateChangeListener = stateChangeListener;
    }

    @Override
    public void populateView(InputControl inputControl, boolean enabled) {
        icBoolean.setEnabled(enabled && !inputControl.isReadOnly());
        icBoolean.setText(inputControl.getLabel());
        if (inputControl.getState().getValue() != null) {
            icBoolean.setChecked(Boolean.parseBoolean(inputControl.getState().getValue()));
        } else {
            icBoolean.setChecked(DEFAULT_STATE);
        }
    }

    public interface StateChangeListener{
        void onStateChanged(int position, boolean state);
    }

}
