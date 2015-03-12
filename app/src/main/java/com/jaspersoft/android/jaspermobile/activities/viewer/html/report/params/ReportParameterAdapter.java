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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params;

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.undo.ReportParametersUndoManager;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import org.androidannotations.annotations.EBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EBean
public class ReportParameterAdapter {

    private static final String UNDO_MANAGER_KEY = "UNDO_MANAGER";

    private ReportParametersUndoManager undoManager = new ReportParametersUndoManager();

    private final Gson gson = new Gson();
    private final Type mapType = new TypeToken<Map<String,Set<String>>>() {}.getType();

    public void add(ArrayList<ReportParameter> reportParameters) {
        undoManager.add(toJson(reportParameters));
    }

    public String removeParams() {
        return undoManager.undo();
    }

    public String getParams() {
        return undoManager.peekLatest();
    }

    public void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(UNDO_MANAGER_KEY)) {
            undoManager = savedInstanceState.getParcelable(UNDO_MANAGER_KEY);
        }
    }

    public void save(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(UNDO_MANAGER_KEY, undoManager);
    }

    private String toJson(ArrayList<ReportParameter> reportParameters) {
        Map<String, Set<String>> params = new HashMap<String, Set<String>>();
        for (ReportParameter parameter : reportParameters) {
            params.put(parameter.getName(), parameter.getValues());
        }
        return gson.toJson(params, mapType);
    }

}
