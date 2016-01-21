package com.jaspersoft.android.jaspermobile.data.entity.mapper;


import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ReportParameterMapperTest {
    private ReportParamsMapper mReportParamsMapper;

    @Before
    public void setUp() throws Exception {
        mReportParamsMapper = new ReportParamsMapper();
    }

    @Test
    public void testTransform() throws Exception {
        ReportParameter parameter =
                new ReportParameter("name", Collections.singleton("value"));

        com.jaspersoft.android.sdk.network.entity.report.ReportParameter result = mReportParamsMapper.toRetrofittedParam(parameter);
        assertThat("Should map report parameter name", result.getName(), is("name"));
        assertThat("Should map report parameter values", result.getValue(), hasItem("value"));
    }

    @Test
    public void testToMapLegacy() throws Exception {
        ReportParameter parameter1 =
                new ReportParameter("name", Collections.singleton("value"));
        Map<String, Set<String>> result = mReportParamsMapper.toMapLegacy(Collections.singletonList(parameter1));

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

        String result = mReportParamsMapper.toJsonLegacyParams(Collections.singletonList(parameter1));
        assertThat(result, is("{\"name\":[\"value\"]}"));
    }
}