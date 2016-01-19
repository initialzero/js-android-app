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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public abstract class BaseInputControlViewHolder extends RecyclerView.ViewHolder {
    public BaseInputControlViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void populateView(InputControl inputControl);

    protected String getUpdatedLabelText(InputControl inputControl) {
        String mandatoryPrefix = (inputControl.isMandatory()) ? "* " : "";
        return mandatoryPrefix + inputControl.getLabel() + ":";
    }

    protected void showError(TextView errorView, InputControl inputControl) {
        String error = inputControl.getState().getError();
        errorView.setText(error);
        errorView.setVisibility(error != null ? View.VISIBLE : View.GONE);
    }
}
