/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.util.print;

import com.jaspersoft.android.jaspermobile.util.server.ServerInfoProvider;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.report.ReportStatus;
import com.jaspersoft.android.sdk.client.oxm.report.ReportStatusResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ReportPrintUnitTest {

    @Mock
    JsRestClient jsRestClient;
    @Mock
    List<ReportParameter> reportParameters;
    @Mock
    ResourceLookup resourceLookup;
    @Mock
    ServerInfoProvider serverInfoProvider;

    @Mock
    ResourcePrintJob resourcePrintJob;

    // REST
    @Mock
    ReportExecutionResponse reportExecutionResponse;
    @Mock
    ExportExecution exportExecution;
    @Mock
    ReportStatusResponse reportStatusResponse;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateProviderWithNULLParams() {
        new ReportPrintUnit(jsRestClient, resourceLookup, null, serverInfoProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateProviderWithoutResourceLookup() {
        new ReportPrintUnit(jsRestClient, null, reportParameters, serverInfoProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateProviderWithoutJsRestClient() {
        new ReportPrintUnit(null, resourceLookup, reportParameters, serverInfoProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateProviderWithoutServerProviderData() {
        new ReportPrintUnit(jsRestClient, resourceLookup, reportParameters, null);
    }

    @Test
    public void shouldProvideResourceTotalPages() {
        when(reportExecutionResponse.getReportStatus()).thenReturn(ReportStatus.ready);

        when(reportExecutionResponse.getTotalPages()).thenReturn(100);
        when(reportExecutionResponse.getExports()).thenReturn(Arrays.asList(new ExportExecution[]{exportExecution}));

        when(jsRestClient.runReportExecution(any(ReportExecutionRequest.class))).thenReturn(reportExecutionResponse);

        PrintUnit reportPrintUnit = new ReportPrintUnit(jsRestClient, resourceLookup, reportParameters, serverInfoProvider);

        Integer pages = reportPrintUnit.fetchPageCount().toBlocking().first();
        assertThat(pages, is(100));
    }

    @Test
    public void shouldProvideResourceTotalPagesAfterDelay() {
        when(reportStatusResponse.getReportStatus()).thenReturn(ReportStatus.ready);
        when(reportExecutionResponse.getReportStatus()).thenReturn(ReportStatus.queued);

        when(reportExecutionResponse.getTotalPages()).thenReturn(100);
        when(reportExecutionResponse.getExports()).thenReturn(Arrays.asList(new ExportExecution[]{exportExecution}));

        when(jsRestClient.runReportExecution(any(ReportExecutionRequest.class))).thenReturn(reportExecutionResponse);
        when(jsRestClient.runReportStatusCheck(any(String.class))).thenReturn(reportStatusResponse);
        when(jsRestClient.runReportDetailsRequest(any(String.class))).thenReturn(reportExecutionResponse);

        PrintUnit reportPrintUnit = new ReportPrintUnit(jsRestClient, resourceLookup, reportParameters, serverInfoProvider);
        Integer pages = reportPrintUnit.fetchPageCount().toBlocking().first();
        assertThat(pages, is(100));
    }

    @Test(expected = RestClientException.class)
    public void shouldRaiseErrorIfExecutionFailed() {
        when(reportExecutionResponse.getReportStatus()).thenReturn(ReportStatus.failed);

        when(reportExecutionResponse.getTotalPages()).thenReturn(100);
        when(reportExecutionResponse.getExports()).thenReturn(Arrays.asList(new ExportExecution[]{exportExecution}));

        when(jsRestClient.runReportExecution(any(ReportExecutionRequest.class))).thenReturn(reportExecutionResponse);

        PrintUnit reportPrintUnit = new ReportPrintUnit(jsRestClient, resourceLookup, reportParameters, serverInfoProvider);
        reportPrintUnit.fetchPageCount().toBlocking().first();
    }

    @Test(expected = RestClientException.class)
    public void shouldRaiseErrorIfExecutionFailedAfterDelay() {
        when(reportStatusResponse.getReportStatus()).thenReturn(ReportStatus.failed);
        when(reportExecutionResponse.getReportStatus()).thenReturn(ReportStatus.queued);

        when(reportExecutionResponse.getTotalPages()).thenReturn(100);
        when(reportExecutionResponse.getExports()).thenReturn(Arrays.asList(new ExportExecution[]{exportExecution}));

        when(jsRestClient.runReportExecution(any(ReportExecutionRequest.class))).thenReturn(reportExecutionResponse);
        when(jsRestClient.runReportStatusCheck(any(String.class))).thenReturn(reportStatusResponse);

        PrintUnit reportPrintUnit = new ReportPrintUnit(jsRestClient, resourceLookup, reportParameters, serverInfoProvider);
        reportPrintUnit.fetchPageCount().toBlocking().first();
    }

}
