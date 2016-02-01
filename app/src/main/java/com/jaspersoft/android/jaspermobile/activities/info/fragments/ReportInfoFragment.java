package com.jaspersoft.android.jaspermobile.activities.info.fragments;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.save.SaveReportActivity_;
import com.jaspersoft.android.jaspermobile.activities.schedule.ScheduleActivity_;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.ReportOptionsFragmentDialog;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.ResourceOpener;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.sdk.client.async.request.ReportOptionsRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsRequest;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.report.option.ReportOptionResponse;
import com.jaspersoft.android.sdk.service.data.report.option.ReportOption;
import com.jaspersoft.android.sdk.util.FileUtils;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@OptionsMenu({R.menu.report_schedule, R.menu.save_item_menu, R.menu.report_options_menu})
@EFragment(R.layout.fragment_resource_info)
public class ReportInfoFragment extends ResourceInfoFragment
        implements ReportOptionsFragmentDialog.ReportOptionsDialogClickListener {

    @Bean
    protected ResourceOpener resourceOpener;

    @Inject
    protected ReportParamsStorage paramsStorage;

    @OptionsMenuItem(R.id.showReportOptions)
    protected MenuItem reportOptions;

    @OptionsMenuItem(R.id.saveAction)
    protected MenuItem saveOptions;

    private boolean mReportOptionsExist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestReportOptions();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        reportOptions.setVisible(mReportOptionsExist && mResourceLookup != null);
        saveOptions.setVisible(mResourceLookup != null);
    }

    @OptionsItem(R.id.saveAction)
    protected void saveReport() {
        if (FileUtils.isExternalStorageWritable()) {
            SaveReportActivity_.intent(this)
                    .resource(mResourceLookup)
                    .pageCount(0)
                    .start();
        } else {
            Toast.makeText(getActivity(),
                    R.string.rv_t_external_storage_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    @OptionsItem(R.id.showReportOptions)
    protected void showReportOptions() {
        ReportOptionsFragmentDialog.createBuilder(getFragmentManager())
                .setReportUri(jasperResource.getId())
                .setCancelableOnTouchOutside(true)
                .setTargetFragment(this)
                .show();
    }

    @OptionsItem(R.id.newSchedule)
    protected void schedule() {
        ScheduleActivity_.intent(getActivity())
                .jasperResource(jasperResource)
                .start();
    }

    @Override
    public void onOptionSelected(ReportOption reportOption) {
        showProgressDialog();

        GetInputControlsRequest request = new GetInputControlsRequest(jsRestClient, reportOption.getUri());
        getSpiceManager().execute(request, new GetInputControlsListener());

        analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.OPENED.getValue(), Analytics.EventLabel.WITH_RO.getValue());
    }

    private void showProgressDialog() {
        ProgressDialogFragment.builder(getFragmentManager())
                .setLoadingMessage(R.string.loading_msg)
                .show();
    }

    private void hideProgressDialog() {
        if (ProgressDialogFragment.isVisible(getFragmentManager())) {
            ProgressDialogFragment.dismiss(getFragmentManager());
        }
    }

    private ArrayList<ReportParameter> initParametersUsingSelectedValues(List<InputControl> inputControls) {
        ArrayList<ReportParameter> parameters = new ArrayList<>();
        for (InputControl inputControl : inputControls) {
            parameters.add(new ReportParameter(inputControl.getId(), inputControl.getSelectedValues()));
        }
        return parameters;
    }

    private void requestReportOptions() {
        Account account = JasperAccountManager.get(getActivity()).getActiveAccount();
        AccountServerData serverData = AccountServerData.get(getActivity(), account);
        boolean isProJrs = serverData.getEdition().equals("PRO");
        if (isProJrs) {
            ReportOptionsRequest runReportExecutionRequest = new ReportOptionsRequest(jsRestClient, jasperResource.getId());
            getSpiceManager().execute(runReportExecutionRequest, new GetReportOptionsListener());
        }
    }

    private class GetInputControlsListener extends SimpleRequestListener<InputControlsList> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            super.onRequestFailure(spiceException);
            paramsStorage.clearInputControlHolder(jasperResource.getId());
            hideProgressDialog();
        }

        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        public void onRequestSuccess(InputControlsList controlsList) {
            ArrayList<InputControl> inputControls = new ArrayList<>(controlsList.getInputControls());
            paramsStorage.getInputControlHolder(jasperResource.getId()).setInputControls(inputControls);
            paramsStorage.getInputControlHolder(jasperResource.getId()).setReportParams(initParametersUsingSelectedValues(inputControls));
            resourceOpener.openResource(ReportInfoFragment.this, mResourceLookup);
            hideProgressDialog();
        }
    }

    private class GetReportOptionsListener extends SimpleRequestListener<ReportOptionResponse> {
        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {

        }

        @Override
        public void onRequestSuccess(ReportOptionResponse reportOptionResponse) {
            mReportOptionsExist = !reportOptionResponse.getOptions().isEmpty();
            getActivity().invalidateOptionsMenu();
        }
    }
}
