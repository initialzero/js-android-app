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

import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.springframework.http.client.ClientHttpResponse;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class StreamReportResourceProviderTest {

    @Mock
    JsRestClient jsRestClient;
    @Mock
    Collection<ReportParameter> reportParameters;
    @Mock
    ResourceLookup resourceLookup;
    @Mock
    ReportExecutionResponse reportExecutionResponse;
    @Mock
    ExportExecution exportExecution;

    @Mock
    ResourcePrintJob resourcePrintJob;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateProviderWithNULLParams() {
        StreamReportResourceProvider
                .builder(RuntimeEnvironment.application)
                .setJsRestClient(jsRestClient)
                .setResource(resourceLookup)
                .addReportParameters(null)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotCreateProviderWithoutResourceLookup() {
        StreamReportResourceProvider
                .builder(RuntimeEnvironment.application)
                .setJsRestClient(jsRestClient)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotCreateProviderWithoutJsRestClient() {
        StreamReportResourceProvider
                .builder(RuntimeEnvironment.application)
                .setResource(resourceLookup)
                .build();
    }

    @Test
    public void shouldProvideResource() {
        when(reportExecutionResponse.getExports()).thenReturn(Arrays.asList(new ExportExecution[] {exportExecution}));
        when(jsRestClient.runReportExecution(any(ReportExecutionRequest.class))).thenReturn(reportExecutionResponse);

        ResourceProvider<ClientHttpResponse> resourceProvider = StreamReportResourceProvider
                .builder(RuntimeEnvironment.application)
                .setJsRestClient(jsRestClient)
                .setResource(resourceLookup)
                .build();

        ClientHttpResponse response = resourceProvider
                .provideResource();
        assertThat(response, is(notNullValue()));
    }
}
