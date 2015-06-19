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

import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.util.report.ExportIdFormat;
import com.jaspersoft.android.jaspermobile.util.report.ExportIdFormatFactory;
import com.jaspersoft.android.jaspermobile.util.server.ServerInfoProvider;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.GetExportOutputRequest;
import com.jaspersoft.android.sdk.client.async.request.RunReportExportsRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ExportsRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.report.ReportStatus;
import com.jaspersoft.android.sdk.client.oxm.report.ReportStatusResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.apache.commons.io.IOUtils;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClientException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.TimeInterval;

/**
 * @author Tom Koptel
 * @since 2.1
 */
final class ReportPrintUnit implements PrintUnit {
    private static final long WAIT_INTERVAL = 1000;

    private final JsRestClient mJsRestClient;
    private final ResourceLookup mResource;
    private final List<ReportParameter> mReportParameters;
    private final ServerInfoProvider mServerInfoProvider;
    private ReportExecutionResponse reportExecutionResponse;

    ReportPrintUnit(JsRestClient jsRestClient, ResourceLookup resource, List<ReportParameter> reportParameters, ServerInfoProvider serverInfoProvider) {
        if (jsRestClient == null) {
            throw new IllegalArgumentException("JsRestClient should not be null");
        }
        if (resource == null) {
            throw new IllegalArgumentException("Resource should not be null");
        }
        if (reportParameters == null) {
            throw new IllegalArgumentException("Resource parameters should not be null");
        }
        if (serverInfoProvider == null) {
            throw new IllegalArgumentException("Server data provider should not be null");
        }

        this.mJsRestClient = jsRestClient;
        this.mResource = resource;
        this.mReportParameters = reportParameters;
        this.mServerInfoProvider = serverInfoProvider;
    }

    @NonNull
    @Override
    public Observable<Boolean> writeContent(final String pageRange, final ParcelFileDescriptor destination) {
        return Observable.create(
                new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        try {
                            ClientHttpResponse response = runExport(pageRange);
                            writeResponseToDestination(response, destination);
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(true);
                                subscriber.onCompleted();
                            }
                        } catch (Exception ex) {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onError(ex);
                            }
                        }
                    }
                });
    }

    @NonNull
    @Override
    public Observable<Integer> fetchPageCount() {
        return startReportExecutionAsync().flatMap(new Func1<ReportExecutionResponse, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(ReportExecutionResponse reportExecutionResponse) {
                return Observable.just(reportExecutionResponse.getTotalPages());
            }
        });
    }

    private Observable<ReportExecutionResponse> startReportExecutionAsync() {
        return startInitialReportExecution().flatMap(
                new Func1<ReportExecutionResponse, Observable<ReportExecutionResponse>>() {
                    @Override
                    public Observable<ReportExecutionResponse> call(ReportExecutionResponse response) {
                        ReportStatus status = response.getReportStatus();
                        switch (status) {
                            case ready:
                                return Observable.just(response);
                            case queued:
                            case execution:
                                return waitForReportCompletedStatus(response.getRequestId());
                            case cancelled:
                            case failed:
                                throw new RestClientException("Failed to run report execution");
                            default:
                                throw new IllegalStateException("Unexpected report status: " + String.valueOf(status));
                        }
                    }
                });
    }

    private Observable<ReportExecutionResponse> waitForReportCompletedStatus(final String executionId) {
        return Observable
                .interval(WAIT_INTERVAL, TimeUnit.MILLISECONDS).timeInterval()
                .flatMap(new Func1<TimeInterval<Long>, Observable<ReportStatusResponse>>() {
                    @Override
                    public Observable<ReportStatusResponse> call(TimeInterval<Long> longTimeInterval) {
                        return Observable.defer(new Func0<Observable<ReportStatusResponse>>() {
                            @Override
                            public Observable<ReportStatusResponse> call() {
                                ReportStatusResponse response = mJsRestClient.runReportStatusCheck(executionId);
                                return Observable.just(response);
                            }
                        });
                    }
                })
                .takeFirst(new Func1<ReportStatusResponse, Boolean>() {
                    @Override
                    public Boolean call(ReportStatusResponse response) {
                        ReportStatus status = response.getReportStatus();
                        if (status.equals(ReportStatus.cancelled) || status.equals(ReportStatus.failed)) {
                            throw new RestClientException("Report has received invalid state: " + String.valueOf(status));
                        }
                        return response.getReportStatus() == ReportStatus.ready;
                    }
                })
                .flatMap(new Func1<ReportStatusResponse, Observable<ReportExecutionResponse>>() {
                    @Override
                    public Observable<ReportExecutionResponse> call(ReportStatusResponse reportStatusResponse) {
                        return startReportDetailsRequest(executionId);
                    }
                });
    }

    private Observable<ReportExecutionResponse> startReportDetailsRequest(final String executionId) {
        return Observable.defer(new Func0<Observable<ReportExecutionResponse>>() {
            @Override
            public Observable<ReportExecutionResponse> call() {
                reportExecutionResponse = mJsRestClient.runReportDetailsRequest(executionId);
                return Observable.just(reportExecutionResponse);
            }
        });
    }

    private Observable<ReportExecutionResponse> startInitialReportExecution() {
        return Observable.defer(new Func0<Observable<ReportExecutionResponse>>() {
            @Override
            public Observable<ReportExecutionResponse> call() {
                return Observable.just(getReportExecution());
            }
        });
    }

    private ReportExecutionResponse getReportExecution() {
        if (reportExecutionResponse == null) {
            ReportExecutionRequest request = prepareReportExecutionRequest();
            reportExecutionResponse = mJsRestClient.runReportExecution(request);
        }
        return reportExecutionResponse;
    }

    private ReportExecutionRequest prepareReportExecutionRequest() {
        ReportExecutionRequest executionRequest = new ReportExecutionRequest();
        executionRequest.setReportUnitUri(mResource.getUri());
        executionRequest.setAsync(true);
        executionRequest.setInteractive(false);
        executionRequest.setOutputFormat("PDF");
        executionRequest.setEscapedAttachmentsPrefix("./");

        if (!mReportParameters.isEmpty()) {
            executionRequest.setParameters(mReportParameters);
        }

        return executionRequest;
    }

    private ClientHttpResponse runExport(String pageRange) throws Exception {
        String executionId = reportExecutionResponse.getRequestId();

        ExportsRequest exportsRequest = new ExportsRequest();
        exportsRequest.setOutputFormat("PDF");

        exportsRequest.setPages(pageRange);

        RunReportExportsRequest request = new RunReportExportsRequest(mJsRestClient, exportsRequest, executionId);
        ExportExecution exportExecutionResponse = request.loadDataFromNetwork();

        String serverVersion = mServerInfoProvider.getServerVersion();
        ExportIdFormat exportIdFormat = ExportIdFormatFactory.builder()
                .setExportsRequest(exportsRequest)
                .setExportExecution(exportExecutionResponse)
                .build()
                .createAdapter(serverVersion);

        String exportOutputId = exportIdFormat.format();

        GetExportOutputRequest getExportOutputRequest = new GetExportOutputRequest(mJsRestClient, executionId, exportOutputId);
        return getExportOutputRequest.loadDataFromNetwork();
    }

    private void writeResponseToDestination(final ClientHttpResponse httpResponse, final ParcelFileDescriptor destination) {
        OutputStream output = new FileOutputStream(destination.getFileDescriptor());
        InputStream inputStream = null;
        try {
            inputStream = httpResponse.getBody();
            IOUtils.copy(httpResponse.getBody(), output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
            IOUtils.closeQuietly(output);
            httpResponse.close();
        }
    }

}
