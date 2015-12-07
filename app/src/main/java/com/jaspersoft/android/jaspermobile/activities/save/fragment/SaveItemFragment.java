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

package com.jaspersoft.android.jaspermobile.activities.save.fragment;

import android.accounts.Account;
import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.save.SaveReportService_;
import com.jaspersoft.android.jaspermobile.dialog.NumberDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.RunReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.util.FileUtils;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.List;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.save_report_layout)
@OptionsMenu(R.menu.save_item_menu)
public class SaveItemFragment extends RoboSpiceFragment implements NumberDialogFragment.NumberDialogClickListener {

    public static final String TAG = SaveItemFragment.class.getSimpleName();

    private final static int FROM_PAGE_REQUEST_CODE = 1243;
    private final static int TO_PAGE_REQUEST_CODE = 2243;

    @ViewById(R.id.output_format_spinner)
    Spinner formatSpinner;
    @ViewById(R.id.report_name_input)
    EditText reportNameInput;

    @ViewById
    LinearLayout rangeControls;
    @ViewById
    TextView fromPageControl;
    @ViewById
    TextView toPageControl;

    @FragmentArg
    ResourceLookup resource;
    @FragmentArg
    int pageCount;

    @Inject
    JsRestClient jsRestClient;
    @Inject
    protected ReportParamsStorage paramsStorage;

    @OptionsMenuItem
    MenuItem saveAction;

    private int mFromPage;
    private int mToPage;
    private JasperAccountManager accountManager;

    public enum OutputFormat {
        HTML,
        PDF,
        XLS
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hasOptionsMenu();

        accountManager = JasperAccountManager.get(getActivity());

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.sr_ab_title);
        }
    }

    @OptionsItem
    final void saveAction() {
        if (isReportNameValid()) {
            final OutputFormat outputFormat = (OutputFormat) formatSpinner.getSelectedItem();
            String reportName = reportNameInput.getText() + "." + outputFormat;
            File reportDir = getAccountReportDir(reportName);

            if (reportDir == null) {
                Toast.makeText(getActivity(), R.string.sr_failed_to_create_local_repo, Toast.LENGTH_SHORT).show();
            } else {
                File reportFile = new File(reportDir, reportName);

                if (reportFile.exists() || getSharedReportDir(reportName).exists()) {
                    // show validation message
                    reportNameInput.setError(getString(R.string.sr_error_report_exists));
                } else {
                    String pageRange = calculatePages(mFromPage, mToPage);
                    ReportExecutionRequest executionData = createReportExecutionRequest(resource, outputFormat, pageRange);

                    RunReportExecutionRequest request =
                            new RunReportExecutionRequest(jsRestClient, executionData);
                    getSpiceManager().execute(request,
                            new RunReportExecutionListener(outputFormat, reportFile, pageRange));

                    ProgressDialogFragment.builder(getFragmentManager())
                            .setLoadingMessage(R.string.loading_msg)
                            .show();
                }
            }
        }
    }

    @AfterViews
    final void init() {
        reportNameInput.setText(resource.getLabel());

        // show spinner with available output formats
        ArrayAdapter<OutputFormat> arrayAdapter = new ArrayAdapter<OutputFormat>(getActivity(),
                android.R.layout.simple_spinner_item, OutputFormat.values());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(arrayAdapter);

        // hide save parts views if report have only 1 page
        if (pageCount > 1) {
            rangeControls.setVisibility(View.VISIBLE);

            mFromPage = 1;
            mToPage = pageCount;

            fromPageControl.setText(String.valueOf(mFromPage));
            toPageControl.setText(String.valueOf(mToPage));
        }
    }

    @Click(R.id.fromPageControl)
    void clickOnFromPage() {
        NumberDialogFragment.createBuilder(getFragmentManager())
                .setMinValue(1)
                .setCurrentValue(mFromPage)
                .setMaxValue(pageCount)
                .setRequestCode(FROM_PAGE_REQUEST_CODE)
                .setTargetFragment(this)
                .show();
    }

    @Click(R.id.toPageControl)
    void clickOnToPage() {
        NumberDialogFragment.createBuilder(getFragmentManager())
                .setMinValue(mFromPage)
                .setCurrentValue(mToPage)
                .setMaxValue(pageCount)
                .setRequestCode(TO_PAGE_REQUEST_CODE)
                .setTargetFragment(this)
                .show();
    }

    @ItemSelect(R.id.output_format_spinner)
    public void formatItemSelected(boolean selected, OutputFormat selectedItem) {
        reportNameInput.setError(null);
    }

    @TextChange(R.id.report_name_input)
    final void reportNameChanged() {
        boolean nameValid = isReportNameValid();
        reportNameInput.setError(null);
        if (saveAction != null) {
            saveAction.setIcon(nameValid ? R.drawable.ic_menu_save : R.drawable.ic_menu_save_disabled);
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private boolean isReportNameValid() {
        String reportName = reportNameInput.getText().toString();

        if (reportName.trim().isEmpty()) {
            reportNameInput.setError(getString(R.string.sr_error_field_is_empty));
            return false;
        }

        // reserved characters: * \ / " ' : ? | < > + [ ]
        if (FileUtils.nameContainsReservedChars(reportName)) {
            reportNameInput.setError(getString(R.string.sr_error_characters_not_allowed));
            return false;
        }

        return true;
    }

    @Nullable
    private File getAccountReportDir(String reportName) {
        File appFilesDir = getActivity().getExternalFilesDir(null);
        File savedReportsDir = new File(appFilesDir, JasperMobileApplication.SAVED_REPORTS_DIR_NAME);
        Account account = accountManager.getActiveAccount();
        if (account != null) {
            File accountReportDir = new File(savedReportsDir, account.name);
            File reportDir = new File(accountReportDir, reportName);

            if (!reportDir.exists() && !reportDir.mkdirs()) {
                Timber.e("Unable to create %s", savedReportsDir);
                return null;
            }

            return reportDir;
        } else {
            return null;
        }
    }

    private File getSharedReportDir(String reportName) {
        File appFilesDir = getActivity().getExternalFilesDir(null);
        File savedReportsDir = new File(appFilesDir, JasperMobileApplication.SAVED_REPORTS_DIR_NAME);
        File accountReportDir = new File(savedReportsDir, JasperMobileApplication.SHARED_DIR);
        return new File(accountReportDir, reportName);
    }

    private ReportExecutionRequest createReportExecutionRequest(ResourceLookup resource, SaveItemFragment.OutputFormat outputFormat,
                                                                String pageRange) {

        ReportExecutionRequest executionData = new ReportExecutionRequest();
        executionData.setReportUnitUri(resource.getUri());
        executionData.setInteractive(false);
        executionData.setOutputFormat(outputFormat.toString());
        executionData.setAsync(true);
        executionData.setEscapedAttachmentsPrefix("./");
        executionData.setPages(pageRange);

        List<ReportParameter> reportParameters = paramsStorage.getInputControlHolder(resource.getUri()).getReportParams();
        if (!reportParameters.isEmpty()) {
            executionData.setParameters(reportParameters);
        }

        return executionData;
    }

    private String calculatePages(int fromPage, int toPage) {
        boolean pagesNumbersIsValid = fromPage > 0 && toPage > 0 && toPage >= fromPage;
        if (pagesNumbersIsValid) {
            boolean isRange = fromPage < toPage;
            if (isRange) {
                return fromPage + "-" + toPage;
            } else {
                return String.valueOf(fromPage);
            }
        }
        return "";
    }

    //---------------------------------------------------------------------
    // Page Select Listeners
    //---------------------------------------------------------------------

    @Override
    public void onPageSelected(int page, int requestCode) {
        if (requestCode == FROM_PAGE_REQUEST_CODE) {
            boolean isPagePositive = (page > 1);
            boolean isRangeCorrect = (page <= mToPage);
            if (isPagePositive && isRangeCorrect) {
                boolean enableComponent = (page != pageCount);
                toPageControl.setEnabled(enableComponent);

                mFromPage = page;
                fromPageControl.setText(String.valueOf(mFromPage));
            }
        } else {
            boolean isRangeCorrect = (page >= mFromPage);
            if (isRangeCorrect) {
                mToPage = page;
                toPageControl.setText(String.valueOf(mToPage));
            }
        }
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class RunReportExecutionListener extends SimpleRequestListener<ReportExecutionResponse> {
        private OutputFormat outputFormat;
        private File reportFile;
        private String pageRange;

        public RunReportExecutionListener(OutputFormat outputFormat, File reportFile, String pageRange) {
            this.outputFormat = outputFormat;
            this.reportFile = reportFile;
            this.pageRange = pageRange;
        }

        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            super.onRequestFailure(exception);
            ProgressDialogFragment.dismiss(getFragmentManager());
        }

        @Override
        public void onRequestSuccess(ReportExecutionResponse response) {
            ProgressDialogFragment.dismiss(getFragmentManager());

            SaveReportService_.intent(getActivity()).saveReport(reportNameInput.getText().toString(), resource.getDescription(), outputFormat,
                    reportFile, pageRange, response.getRequestId()).start();
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.sdr_starting_downloading_msg), Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }
}