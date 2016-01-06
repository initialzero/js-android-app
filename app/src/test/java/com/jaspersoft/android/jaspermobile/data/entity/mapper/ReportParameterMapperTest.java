package com.jaspersoft.android.jaspermobile.data.entity.mapper;


import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

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

        com.jaspersoft.android.sdk.network.entity.report.ReportParameter result = mReportParamsMapper.transform(parameter);
        assertThat("Should map report parameter name", result.getName(), is("name"));
        assertThat("Should map report parameter values", result.getValue(), hasItem("value"));
    }
}