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

package com.jaspersoft.android.jaspermobile.data.service;

import com.jaspersoft.android.jaspermobile.domain.service.ReportExecutionService;
import com.jaspersoft.android.jaspermobile.domain.service.ReportService;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.service.Session;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.report.ReportExecution;
import com.jaspersoft.android.sdk.service.report.RunReportCriteria;

import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.functions.Func0;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class RestReportService implements ReportService {
    private final JsRestClient mJsRestClient;
    private final Session mSession;

    public RestReportService(JsRestClient jsRestClient,
                             Session session) {
        mJsRestClient = jsRestClient;
        mSession = session;
    }

    @Override
    public Observable<List<InputControl>> loadControls(final String reportUri) {
        return Observable.defer(new Func0<Observable<List<InputControl>>>() {
            @Override
            public Observable<List<InputControl>> call() {
                List<InputControl> controls = mJsRestClient.getInputControls(reportUri);
                return Observable.just(controls);
            }
        });
    }

    @Override
    public Observable<ReportExecutionService> runReport(final String reportUri, final Map<String, Set<String>> params) {
        return Observable.defer(new Func0<Observable<ReportExecutionService>>() {
            @Override
            public Observable<ReportExecutionService> call() {
                RunReportCriteria criteria = RunReportCriteria.builder()
                        .params(params)
                        .create();
                try {
                    ReportExecution reportExecution = mSession.reportApi().run(reportUri, criteria);
                    ReportExecutionService executionService = new RestReportExecutionService(reportExecution);
                    return Observable.just(executionService);
                } catch (ServiceException e) {
                    return Observable.error(e);
                }
            }
        });
    }
}