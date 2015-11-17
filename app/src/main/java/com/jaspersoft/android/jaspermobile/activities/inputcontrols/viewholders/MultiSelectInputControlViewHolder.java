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

import android.text.TextUtils;
import android.view.View;

import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class MultiSelectInputControlViewHolder extends ValueInputControlViewHolder {

    private final static int ITEM_TO_SHOW_MAX_COUNT = 30;

    public MultiSelectInputControlViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected String getCurrentValue(InputControl inputControl) {
        List<String> selectionList = new ArrayList<>();
        for (InputControlOption option : inputControl.getState().getOptions()) {
            if (option.isSelected()) {
                selectionList.add(option.getLabel());
                if (selectionList.size() > ITEM_TO_SHOW_MAX_COUNT) break;
            }
        }
        return selectionList.isEmpty() ? InputControlWrapper.NOTHING_SUBSTITUTE_LABEL : TextUtils.join(", ", selectionList);
    }
}
