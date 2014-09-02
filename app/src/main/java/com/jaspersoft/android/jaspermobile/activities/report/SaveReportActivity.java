/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.report;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.BaseHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.ReportHtmlViewerActivity;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.async.request.RunReportExecutionRequest;
import com.jaspersoft.android.sdk.client.async.request.SaveExportAttachmentRequest;
import com.jaspersoft.android.sdk.client.async.request.SaveExportOutputRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportOutputResource;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.util.FileUtils;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.io.File;
import java.util.ArrayList;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

/**
 * @author Ivan Gadzhega
 * @since 1.8
 */
public class SaveReportActivity extends RoboActivity {

    // Action Bar IDs
    private static final int ID_AB_INDETERMINATE_PROGRESS = 10;

    @Inject
    private JsRestClient jsRestClient;

    @InjectView(R.id.output_format_spinner)
    private Spinner formatSpinner;
    @InjectView(R.id.report_name_input)
    private EditText reportNameInput;
    @InjectView(R.id.save_report_button)
    private Button saveReportButton;

    private SpiceManager serviceManager;
    private Menu optionsMenu;
    private int runningRequests;

    private enum OutputFormat {
        HTML,
        PDF,
        XLS
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_report_layout);

        serviceManager = new SpiceManager(JsXmlSpiceService.class);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.sr_ab_title);
        }

        String reportLabel = getIntent().getExtras().getString(BaseHtmlViewerActivity.EXTRA_RESOURCE_LABEL);
        reportNameInput.setText(reportLabel);

        // show spinner with available output formats
        ArrayAdapter<OutputFormat> arrayAdapter = new ArrayAdapter<OutputFormat>(this, android.R.layout.simple_spinner_item,
                OutputFormat.values());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(arrayAdapter);
        formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reportNameInput.setError(null);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    public void saveReportButtonClickHandler(View view) {
        if (isReportNameValid()) {
            OutputFormat outputFormat = (OutputFormat) formatSpinner.getSelectedItem();
            String reportName = reportNameInput.getText() + "." + outputFormat;
            File reportFile = new File(getReportDir(reportName), reportName);

            if (reportFile.exists()) {
                // show validation message
                reportNameInput.setError(getString(R.string.sr_error_report_exists));
            } else {
                // save report
                String reportUri = getIntent().getExtras().getString(BaseHtmlViewerActivity.EXTRA_RESOURCE_URI);
                ArrayList<ReportParameter> parameters =
                        getIntent().getExtras().getParcelableArrayList(ReportHtmlViewerActivity.EXTRA_REPORT_PARAMETERS);

                // run new report execution
                RunReportExecutionRequest request =
                        new RunReportExecutionRequest(jsRestClient, reportUri, outputFormat.toString(), parameters, false, "./");
                serviceManager.execute(request, new RunReportExecutionListener(reportFile, outputFormat));

                setRefreshActionButtonState(true);
            }
        }
    }

    @Override
    protected void onStart() {
        serviceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        serviceManager.shouldStop();
        super.onStop();
    }

    //---------------------------------------------------------------------
    // Options Menu
    //---------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        MenuItem item = menu.add(Menu.NONE, ID_AB_INDETERMINATE_PROGRESS, Menu.NONE, R.string.loading_msg);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        setRefreshActionButtonState(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setRefreshActionButtonState(boolean refreshing) {
        reportNameInput.setEnabled(!refreshing);
        formatSpinner.setEnabled(!refreshing);
        saveReportButton.setEnabled(!refreshing);

        if (optionsMenu != null) {
            MenuItem refreshItem = optionsMenu.findItem(ID_AB_INDETERMINATE_PROGRESS);
            if (refreshItem != null) {
                refreshItem.setVisible(refreshing);
                if (refreshing) {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
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
        File appFilesDir = getExternalFilesDir(null);
        File savedReportsDir = new File(appFilesDir, JasperMobileApplication.SAVED_REPORTS_DIR_NAME);
        File reportDir = new File(savedReportsDir, reportName);

        if (!reportDir.exists() && !reportDir.mkdirs()){
            Ln.e("Unable to create %s", savedReportsDir);
        }

        return reportDir;
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
            RequestExceptionHandler.handle(exception, SaveReportActivity.this, false);
            setRefreshActionButtonState(false);
        }

        @Override
        public void onRequestSuccess(ReportExecutionResponse response) {
            ExportExecution execution = response.getExports().get(0);
            String exportOutput = execution.getId();
            String executionId = response.getRequestId();

            // save report file
            SaveExportOutputRequest outputRequest = new SaveExportOutputRequest(jsRestClient, executionId, exportOutput, reportFile);
            serviceManager.execute(outputRequest, new SaveFileListener());

            // save attachments
            if (OutputFormat.HTML == outputFormat) {
                for (ReportOutputResource attachment : execution.getAttachments()) {
                    String attachmentName = attachment.getFileName();
                    File attachmentFile = new File(reportFile.getParentFile(), attachmentName);

                    SaveExportAttachmentRequest attachmentRequest = new SaveExportAttachmentRequest(jsRestClient,
                            executionId, exportOutput, attachmentName, attachmentFile);
                    serviceManager.execute(attachmentRequest, new SaveFileListener());
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
            RequestExceptionHandler.handle(exception, SaveReportActivity.this, false);
            setRefreshActionButtonState(false);
        }

        @Override
        public void onRequestSuccess(File outputFile) {
            runningRequests--;

            if (runningRequests == 0) {
                // activity is done and should be closed
                Toast.makeText(SaveReportActivity.this, R.string.sr_t_report_saved, Toast.LENGTH_SHORT).show();
                setRefreshActionButtonState(false);
                finish();
            }
        }

    }

}
