/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util;

import android.support.annotation.NonNull;

import com.google.inject.Inject;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Tivodar
 * @author Tom Koptel
 * @since 2.0
 */
public class ReportParamsStorage {
    private final Map<String, WeakReference<ArrayList<ReportParameter>>> paramsCache = new HashMap<String, WeakReference<ArrayList<ReportParameter>>>();
    private final Map<String, WeakReference<ArrayList<InputControl>>> inputControlsCache = new HashMap<String, WeakReference<ArrayList<InputControl>>>();

    @Inject
    public ReportParamsStorage() {
    }

    @NonNull
    public ArrayList<ReportParameter> getReportParameters(@NonNull String resourceUri) {
        WeakReference<ArrayList<ReportParameter>> weakReference = paramsCache.get(resourceUri);
        if (weakReference == null) {
            return new ArrayList<ReportParameter>();
        } else {
            ArrayList<ReportParameter> params = weakReference.get();
            if (params == null) {
                return new ArrayList<ReportParameter>();
            } else {
                return params;
            }
        }
    }

    @NonNull
    public ArrayList<InputControl> getInputControls(@NonNull String resourceUri) {
        WeakReference<ArrayList<InputControl>> weakReference = inputControlsCache.get(resourceUri);
        if (weakReference == null) {
            return new ArrayList<InputControl>();
        } else {
            ArrayList<InputControl> controls = weakReference.get();
            if (controls == null) {
                return new ArrayList<InputControl>();
            } else {
                return controls;
            }
        }
    }

    public void putReportParameters(@NonNull String resourceUri, @NonNull ArrayList<ReportParameter> reportParameters) {
        WeakReference<ArrayList<ReportParameter>> weakReference = paramsCache.get(resourceUri);
        if (weakReference == null) {
            createReportParametersReference(resourceUri, reportParameters);
        } else {
            if (weakReference.get() == null) {
                createReportParametersReference(resourceUri, reportParameters);
            } else {
                weakReference.get().clear();
                weakReference.get().addAll(reportParameters);
            }
        }
    }

    public void putInputControls(@NonNull String resourceUri, @NonNull ArrayList<InputControl> inputControls) {
        WeakReference<ArrayList<InputControl>> weakReference = inputControlsCache.get(resourceUri);
        if (weakReference == null) {
            createInputControlsWeakReference(resourceUri, inputControls);
        } else {
            if (weakReference.get() == null) {
                createInputControlsWeakReference(resourceUri, inputControls);
            } else {
                weakReference.get().clear();
                weakReference.get().addAll(inputControls);
            }
        }
    }

    private void createReportParametersReference(String resourceUri, ArrayList<ReportParameter> reportParameters) {
        WeakReference<ArrayList<ReportParameter>> weakReference;
        weakReference = new WeakReference<ArrayList<ReportParameter>>(reportParameters);
        paramsCache.put(resourceUri, weakReference);
    }

    private void createInputControlsWeakReference(String resourceUri, ArrayList<InputControl> inputControls) {
        WeakReference<ArrayList<InputControl>> weakReference;
        weakReference = new WeakReference<ArrayList<InputControl>>(inputControls);
        inputControlsCache.put(resourceUri, weakReference);
    }
}
