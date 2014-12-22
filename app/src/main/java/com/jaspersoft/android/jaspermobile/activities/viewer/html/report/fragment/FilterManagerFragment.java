package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.report.ReportOptionsActivity;
import com.jaspersoft.android.jaspermobile.activities.report.SaveReportActivity_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsRequest;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.util.FileUtils;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

import java.util.ArrayList;

import static com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity.EXTRA_REPORT_CONTROLS;
import static com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity.EXTRA_REPORT_PARAMETERS;
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
    JsRestClient jsRestClient;

    @FragmentArg
    ResourceLookup resource;

    @OptionsMenuItem
    MenuItem saveReport;
    @OptionsMenuItem
    MenuItem showFilters;

    @InstanceState
    ArrayList<InputControl> previousInputControls;
    @InstanceState
    ArrayList<InputControl> cachedInputControls;
    @InstanceState
    ArrayList<ReportParameter> reportParameters;
    @InstanceState
    boolean mShowFilterOption;
    @InstanceState
    boolean mShowSaveOption;

    private ReportExecutionFragment reportExecutionFragment;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            final GetInputControlsRequest request =
                    new GetInputControlsRequest(jsRestClient, resource.getUri());

            DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (!request.isCancelled()) {
                        getSpiceManager().cancel(request);
                        getActivity().finish();
                    }
                }
            };
            DialogInterface.OnShowListener showListener = new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    getSpiceManager().execute(request, new GetInputControlsListener());
                }
            };

            if (ProgressDialogFragment.isVisible(getFragmentManager())) {
                ProgressDialogFragment.getInstance(getFragmentManager())
                        .setOnCancelListener(cancelListener);
                // Send request
                showListener.onShow(null);
            } else {
                ProgressDialogFragment.show(getFragmentManager(), cancelListener, showListener);
            }
        }
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
                    .reportParameters(reportParameters)
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
        showReportOptions(cachedInputControls);
    }

    public void showPreviousFilters() {
        showReportOptions(previousInputControls);
    }

    private void showReportOptions(ArrayList<InputControl> inputControls) {
        // Run Report Options activity
        Intent intent = new Intent(getActivity(), ReportOptionsActivity.class);
        intent.putExtra(ReportOptionsActivity.EXTRA_REPORT_URI, resource.getUri());
        intent.putExtra(ReportOptionsActivity.EXTRA_REPORT_LABEL, resource.getLabel());
        intent.putParcelableArrayListExtra(ReportOptionsActivity.EXTRA_REPORT_CONTROLS, inputControls);
        startActivityForResult(intent, REQUEST_REPORT_PARAMETERS);
    }

    @OnActivityResult(REQUEST_REPORT_PARAMETERS)
    final void loadReportParameters(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            reportParameters = data.getParcelableArrayListExtra(EXTRA_REPORT_PARAMETERS);
            previousInputControls = cachedInputControls;
            cachedInputControls = data.getParcelableArrayListExtra(EXTRA_REPORT_CONTROLS);
            getReportExecutionFragment().executeReport(reportParameters);
        } else {
            // Check if user has experienced report loading. Otherwise remove him from this page.
            if (!getReportExecutionFragment().isResourceLoaded()) {
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

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private class GetInputControlsListener implements RequestListener<InputControlsList> {
        @Override
        public void onRequestFailure(SpiceException exception) {
            if (exception instanceof RequestCancelledException) {
                Toast.makeText(getActivity(),R.string.cancelled_msg, Toast.LENGTH_SHORT).show();
            } else {
                RequestExceptionHandler.handle(exception, getActivity(), false);
            }
            ProgressDialogFragment.dismiss(getFragmentManager());
        }

        @Override
        public void onRequestSuccess(InputControlsList controlsList) {
            ArrayList<InputControl> inputControls = Lists.newArrayList(controlsList.getInputControls());
            boolean showFilterActionVisible = !inputControls.isEmpty();
            mShowFilterOption = showFilterActionVisible;
            mShowSaveOption = true;
            getActivity().invalidateOptionsMenu();

            if (showFilterActionVisible) {
                cachedInputControls = inputControls;
                showReportOptions(inputControls);
                ProgressDialogFragment.dismiss(getFragmentManager());
            } else {
                getReportExecutionFragment().executeReport();
            }
        }
    }

}
