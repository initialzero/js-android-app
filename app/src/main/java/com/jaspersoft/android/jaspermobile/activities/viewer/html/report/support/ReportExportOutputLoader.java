package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.google.common.base.Preconditions;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.PaginationManagerFragment;
import com.jaspersoft.android.jaspermobile.dialog.AlertDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.ExceptionRule;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.network.UniversalRequestListener;
import com.jaspersoft.android.jaspermobile.util.ReportExecutionUtil;
import com.jaspersoft.android.jaspermobile.util.ReportExecutionUtil_;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.RunReportExportOutputRequest;
import com.jaspersoft.android.sdk.client.async.request.RunReportExportsRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ErrorDescriptor;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ExportsRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportDataResponse;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ReportExportOutputLoader {
    private final RoboSpiceFragment controllFragment;
    private final FragmentManager fragmentManager;
    private final RequestExecutor requestExecutor;
    private final String requestId;
    private final JsRestClient jsRestClient;
    private final double versionCode;
    private final ReportExecutionUtil reportExecutionUtil;
    private ResultListener resultListener;

    public static Builder builder() {
        return new Builder();
    }

    private ReportExportOutputLoader(RoboSpiceFragment fragment,
                                     RequestExecutor.Mode executionMode,
                                     JsRestClient jsRestClient,
                                     String requestId,
                                     double versionCode) {
        this.controllFragment = fragment;
        this.fragmentManager = fragment.getFragmentManager();
        this.versionCode = versionCode;
        this.jsRestClient = jsRestClient;
        this.requestId = requestId;
        this.reportExecutionUtil = ReportExecutionUtil_
                .getInstance_(controllFragment.getActivity());
        this.requestExecutor = RequestExecutor.builder()
                .setSpiceManager(fragment.getSpiceManager())
                .setFragmentManager(fragmentManager)
                .setExecutionMode(executionMode)
                .create();
    }

    public void setResultListener(ResultListener resultListener) {
        this.resultListener = resultListener;
    }

    public void loadByPage(int page) {
        ExportsRequest executionData = new ExportsRequest();
        reportExecutionUtil.setupAttachmentPrefix(executionData, versionCode);
        reportExecutionUtil.setupBaseUrl(executionData);
        executionData.setOutputFormat("html");
        executionData.setPages(String.valueOf(page));

        final RunReportExportsRequest request = new RunReportExportsRequest(jsRestClient,
                executionData, requestId);
        requestExecutor.execute(request, new RunReportExportsRequestListener(page));
    }

    private void handleFailure(SpiceException exception) {
        Activity activity = controllFragment.getActivity();
        if (controllFragment.isVisible() && activity != null) {
            if (exception instanceof RequestCancelledException) {
                Toast.makeText(activity, R.string.cancelled_msg, Toast.LENGTH_SHORT).show();
            } else {
                RequestExceptionHandler.handle(exception, activity, false);
            }
            ProgressDialogFragment.dismiss(fragmentManager);
        }
    }

    private class RunReportExportsRequestListener implements RequestListener<ExportExecution> {
        private final int mPage;

        private RunReportExportsRequestListener(int page) {
            mPage = page;
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            if (!requestExecutor.runsInSilentMode()) {
                handleFailure(exception);
            }
        }

        @Override
        public void onRequestSuccess(ExportExecution response) {
            String executionId = response.getId();
            if (versionCode <= ServerInfo.VERSION_CODES.EMERALD_TWO) {
                executionId = ("html;pages=" + mPage);
            }

            RunReportExportOutputRequestListener semanticListener
                    = new RunReportExportOutputRequestListener(executionId);
            UniversalRequestListener<ReportDataResponse> listener =
                    UniversalRequestListener.builder(controllFragment.getActivity())
                            .semanticListener(semanticListener)
                            .removeRule(ExceptionRule.FORBIDDEN)
                            .closeActivityMode()
                            .create();

            RunReportExportOutputRequest request = new RunReportExportOutputRequest(jsRestClient,
                    requestId, executionId);
            requestExecutor.execute(request, listener);
        }
    }

    private class RunReportExportOutputRequestListener implements UniversalRequestListener.SemanticListener<ReportDataResponse> {
        private final String mExecutionId;

        private RunReportExportOutputRequestListener(String executionId) {
            mExecutionId = executionId;
        }

        @Override
        public void onSemanticFailure(SpiceException spiceException) {
            ProgressDialogFragment.dismiss(fragmentManager);

            if (requestExecutor.runsInSilentMode()) {
                return;
            }

            HttpStatus httpStatus = RequestExceptionHandler.extractStatusCode(spiceException);
            if (httpStatus == HttpStatus.FORBIDDEN) {
                HttpStatusCodeException exception = (HttpStatusCodeException)
                        spiceException.getCause();
                ErrorDescriptor errorDescriptor = ErrorDescriptor.valueOf(exception);

                boolean outOfRange = errorDescriptor.getErrorCode().equals("export.pages.out.of.range");
                if (outOfRange) {
                    AlertDialogFragment.createBuilder(controllFragment.getActivity(), fragmentManager)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNegativeButton(new AlertDialogFragment.NegativeClickListener() {
                                @Override
                                public void onClick(DialogFragment fragment) {
                                    fragment.getActivity().finish();
                                }
                            })
                            .setPositiveButton(new AlertDialogFragment.PositiveClickListener() {
                                @Override
                                public void onClick(DialogFragment fragment) {
                                    PaginationManagerFragment paginationManagerFragment =
                                            (PaginationManagerFragment) fragment.getFragmentManager()
                                                    .findFragmentByTag(PaginationManagerFragment.TAG);
                                    paginationManagerFragment.paginateTo(1);

                                }
                            })
                            .setTitle(R.string.rv_out_of_range)
                            .setMessage(errorDescriptor.getMessage())
                            .setCancelableOnTouchOutside(false)
                            .setNegativeButtonText(android.R.string.cancel)
                            .setPositiveButtonText(R.string.rv_dialog_reload)
                            .show();
                } else {
                    AlertDialogFragment.createBuilder(controllFragment.getActivity(), fragmentManager)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(errorDescriptor.getErrorCode())
                            .setMessage(errorDescriptor.getMessage())
                            .setCancelableOnTouchOutside(false)
                            .show();
                }
            } else {
                handleFailure(spiceException);
            }
            if (resultListener != null) {
                resultListener.onFailure();
            }
        }

        @Override
        public void onSemanticSuccess(ReportDataResponse response) {
            ProgressDialogFragment.dismiss(fragmentManager);
            ExportOutputData exportOutputData = ExportOutputData.builder()
                    .setExecutionId(mExecutionId)
                    .setResponse(response)
                    .create();
            if (resultListener != null) {
                resultListener.onSuccess(exportOutputData);
            }
        }
    }

    public static interface ResultListener {
        public void onFailure();
        public void onSuccess(ExportOutputData exportOutputData);
    }

    public static class Builder {
        private ResultListener resultListener;
        private RoboSpiceFragment fragment;
        private RequestExecutor.Mode executionMode;
        private JsRestClient jsRestClient;
        private String requestId;
        private double versionCode;

        public Builder() {
            this.executionMode = RequestExecutor.Mode.VISIBLE;
        }

        public Builder setControlFragment(RoboSpiceFragment fragment) {
            this.fragment = fragment;
            return this;
        }

        public Builder setExecutionMode(RequestExecutor.Mode executionMode) {
            this.executionMode = executionMode;
            return this;
        }

        public Builder setVersionCode(double versionCode) {
            this.versionCode = versionCode;
            return this;
        }

       public Builder setJSRestClient(JsRestClient jsRestClient) {
            this.jsRestClient = jsRestClient;
            return this;
        }

        public Builder setRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder setResultListener(ResultListener resultListener) {
            this.resultListener = resultListener;
            return this;
        }

        public ReportExportOutputLoader create() {
            Preconditions.checkNotNull(fragment);
            Preconditions.checkNotNull(executionMode);
            Preconditions.checkNotNull(jsRestClient);
            Preconditions.checkNotNull(requestId);
            Preconditions.checkArgument(versionCode != 0d);

            ReportExportOutputLoader reportExportOutputLoader =
                    new ReportExportOutputLoader(fragment, executionMode,
                    jsRestClient, requestId, versionCode);
            reportExportOutputLoader.setResultListener(resultListener);
            return reportExportOutputLoader;
        }
    }
}
