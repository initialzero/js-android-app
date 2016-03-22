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
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public abstract class ValueInputControlViewHolder extends BaseInputControlViewHolder {

    private View item;
    private TextView label;
    private TextView value;
    private TextView errorText;

    private ClickListener mClickListener;

    public ValueInputControlViewHolder(View itemView) {
        super(itemView);
        value = (TextView) itemView.findViewById(R.id.ic_value);
        errorText = (TextView) itemView.findViewById(R.id.ic_error_text);
        label = (TextView) itemView.findViewById(R.id.ic_text_label);
        item = itemView;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onClick(getAdapterPosition());
                }
            }
        });
    }

    @Override
    public void populateView(InputControl inputControl) {
        value.setEnabled(!inputControl.isReadOnly());
        item.setEnabled(!inputControl.isReadOnly());

        value.setText(getCurrentValue(inputControl));
        label.setText(getUpdatedLabelText(inputControl));

        showError(errorText, inputControl);
    }

    public void setOnSelectListener(ClickListener onSelectListener) {
        this.mClickListener = onSelectListener;
    }

    protected abstract String getCurrentValue(InputControl inputControl);

    public interface ClickListener {
        void onClick(int position);
    }
}
