package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.report.ReportOptionsActivity;
import com.jaspersoft.android.jaspermobile.activities.report.SaveReportActivity_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportView;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.RequestExecutor;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsRequest;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.util.FileUtils;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

import java.util.ArrayList;

import static com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity.REQUEST_REPORT_PARAMETERS;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
@OptionsMenu(R.menu.report_filter_manager_menu)
public class FilterManagerFragment extends RoboSpiceFragment {
    public static final String TAG = FilterManagerFragment.class.getSimpleName();

    @Inject
    protected JsRestClient jsRestClient;
    @Inject
    protected ReportParamsStorage paramsStorage;

    @FragmentArg
    protected ResourceLookup resource;

    @OptionsMenuItem
    protected MenuItem saveReport;
    @OptionsMenuItem
    protected MenuItem showFilters;

    @InstanceState
    protected boolean mShowFilterOption;
    @InstanceState
    protected boolean mShowSaveOption;

    private boolean mPageWasLoadedAtLeastOnce;

    private ArrayList<ReportParameter> reportParameters;
    private ArrayList<InputControl> inputControls;

    private ReportExecutionFragment reportExecutionFragment;
    private RequestExecutor requestExecutor;
    private ReportView reportView;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        reportView = (ReportView) getActivity();
        requestExecutor = RequestExecutor.builder()
                .setExecutionMode(RequestExecutor.Mode.VISIBLE)
                .setFragmentManager(getFragmentManager())
                .setSpiceManager(getSpiceManager())
                .create();

        final GetInputControlsRequest request =
                new GetInputControlsRequest(jsRestClient, resource.getUri());
        requestExecutor.execute(request, new GetInputControlsListener());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        saveReport.setVisible(mShowSaveOption);
        showFilters.setVisible(mShowFilterOption);
    }

    @OptionsItem
    final void saveReport() {
        if (FileUtils.isExternalStorageWritable()) {
            PaginationManagerFragment manager = (PaginationManagerFragment) getFragmentManager().findFragmentByTag(PaginationManagerFragment.TAG);

            SaveReportActivity_.intent(this)
                    .reportParameters(paramsStorage.getReportParameters(resource.getUri()))
                    .resource(resource)
                    .pageCount(manager.mTotalPage)
                    .start();
        } else {
            Toast.makeText(getActivity(),
                    R.string.rv_t_external_storage_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    @OptionsItem
    public void showFilters() {
        showReportOptions();
    }

    private void showReportOptions() {
        Intent intent = new Intent(getActivity(), ReportOptionsActivity.class);
        intent.putExtra(ReportOptionsActivity.EXTRA_REPORT_URI, resource.getUri());
        intent.putExtra(ReportOptionsActivity.EXTRA_REPORT_LABEL, resource.getLabel());
        startActivityForResult(intent, REQUEST_REPORT_PARAMETERS);
    }

    @OnActivityResult(REQUEST_REPORT_PARAMETERS)
    final void loadReportParameters(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            getReportExecutionFragment().executeReport(getReportParameters());
        } else {
            // Check if user has experienced report loading. Otherwise remove him from this page.
            if (!hasSnapshot()) {
                getActivity().finish();
            }
        }
    }

    private ReportExecutionFragment getReportExecutionFragment() {
        if (reportExecutionFragment == null) {
            reportExecutionFragment = (ReportExecutionFragment)
                    getFragmentManager().findFragmentByTag(ReportExecutionFragment.TAG);
        }
        return reportExecutionFragment;
    }

    private ArrayList<ReportParameter> getReportParameters() {
        return paramsStorage.getReportParameters(resource.getUri());
    }

    public boolean hasSnapshot() {
        return mPageWasLoadedAtLeastOnce;
    }

    public void makeSnapshot() {
        mPageWasLoadedAtLeastOnce = true;
    }

    public void disableSaveOption() {
        mShowSaveOption = false;
        getActivity().supportInvalidateOptionsMenu();
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private class GetInputControlsListener extends SimpleRequestListener<InputControlsList> {

        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            super.onRequestFailure(exception);

            ProgressDialogFragment.dismiss(getFragmentManager());

            String errorMessage = RequestExceptionHandler.extractMessage(getActivity(), exception);
            reportView.showErrorView(errorMessage);
        }

        @Override
        public void onRequestSuccess(InputControlsList controlsList) {
            reportParameters = new ArrayList<ReportParameter>();
            inputControls = new ArrayList<InputControl>(controlsList.getInputControls());
            reportView.hideErrorView();

            boolean showFilterActionVisible = !inputControls.isEmpty();
            mShowFilterOption = showFilterActionVisible;
            mShowSaveOption = true;
            getActivity().supportInvalidateOptionsMenu();

            if (showFilterActionVisible) {
                ProgressDialogFragment.dismiss(getFragmentManager());
                paramsStorage.putReportParameters(resource.getUri(), reportParameters);
                paramsStorage.putInputControls(resource.getUri(), inputControls);
                showReportOptions();
            } else {
                getReportExecutionFragment().executeReport();
            }
        }
    }

}
