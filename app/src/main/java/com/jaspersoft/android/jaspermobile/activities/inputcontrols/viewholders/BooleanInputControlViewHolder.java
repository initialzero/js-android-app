/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class BooleanInputControlViewHolder extends BaseInputControlViewHolder {

    private final static boolean DEFAULT_STATE = false;
    private View itemView;
    private CheckBox icBoolean;
    private TextView icTitle;
    private StateChangeListener mStateChangeListener;

    public BooleanInputControlViewHolder(View itemView) {
        super(itemView);

        this.itemView = itemView;
        icBoolean = (CheckBox) itemView.findViewById(R.id.ic_boolean);
        icTitle = (TextView) itemView.findViewById(R.id.ic_boolean_title);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                icBoolean.performClick();
                if (mStateChangeListener != null) {
                    mStateChangeListener.onStateChanged(getAdapterPosition(), icBoolean.isChecked());
                }
            }
        });
    }

    public void setStateChangeListener(StateChangeListener stateChangeListener) {
        this.mStateChangeListener = stateChangeListener;
    }

    @Override
    public void populateView(InputControl inputControl) {
        icBoolean.setEnabled(!inputControl.isReadOnly());
        icTitle.setEnabled(!inputControl.isReadOnly());
        itemView.setEnabled(!inputControl.isReadOnly());

        icTitle.setText(getUpdatedLabelText(inputControl));
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
