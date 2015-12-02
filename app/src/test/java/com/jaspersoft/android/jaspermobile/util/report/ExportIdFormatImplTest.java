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

package com.jaspersoft.android.jaspermobile.util.report;

import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ExportsRequest;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(JUnitParamsRunner.class)
public class ExportIdFormatImplTest {

    @Test
    public void shouldAdaptExportIdFor5_5() {
        ExportsRequest exportRequest = new ExportsRequest();
        exportRequest.setPages("1-5");
        exportRequest.setOutputFormat("PDF");

        ExportExecution exportExecution = new ExportExecution();
        exportExecution.setId("PDF");

        ExportIdFormat adapter = ExportIdFormatFactory.builder()
                .setExportExecution(exportExecution)
                .setExportsRequest(exportRequest)
                .build()
                .createAdapter(ServerVersion.v5_5);
        assertThat(adapter.format(), is("PDF;pages=1-5"));
    }

    @Test
    @Parameters({"v5_6", "v5_6_1", "v6", "v6_0_1", "v6_1"})
    public void shouldAdaptExportIdByDefault(String serverVersion) throws Exception {
        ExportsRequest exportRequest = new ExportsRequest();
        exportRequest.setPages("1-5");
        exportRequest.setOutputFormat("PDF");

        ExportExecution exportExecution = new ExportExecution();
        exportExecution.setId("1234");

        ServerVersion version = (ServerVersion) ServerVersion.class.getField(serverVersion).get(null);
        ExportIdFormat adapter = ExportIdFormatFactory.builder()
                .setExportExecution(exportExecution)
                .setExportsRequest(exportRequest)
                .build()
                .createAdapter(version);
        assertThat(adapter.format(), is("1234"));
    }

}
