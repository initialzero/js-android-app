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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class ReportParamsSerializerImpl implements ReportParamsSerializer {
    private static final String EMPTY_JSON = "{}";

    @Inject
    public ReportParamsSerializerImpl() {
    }

    @Override
    public String toJson(List<ReportParameter> reportParameters) {
        Map<String, Set<String>> params = new HashMap<String, Set<String>>();
        if (reportParameters == null) {
            return EMPTY_JSON;
        } else {
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String,Set<String>>>() {}.getType();
            for (ReportParameter parameter : reportParameters) {
                params.put(parameter.getName(), parameter.getValues());
            }
            return gson.toJson(params, mapType);
        }
    }
}
