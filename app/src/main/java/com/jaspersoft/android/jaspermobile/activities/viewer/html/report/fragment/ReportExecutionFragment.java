package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.RequestExecutor;
import com.jaspersoft.android.jaspermobile.dialog.AlertDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.util.ReportExecutionUtil;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.CheckReportStatusRequest;
import com.jaspersoft.android.sdk.client.async.request.RunReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.report.ReportStatus;
import com.jaspersoft.android.sdk.client.oxm.report.ReportStatusResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class ReportExecutionFragment extends RoboSpiceFragment {
    public static final String TAG = ReportExecutionFragment.class.getSimpleName();

    @FragmentArg
    ResourceLookup resource;
    @FragmentArg
    double versionCode;

    @Inject
    JsRestClient jsRestClient;

    @Bean
    ReportExecutionUtil reportExecutionUtil;

    private final Handler mHandler = new Handler();
    private RequestExecutor requestExecutor;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        requestExecutor = RequestExecutor.builder()
                .setExecutionMode(RequestExecutor.Mode.VISIBLE)
                .setFragmentManager(getFragmentManager())
                .setSpiceManager(getSpiceManager())
                .create();
    }


    public void executeReport(ArrayList<ReportParameter> reportParameters) {
        ReportExecutionRequest executionData = prepareExecutionData(reportParameters);
        final RunReportExecutionRequest request = new RunReportExecutionRequest(jsRestClient, executionData);
        requestExecutor.execute(request, ReportExecutionResponse.class.getSimpleName(),
                DurationInMillis.ALWAYS_RETURNED, new RunReportExecutionListener());
    }

    public void executeReport() {
        executeReport(new ArrayList<ReportParameter>());
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
    }

    public void showEmptyReportOptionsDialog() {
        FilterManagerFragment filterManagerFragment = getFilterMangerFragment();
        if (!filterManagerFragment.hasSnapshot()) {
            AlertDialogFragment.createBuilder(getActivity(), getFragmentManager())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNegativeButton(new AlertDialogFragment.NegativeClickListener() {
                        @Override
                        public void onClick(DialogFragment fragment) {
                            FilterManagerFragment filterManagerFragment =
                                    (FilterManagerFragment) fragment.getFragmentManager()
                                            .findFragmentByTag(FilterManagerFragment.TAG);
                            if (filterManagerFragment != null) {
                                filterManagerFragment.showFilters();
                            }
                        }
                    })
                    .setTitle(R.string.warning_msg)
                    .setCancelableOnTouchOutside(false)
                    .setMessage(R.string.rv_error_empty_report)
                    .setNegativeButtonText(android.R.string.ok)
                    .show();
            return;
        }

        AlertDialogFragment.createBuilder(getActivity(), getFragmentManager())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton(new AlertDialogFragment.NegativeClickListener() {
                    @Override
                    public void onClick(DialogFragment fragment) {
                        FilterManagerFragment filterManagerFragment =
                                (FilterManagerFragment) fragment.getFragmentManager()
                                        .findFragmentByTag(FilterManagerFragment.TAG);
                        if (filterManagerFragment != null) {
                            filterManagerFragment.showPreviousReport();
                        }
                    }
                })
                .setPositiveButton(new AlertDialogFragment.PositiveClickListener() {
                    @Override
                    public void onClick(DialogFragment fragment) {
                        FilterManagerFragment filterManagerFragment =
                                (FilterManagerFragment) fragment.getFragmentManager()
                                        .findFragmentByTag(FilterManagerFragment.TAG);
                        if (filterManagerFragment != null) {
                            filterManagerFragment.showFilters();
                        }
                    }
                })
                .setNegativeButtonText(android.R.string.cancel)
                .setPositiveButtonText(android.R.string.ok)
                .setCancelableOnTouchOutside(false)
                .setTitle(R.string.rv_error_empty_report_title)
                .setMessage(R.string.rv_error_empty_report_message)
                .show();
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private ReportExecutionRequest prepareExecutionData(ArrayList<ReportParameter> reportParameters) {
        ReportExecutionRequest executionData = new ReportExecutionRequest();

        reportExecutionUtil.setupAttachmentPrefix(executionData, versionCode);
        reportExecutionUtil.setupBaseUrl(executionData);

        executionData.setReportUnitUri(resource.getUri());
        executionData.setOutputFormat("html");
        executionData.setAsync(true);
        executionData.setFreshData(true);
        executionData.setIgnorePagination(false);
        if (!reportParameters.isEmpty()) {
            executionData.setParameters(reportParameters);
        }

        boolean interactive = !(versionCode >= ServerInfo.VERSION_CODES.EMERALD_THREE && versionCode < ServerInfo.VERSION_CODES.AMBER);
        executionData.setInteractive(interactive);

        return executionData;
    }

    @NonNull
    private FilterManagerFragment getFilterMangerFragment() {
        return (FilterManagerFragment)
                getFragmentManager().findFragmentByTag(FilterManagerFragment.TAG);
    }

    @NonNull
    private PaginationManagerFragment getPaginationManagerFragment() {
        return (PaginationManagerFragment)
                    getFragmentManager().findFragmentByTag(PaginationManagerFragment.TAG);
    }

    private boolean isStatusPending(ReportStatus status) {
        return (status == ReportStatus.queued || status == ReportStatus.execution);
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private class RunReportExecutionListener implements RequestListener<ReportExecutionResponse> {
        @Override
        public void onRequestFailure(SpiceException exception) {
            if (exception instanceof RequestCancelledException) {
                Toast.makeText(getActivity(), R.string.cancelled_msg, Toast.LENGTH_SHORT).show();
            } else {
                RequestExceptionHandler.handle(exception, getActivity(), true);
            }
            ProgressDialogFragment.dismiss(getFragmentManager());
        }

        public void onRequestSuccess(ReportExecutionResponse response) {
            // This is possible in the test case, as soon as we are stubing out all responses
            if (response == null) {
                ProgressDialogFragment.dismiss(getFragmentManager());
                return;
            }

            PaginationManagerFragment paginationManagerFragment = getPaginationManagerFragment();
            final String requestId = response.getRequestId();
            paginationManagerFragment.setRequestId(requestId);

            ReportStatus status = response.getReportStatus();
            if (status == ReportStatus.ready) {
                int totalPageCount = response.getTotalPages();
                paginationManagerFragment.showTotalPageCount(totalPageCount);

                if (totalPageCount == 0) {
                    getFilterMangerFragment().makeSnapshot();
                    showEmptyReportOptionsDialog();
                } else {
                    paginationManagerFragment.paginateToCurrentSelection();
                    paginationManagerFragment.loadNextPageInBackground();
                }
            } else if (isStatusPending(status)) {
                paginationManagerFragment.paginateToCurrentSelection();
                paginationManagerFragment.loadNextPageInBackground();
                mHandler.postDelayed(new StatusCheckTask(requestId), TimeUnit.SECONDS.toMillis(1));
            }
        }
    }

    private class StatusCheckTask implements Runnable {
        private final String requestId;

        private StatusCheckTask(String requestId) {
            this.requestId = requestId;
        }

        @Override
        public void run() {
            CheckReportStatusRequest checkReportStatusRequest =
                    new CheckReportStatusRequest(jsRestClient, requestId);
            CheckReportStatusRequestListener checkReportStatusRequestListener =
                    new CheckReportStatusRequestListener(requestId);
            getSpiceManager().execute(checkReportStatusRequest, checkReportStatusRequestListener);
        }
    }

    private class CheckReportStatusRequestListener implements RequestListener<ReportStatusResponse> {
        private final String requestId;

        private CheckReportStatusRequestListener(String requestId) {
            this.requestId = requestId;
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, getActivity(), true);
        }

        @Override
        public void onRequestSuccess(ReportStatusResponse response) {
            ReportStatus status = response.getReportStatus();
            if (status == ReportStatus.ready) {
                getPaginationManagerFragment().update();
            } else if (isStatusPending(status)) {
                mHandler.postDelayed(new StatusCheckTask(requestId), TimeUnit.SECONDS.toMillis(1));
            }
        }
    }

}
