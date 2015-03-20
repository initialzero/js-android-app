/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params.InputControlsSerializer;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params.InputControlSerializerImpl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class ReportModel implements Parcelable {
    private final InputControlsSerializer serializer = new InputControlSerializerImpl();
    private List<InputControl> inputControls;

    public static ReportModel copy(ReportModel original) {
        ReportModel copy = new ReportModel();
        copy.setInputControls(original.getInputControls());
        return copy;
    }

    public List<InputControl> getInputControls() {
        return inputControls;
    }

    public ArrayList<ReportParameter> getReportParameters() {
        ArrayList<ReportParameter> parameters = new ArrayList<ReportParameter>();
        for (InputControl inputControl : inputControls) {
            parameters.add(new ReportParameter(inputControl.getId(), inputControl.getSelectedValues()));
        }
        return parameters;
    }


    public String getJsonReportParameters() {
        return serializer.toJson(inputControls);
    }

    public void setInputControls(List<InputControl> inputControls) {
        this.inputControls = inputControls;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(inputControls);
    }

    public ReportModel() {
    }

    private ReportModel(Parcel in) {
        in.readTypedList(inputControls, InputControl.CREATOR);
    }

    public static final Parcelable.Creator<ReportModel> CREATOR = new Parcelable.Creator<ReportModel>() {
        public ReportModel createFromParcel(Parcel source) {
            return new ReportModel(source);
        }

        public ReportModel[] newArray(int size) {
            return new ReportModel[size];
        }
    };
}
