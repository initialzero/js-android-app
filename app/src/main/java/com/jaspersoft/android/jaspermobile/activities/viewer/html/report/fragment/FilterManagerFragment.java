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
import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.save.SaveReportActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportView;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.RequestExecutor;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.print.JasperPrintJobFactory;
import com.jaspersoft.android.jaspermobile.util.print.JasperPrinter;
import com.jaspersoft.android.jaspermobile.util.print.ResourcePrintJob;
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
import java.util.List;

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
    @Inject
    protected Analytics analytics;

    @FragmentArg
    protected ResourceLookup resource;

    @OptionsMenuItem
    protected MenuItem saveReport;
    @OptionsMenuItem (R.id.printAction)
    protected MenuItem printReport;
    @OptionsMenuItem
    protected MenuItem showFilters;

    @InstanceState
    protected boolean mShowFilterOption;
    @InstanceState
    protected boolean mShowSaveAndPrintOption;

    private boolean mPageWasLoadedAtLeastOnce;

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
        requestExecutor.execute(request, new GetInputControlsListener(), new RequestExecutor.OnProgressDialogCancelListener() {
            @Override
            public void onCancel() {
                getActivity().finish();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        saveReport.setVisible(mShowSaveAndPrintOption);
        showFilters.setVisible(mShowFilterOption);

        if (printReport != null) {
            printReport.setVisible(mShowSaveAndPrintOption);
        }
    }

    @OptionsItem
    final void saveReport() {
        if (FileUtils.isExternalStorageWritable()) {
            PaginationManagerFragment manager = (PaginationManagerFragment) getFragmentManager().findFragmentByTag(PaginationManagerFragment.TAG);

            SaveReportActivity_.intent(this)
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

    @OptionsItem
    final void printAction() {
        analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.PRINT.getValue(), Analytics.EventLabel.CLICK.getValue());
        ResourcePrintJob job = JasperPrintJobFactory
                .createReportPrintJob(getActivity(), jsRestClient, resource, paramsStorage.getInputControlHolder(resource.getUri()).getReportParams());
        JasperPrinter.print(job);
    }

    private void showReportOptions() {
        InputControlsActivity_.intent(this).reportUri(resource.getUri()).startForResult(REQUEST_REPORT_PARAMETERS);
    }

    @OnActivityResult(REQUEST_REPORT_PARAMETERS)
    final void loadReportParameters(int resultCode, Intent data) {
        boolean isFirstReportMissing = !hasSnapshot();

        if (resultCode == Activity.RESULT_OK) {
            boolean isNewParamsEqualOld = data.getBooleanExtra(InputControlsActivity.RESULT_SAME_PARAMS, false);
            if (isNewParamsEqualOld && !isFirstReportMissing) {
                return;
            }

            getReportExecutionFragment().executeReport(getReportParameters());
        } else {
            // Check if user has experienced report loading. Otherwise remove him from this page.
            if (isFirstReportMissing) {
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

    private List<ReportParameter> getReportParameters() {
        return paramsStorage.getInputControlHolder(resource.getUri()).getReportParams();
    }

    public boolean hasSnapshot() {
        return mPageWasLoadedAtLeastOnce;
    }

    public void makeSnapshot() {
        mPageWasLoadedAtLeastOnce = true;
    }

    public void disableSaveOption() {
        mShowSaveAndPrintOption = false;
        getActivity().supportInvalidateOptionsMenu();
    }

    public void enableSaveOption() {
        mShowSaveAndPrintOption = true;
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
            List<InputControl> icList = new ArrayList<>(controlsList.getInputControls());
            paramsStorage.getInputControlHolder(resource.getUri()).setInputControls(icList);
            reportView.hideErrorView();

            boolean showFilterActionVisible = !icList.isEmpty();
            mShowFilterOption = showFilterActionVisible;
            getActivity().supportInvalidateOptionsMenu();

            if (showFilterActionVisible) {
                ProgressDialogFragment.dismiss(getFragmentManager());
                showReportOptions();
            } else {
                getReportExecutionFragment().executeReport();
            }
        }
    }

}
