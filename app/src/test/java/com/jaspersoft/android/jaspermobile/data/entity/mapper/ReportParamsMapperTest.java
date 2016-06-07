/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.data.entity.mapper;


import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ReportParamsMapperTest {
    private ReportParamsMapper mReportParamsMapper;

    @Before
    public void setUp() throws Exception {
        mReportParamsMapper = new ReportParamsMapper();
    }

    @Test
    public void testTransform() throws Exception {
        ReportParameter parameter =
                new ReportParameter("name", Collections.singleton("value"));

        com.jaspersoft.android.sdk.network.entity.report.ReportParameter result = mReportParamsMapper.legacyParamToRetrofitted(parameter);
        assertThat("Should map report parameter name", result.getName(), is("name"));
        assertThat("Should map report parameter values", result.getValue(), hasItem("value"));
    }

    @Test
    public void testToMapLegacy() throws Exception {
        ReportParameter parameter1 =
                new ReportParameter("name", Collections.singleton("value"));
        Map<String, Set<String>> result = mReportParamsMapper.legacyToMap(Collections.singletonList(parameter1));

        Set<String> actual = result.keySet();
        assertThat("Should map report parameter name", actual, hasItem("name"));

        Collection<Set<String>> valueSets = result.values();
        Set<String> hashSet = new HashSet<>();
        for (Set<String> valueSet : valueSets) {
            hashSet.addAll(valueSet);
        }
        assertThat("Should map report parameter values", hashSet, hasItem("value"));
    }

    @Test
    public void testToJsonLegacy() throws Exception {
        ReportParameter parameter1 =
                new ReportParameter("name", Collections.singleton("value"));

        String result = mReportParamsMapper.legacyParamsToJson(Collections.singletonList(parameter1));
        assertThat(result, is("{\"name\":[\"value\"]}"));
    }

    @Test
    public void should_map_to_legacy_params() throws Exception {
        Map<String, Set<String>> params = new HashMap<>();
        params.put("name", Collections.singleton("value"));

        List<ReportParameter> result = mReportParamsMapper.mapToLegacyParams(params);
        ReportParameter expected = result.get(0);

        assertThat(expected.getName(), is("name"));
        assertThat(expected.getValues(), hasItem("value"));
    }
}