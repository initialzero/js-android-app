package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.ReportSession;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.RequestExecutor;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.SimpleDialogFragment;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.ReportExecutionUtil;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.CheckReportStatusRequest;
import com.jaspersoft.android.sdk.client.async.request.ReportDetailsRequest;
import com.jaspersoft.android.sdk.client.async.request.RunReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.report.ReportStatus;
import com.jaspersoft.android.sdk.client.oxm.report.ReportStatusResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.octo.android.robospice.persistence.exception.SpiceException;

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
public class ReportExecutionFragment extends RoboSpiceFragment implements SimpleDialogFragment.SimpleDialogClickListener {
    public static final String TAG = ReportExecutionFragment.class.getSimpleName();

    private final int EMPTY_REPORT_DIALOG_WITH_SNAPSHOT = 121;
    private final int EMPTY_REPORT_DIALOG_WITHOUT_SNAPSHOT = 122;

    @FragmentArg
    ResourceLookup resource;

    @Inject
    JsRestClient jsRestClient;

    @Bean
    ReportExecutionUtil reportExecutionUtil;

    @Bean
    ReportSession reportSession;

    private final Handler mHandler = new Handler();
    private RequestExecutor requestExecutor;
    private String requestId;

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
        requestExecutor.execute(request, new RunReportExecutionListener());
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
            SimpleDialogFragment.createBuilder(getActivity(), getFragmentManager())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.warning_msg)
                    .setMessage(R.string.rv_error_empty_report)
                    .setNegativeButtonText(android.R.string.ok)
                    .setTargetFragment(this)
                    .setCancelableOnTouchOutside(false)
                    .setRequestCode(EMPTY_REPORT_DIALOG_WITH_SNAPSHOT)
                    .show();
            return;
        }

        SimpleDialogFragment.createBuilder(getActivity(), getFragmentManager())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButtonText(android.R.string.cancel)
                .setPositiveButtonText(android.R.string.ok)
                .setTitle(R.string.rv_error_empty_report_title)
                .setMessage(R.string.rv_error_empty_report_message)
                .setTargetFragment(this)
                .setCancelableOnTouchOutside(false)
                .setRequestCode(EMPTY_REPORT_DIALOG_WITHOUT_SNAPSHOT)
                .show();
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private ReportExecutionRequest prepareExecutionData(ArrayList<ReportParameter> reportParameters) {
        ReportExecutionRequest executionData = new ReportExecutionRequest();

        reportExecutionUtil.setupInteractiveness(executionData);
        reportExecutionUtil.setupAttachmentPrefix(executionData);
        reportExecutionUtil.setupBaseUrl(executionData);

        executionData.setReportUnitUri(resource.getUri());
        executionData.setOutputFormat("html");
        executionData.setAsync(true);
        executionData.setFreshData(true);
        executionData.setIgnorePagination(false);
        if (!reportParameters.isEmpty()) {
            executionData.setParameters(reportParameters);
        }

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
    // Implements DeleteDialogFragment.DeleteDialogClickListener
    //---------------------------------------------------------------------

    @Override
    public void onPositiveClick(int requestCode) {
        FilterManagerFragment filterManagerFragment =
                (FilterManagerFragment) getFragmentManager()
                        .findFragmentByTag(FilterManagerFragment.TAG);
        if (filterManagerFragment != null) {
            filterManagerFragment.showFilters();
        }
    }

    @Override
    public void onNegativeClick(int requestCode) {
        FilterManagerFragment filterManagerFragment =
                (FilterManagerFragment) getFragmentManager()
                        .findFragmentByTag(FilterManagerFragment.TAG);
        if (filterManagerFragment != null) {
            if (requestCode == EMPTY_REPORT_DIALOG_WITH_SNAPSHOT) {
                filterManagerFragment.showFilters();
            } else {
                filterManagerFragment.showPreviousReport();
            }
        }
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private class RunReportExecutionListener extends SimpleRequestListener<ReportExecutionResponse> {

        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            super.onRequestFailure(exception);
            ProgressDialogFragment.dismiss(getFragmentManager());
        }

        public void onRequestSuccess(ReportExecutionResponse response) {
            // This is possible in the test case, as soon as we are stubing out all responses
            if (response == null) {
                ProgressDialogFragment.dismiss(getFragmentManager());
                return;
            }

            PaginationManagerFragment paginationManagerFragment = getPaginationManagerFragment();
            requestId = response.getRequestId();
            reportSession.setRequestId(requestId);

            ReportStatus status = response.getReportStatus();
            if (status == ReportStatus.ready) {
                int totalPageCount = response.getTotalPages();
                reportSession.setTotalPage(totalPageCount);

                if (totalPageCount == 0) {
                    showEmptyReportOptionsDialog();
                } else {
                    getFilterMangerFragment().makeSnapshot();
                    paginationManagerFragment.paginateToCurrentSelection();
                    paginationManagerFragment.loadNextPageInBackground();
                }
            } else if (isStatusPending(status)) {
                getFilterMangerFragment().makeSnapshot();
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

    private class CheckReportStatusRequestListener extends SimpleRequestListener<ReportStatusResponse> {
        private final String requestId;

        private CheckReportStatusRequestListener(String requestId) {
            this.requestId = requestId;
        }

        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        public void onRequestSuccess(ReportStatusResponse response) {
            ReportStatus status = response.getReportStatus();
            if (status == ReportStatus.ready) {
                ReportDetailsRequest reportDetailsRequest = new ReportDetailsRequest(jsRestClient, requestId);
                getSpiceManager().execute(reportDetailsRequest, new ReportDetailsRequestListener());
            } else if (isStatusPending(status)) {
                mHandler.postDelayed(new StatusCheckTask(requestId), TimeUnit.SECONDS.toMillis(1));
            }
        }
    }

    private class ReportDetailsRequestListener extends SimpleRequestListener<ReportExecutionResponse> {

        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        public void onRequestSuccess(ReportExecutionResponse reportExecutionResponse) {
            int totalPageCount = reportExecutionResponse.getTotalPages();
            reportSession.setTotalPage(totalPageCount);

            if (totalPageCount == 0) {
                showEmptyReportOptionsDialog();
            } else {
                getFilterMangerFragment().makeSnapshot();
            }
        }
    }

}
