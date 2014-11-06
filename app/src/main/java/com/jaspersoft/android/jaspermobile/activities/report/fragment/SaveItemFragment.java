/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.report.fragment;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.RunReportExecutionRequest;
import com.jaspersoft.android.sdk.client.async.request.SaveExportAttachmentRequest;
import com.jaspersoft.android.sdk.client.async.request.SaveExportOutputRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportOutputResource;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.util.FileUtils;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;

import roboguice.util.Ln;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.save_report_layout)
@OptionsMenu(R.menu.save_item_menu)
public class SaveItemFragment extends RoboSpiceFragment {

    public static final String TAG = SaveItemFragment.class.getSimpleName();

    @ViewById(R.id.output_format_spinner)
    Spinner formatSpinner;
    @ViewById(R.id.report_name_input)
    EditText reportNameInput;

    @FragmentArg
    String resourceLabel;
    @FragmentArg
    String resourceUri;
    @FragmentArg
    ArrayList<ReportParameter> reportParameters;

    @OptionsMenuItem
    MenuItem saveAction;

    @Inject
    JsRestClient jsRestClient;

    @InstanceState
    int runningRequests;

    protected static enum OutputFormat {
        HTML,
        PDF,
        XLS
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hasOptionsMenu();

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.sr_ab_title);
        }
    }

    @OptionsItem
    final void saveAction() {
        if (isReportNameValid()) {
            OutputFormat outputFormat = (OutputFormat) formatSpinner.getSelectedItem();
            String reportName = reportNameInput.getText() + "." + outputFormat;
            File reportFile = new File(getReportDir(reportName), reportName);

            if (reportFile.exists()) {
                // show validation message
                reportNameInput.setError(getString(R.string.sr_error_report_exists));
            } else {
                // save report
                // run new report execution
                setRefreshActionButtonState(true);

                ReportExecutionRequest executionRequest = new ReportExecutionRequest();
                executionRequest.setReportUnitUri(resourceUri);
                executionRequest.setInteractive(false);
                executionRequest.setOutputFormat(outputFormat.toString());
                if (reportParameters != null && !reportParameters.isEmpty()) {
                    executionRequest.setParameters(reportParameters);
                }
                RunReportExecutionRequest request =
                        new RunReportExecutionRequest(jsRestClient, executionRequest);
                getSpiceManager().execute(request, new RunReportExecutionListener(reportFile, outputFormat));
            }
        }
    }

    @AfterViews
    final void init() {
        reportNameInput.setText(resourceLabel);

        // show spinner with available output formats
        ArrayAdapter<OutputFormat> arrayAdapter = new ArrayAdapter<OutputFormat>(getActivity(),
                android.R.layout.simple_spinner_item, OutputFormat.values());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(arrayAdapter);
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
            saveAction.setIcon(nameValid ? R.drawable.ic_action_submit : R.drawable.ic_action_submit_disabled);
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private boolean isReportNameValid() {
        String reportName = reportNameInput.getText().toString();

        if (reportName.isEmpty()) {
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

    private File getReportDir(String reportName) {
        File appFilesDir = getActivity().getExternalFilesDir(null);
        File savedReportsDir = new File(appFilesDir, JasperMobileApplication.SAVED_REPORTS_DIR_NAME);
        File reportDir = new File(savedReportsDir, reportName);

        if (!reportDir.exists() && !reportDir.mkdirs()){
            Ln.e("Unable to create %s", savedReportsDir);
        }

        return reportDir;
    }

    private void setRefreshActionButtonState(boolean refreshing) {
        if (refreshing) {
            saveAction.setActionView(R.layout.actionbar_indeterminate_progress);
        } else {
            saveAction.setActionView(null);
        }
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class RunReportExecutionListener implements RequestListener<ReportExecutionResponse> {

        private File reportFile;
        private OutputFormat outputFormat;

        private RunReportExecutionListener(File reportFile, OutputFormat outputFormat) {
            this.reportFile = reportFile;
            this.outputFormat = outputFormat;
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, getActivity(), false);
            setRefreshActionButtonState(false);
        }

        @Override
        public void onRequestSuccess(ReportExecutionResponse response) {

            ExportExecution execution = response.getExports().get(0);
            String exportOutput = execution.getId();
            String executionId = response.getRequestId();

            // save report file
            SaveExportOutputRequest outputRequest = new SaveExportOutputRequest(jsRestClient,
                    executionId, exportOutput, reportFile);
            getSpiceManager().execute(outputRequest, new SaveFileListener());

            // save attachments
            if (OutputFormat.HTML == outputFormat) {
                for (ReportOutputResource attachment : execution.getAttachments()) {
                    String attachmentName = attachment.getFileName();
                    File attachmentFile = new File(reportFile.getParentFile(), attachmentName);

                    SaveExportAttachmentRequest attachmentRequest = new SaveExportAttachmentRequest(jsRestClient,
                            executionId, exportOutput, attachmentName, attachmentFile);
                    getSpiceManager().execute(attachmentRequest, new SaveFileListener());
                }
            }
        }

    }

    private class SaveFileListener implements RequestListener<File> {

        private SaveFileListener() {
            runningRequests++;
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, getActivity(), false);
            setRefreshActionButtonState(false);
        }

        @Override
        public void onRequestSuccess(File outputFile) {
            runningRequests--;

            if (runningRequests == 0) {
                // activity is done and should be closed
                setRefreshActionButtonState(false);
                Toast.makeText(getActivity(), R.string.sr_t_report_saved, Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }

    }

}
