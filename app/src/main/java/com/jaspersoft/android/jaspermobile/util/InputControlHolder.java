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

package com.jaspersoft.android.jaspermobile.util;

import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class InputControlHolder{
    private List<ReportParameter> mReportParams;
    private List<InputControl> mInputControls;
    private List<ReportOptionHolder> mReportOptions;

    public InputControlHolder() {
        this.mReportOptions = new ArrayList<>();
        this.mReportParams = new ArrayList<>();
    }

    public List<InputControl> getInputControls() {
        return mInputControls;
    }

    public void setInputControls(List<InputControl> inputControls) {
        this.mInputControls = inputControls;
    }

    public List<ReportOptionHolder> getReportOptions() {
        return mReportOptions;
    }

    public void setReportOptions(List<ReportOptionHolder> reportOptions) {
        this.mReportOptions = reportOptions;
    }

    public List<ReportParameter> getReportParams() {
        return mReportParams;
    }

    public void setReportParams(List<ReportParameter> reportParams) {
        this.mReportParams = reportParams;
    }
}
