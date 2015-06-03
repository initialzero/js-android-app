package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.ReportExecutionUtil;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.RunReportExportOutputRequest;
import com.jaspersoft.android.sdk.client.async.request.RunReportExportsRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ErrorDescriptor;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ExportsRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportDataResponse;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean
public class ReportExportOutputLoader {

    @Inject
    protected JsRestClient jsRestClient;

    @RootContext
    protected FragmentActivity context;
    @Bean
    protected ReportExecutionUtil reportExecutionUtil;
    @Bean
    protected ReportSession reportSession;

    private ResultListener resultListener;
    private RequestExecutor requestExecutor;

    @AfterInject
    protected void injectRoboGuiceDependencies() {
        final RoboInjector injector = RoboGuice.getInjector(context);
        injector.injectMembersWithoutViews(this);
    }

    public void loadByPage(RequestExecutor requestExecutor, ResultListener resultListener, int page) {
        if (page == 0) {
            throw new IllegalArgumentException("Page cant be 0");
        }
        if (requestExecutor == null) {
            throw new IllegalArgumentException("Request executor can`t be null");
        }

        this.requestExecutor = requestExecutor;
        this.resultListener = resultListener;

        ExportsRequest executionData = new ExportsRequest();
        reportExecutionUtil.setupAttachmentPrefix(executionData);
        reportExecutionUtil.setupBaseUrl(executionData);
        executionData.setOutputFormat("html");
        executionData.setPages(String.valueOf(page));

        final RunReportExportsRequest request = new RunReportExportsRequest(jsRestClient,
                executionData, reportSession.getRequestId());
        requestExecutor.execute(request, new RunReportExportsRequestListener(page), new RequestExecutor.OnProgressDialogCancelListener() {
            @Override
            public void onCancel() {
                context.finish();
            }
        });
    }

    private class RunReportExportsRequestListener extends SimpleRequestListener<ExportExecution> {
        private final int mPage;

        private RunReportExportsRequestListener(int page) {
            mPage = page;
        }

        @Override
        protected Context getContext() {
            return context;
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            if (!requestExecutor.runsInSilentMode()) {
                super.onRequestFailure(exception);
                ProgressDialogFragment.dismiss(context.getSupportFragmentManager());
            }
        }

        @Override
        public void onRequestSuccess(ExportExecution response) {
            String executionId = reportExecutionUtil.createExecutionId(response, String.valueOf(mPage));

            RunReportExportOutputRequest request = new RunReportExportOutputRequest(jsRestClient,
                    reportSession.getRequestId(), executionId);
            requestExecutor.execute(request, new RunReportExportOutputRequestListener(executionId), new RequestExecutor.OnProgressDialogCancelListener() {
                @Override
                public void onCancel() {
                    context.finish();
                }
            });
        }
    }

    private class RunReportExportOutputRequestListener extends SimpleRequestListener<ReportDataResponse> {
        private final String mExecutionId;

        private RunReportExportOutputRequestListener(String executionId) {
            mExecutionId = executionId;
        }

        @Override
        protected Context getContext() {
            return context;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            ProgressDialogFragment.dismiss(context.getSupportFragmentManager());

            if (resultListener != null) {
                resultListener.onFailure(spiceException);
            }

            if (requestExecutor.runsInSilentMode()) {
                return;
            }

            int httpStatus = RequestExceptionHandler.extractStatusCode(spiceException);
            if (httpStatus == HttpStatus.FORBIDDEN.value()) {
                HttpStatusCodeException exception = (HttpStatusCodeException)
                        spiceException.getCause();
                ErrorDescriptor errorDescriptor = ErrorDescriptor.valueOf(exception);

                boolean outOfRange = errorDescriptor.getErrorCode().equals("export.pages.out.of.range");
                if (resultListener != null) {
                    resultListener.onOutOfRange(outOfRange, errorDescriptor);
                }
            } else {
                super.onRequestFailure(spiceException);
                ProgressDialogFragment.dismiss(context.getSupportFragmentManager());
            }
        }

        @Override
        public void onRequestSuccess(ReportDataResponse reportDataResponse) {
            ProgressDialogFragment.dismiss(context.getSupportFragmentManager());
            ExportOutputData exportOutputData = ExportOutputData.builder()
                    .setExecutionId(mExecutionId)
                    .setResponse(reportDataResponse)
                    .create();
            if (resultListener != null) {
                resultListener.onSuccess(exportOutputData);
            }
        }
    }

    public static interface ResultListener {
        public void onFailure(Exception exception);
        public void onSuccess(ExportOutputData exportOutputData);
        public void onOutOfRange(boolean isOutOfRange, ErrorDescriptor errorDescriptor );
    }
}
