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

package com.jaspersoft.android.jaspermobile.data.repository.report.page;

import android.content.Context;
import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.sdk.service.data.report.PageRange;
import com.jaspersoft.android.sdk.service.data.report.ReportExportOutput;
import com.jaspersoft.android.sdk.service.report.ReportExecution;
import com.jaspersoft.android.sdk.service.report.ReportExport;
import com.jaspersoft.android.sdk.service.report.ReportExportOptions;
import com.jaspersoft.android.sdk.service.report.ReportFormat;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tom Koptel
 * @since 2.5
 */
final class HtmlPageCreator extends PageCreator {
    private final Context context;
    private final JasperServer server;
    private final PageRequest pageRequest;
    private final ReportExecution execution;

    HtmlPageCreator(Context context, PageRequest pageRequest, ReportExecution execution, JasperServer server) {
        this.context = context;
        this.pageRequest = pageRequest;
        this.execution = execution;
        this.server = server;
    }

    @Override
    @NonNull
    public ReportPage create() throws Exception {
        String range = pageRequest.getRange();
        PageRange pageRange = PageRange.parse(range);

        ReportExportOptions options = ReportExportOptions.builder()
                .withFormat(ReportFormat.valueOf(pageRequest.getFormat()))
                .withPageRange(pageRange)
                .build();

        ReportExport export = execution.export(options);
        ReportExportOutput output = export.download();

        InputStream reportExport = output.getStream();
        InputStream customScript = context.getAssets().open("rest-report.js");

        try {
            Reader reader = new InputStreamReader(customScript);
            Template template = Mustache.compiler().compile(reader);

            int pageIndex = Integer.valueOf(range) - 1;
            Map<String, Object> data = new HashMap<>();
            data.put("jasperPrintName", execution.getExecutionId());
            data.put("pageIndex", pageIndex);
            data.put("isPro", server.isProEdition());
            String customJs = template.execute(data);

            DataNode indexHeadScript = DataNode.createFromEncoded("<script>" + customJs + "</script>", "");

            Document document = Jsoup.parse(reportExport, Charset.defaultCharset().name(), "");
            Elements head = document.getElementsByTag("head");
            head.append(indexHeadScript.toString());

            byte[] exportContent = document.toString().getBytes(Charset.forName("UTF-8"));
            return new ReportPage(exportContent, output.isFinal());
        } finally {
            IOUtils.closeQuietly(reportExport);
            IOUtils.closeQuietly(customScript);
        }
    }
}
