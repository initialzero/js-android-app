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

package com.jaspersoft.android.jaspermobile.presentation.model;

import com.jaspersoft.android.jaspermobile.presentation.mapper.ReportParamsTransformer;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsRequest;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.service.Session;
import com.jaspersoft.android.sdk.service.data.report.ReportOutput;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.report.ExecutionCriteria;
import com.jaspersoft.android.sdk.service.report.ReportExecution;
import com.jaspersoft.android.sdk.service.report.ReportExport;
import com.jaspersoft.android.sdk.service.report.RunExportCriteria;
import com.jaspersoft.android.sdk.service.report.RunReportCriteria;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.functions.Func0;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class ReportModel {
    private final JsRestClient mJsRestClient;
    private final Session mSession;
    private final String mReportUri;

    public ReportExecution mExecution;
    private final ReportParamsStorage mReportParamsStorage;
    private final ReportParamsTransformer mReportParamsTransformer;

    public ReportModel(JsRestClient jsRestClient,
                       Session session,
                       String reportUri,
                       ReportParamsStorage reportParamsStorage,
                       ReportParamsTransformer reportParamsTransformer) {
        mJsRestClient = jsRestClient;
        mSession = session;
        mReportUri = reportUri;
        mReportParamsStorage = reportParamsStorage;
        mReportParamsTransformer = reportParamsTransformer;
    }

    public Observable<InputControlsList> loadInputControls() {
        return Observable.defer(new Func0<Observable<InputControlsList>>() {
            @Override
            public Observable<InputControlsList> call() {
                GetInputControlsRequest request =
                        new GetInputControlsRequest(mJsRestClient, mReportUri);
                try {
                    return Observable.just(request.loadDataFromNetwork());
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }
        });
    }

    public Observable<ReportExecution> runReport() {
        return Observable.defer(new Func0<Observable<ReportExecution>>() {

            @Override
            public Observable<ReportExecution> call() {
                List<ReportParameter> params = mReportParamsStorage.getInputControlHolder(mReportUri).getReportParams();
                Map<String, Set<String>> repoParams = mReportParamsTransformer.transform(params);
                RunReportCriteria reportCriteria = RunReportCriteria.builder()
                        .params(repoParams)
                        .create();
                try {
                    ReportExecution execution = mSession.reportApi().run(mReportUri, reportCriteria);
                    mExecution = execution;
                    return Observable.just(execution);
                } catch (ServiceException e) {
                    return Observable.error(e);
                }
            }
        });
    }

    public Observable<String> downloadExport(final int page) {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                try {
                    RunExportCriteria criteria = RunExportCriteria.builder()
                            .pages(String.valueOf(page))
                            .format(ExecutionCriteria.Format.HTML)
                            .create();
                    ReportExport export = mExecution.export(criteria);
                    ReportOutput reportOutput = export.download();
                    InputStream stream = reportOutput.getStream();
                    String page = IOUtils.toString(reportOutput.getStream());
                    IOUtils.closeQuietly(stream);
                    return Observable.just(page);
                } catch (ServiceException e) {
                    return Observable.error(e);
                } catch (IOException e) {
                    return Observable.error(e);
                }
            }
        });
    }

    public void setControls(List<InputControl> controls) {
        mReportParamsStorage.getInputControlHolder(mReportUri).setInputControls(controls);
    }
}
