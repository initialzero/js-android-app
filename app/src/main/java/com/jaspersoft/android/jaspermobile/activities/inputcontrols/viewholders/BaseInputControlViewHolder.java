package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public abstract class BaseInputControlViewHolder extends RecyclerView.ViewHolder {
    public BaseInputControlViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void populateView(InputControl inputControl, boolean enabled);

    protected String getUpdatedLabelText(InputControl inputControl) {
        String mandatoryPrefix = (inputControl.isMandatory()) ? "* " : "";
        return mandatoryPrefix + inputControl.getLabel() + ":";
    }
}
