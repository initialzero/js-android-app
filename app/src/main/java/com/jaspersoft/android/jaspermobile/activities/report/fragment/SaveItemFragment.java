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
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.info.ServerInfoManager;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.RunReportExportsRequest;
import com.jaspersoft.android.sdk.client.async.request.SaveExportAttachmentRequest;
import com.jaspersoft.android.sdk.client.async.request.SaveExportOutputRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ExportsRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportOutputResource;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.util.FileUtils;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    String requestId;
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
    @Bean
    ServerInfoManager infoManager;

    @InstanceState
    int runningRequests;

    private List<SpiceRequest<?>> requests = Lists.newArrayList();
    private File reportFile;

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
            final OutputFormat outputFormat = (OutputFormat) formatSpinner.getSelectedItem();
            String reportName = reportNameInput.getText() + "." + outputFormat;
            reportFile = new File(getReportDir(reportName), reportName);

            if (reportFile.exists()) {
                // show validation message
                reportNameInput.setError(getString(R.string.sr_error_report_exists));
            } else {
                // save report
                // run new report execution
                setRefreshActionButtonState(true);

                final ExportsRequest executionData = new ExportsRequest();
                executionData.setReportUnitUri(resourceUri);
                executionData.setInteractive(false);
                executionData.setOutputFormat(outputFormat.toString());
                executionData.setEscapedAttachmentsPrefix("./");
                if (reportParameters != null && !reportParameters.isEmpty()) {
                    executionData.setParameters(reportParameters);
                }

                final RunReportExportsRequest request = new RunReportExportsRequest(jsRestClient,
                        executionData, requestId);
                getSpiceManager().execute(request,
                        new RunReportExecutionListener(outputFormat));
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

    private File getReportDir(String reportName) {
        File appFilesDir = getActivity().getExternalFilesDir(null);
        File savedReportsDir = new File(appFilesDir, JasperMobileApplication.SAVED_REPORTS_DIR_NAME);
        File reportDir = new File(savedReportsDir, reportName);

        if (!reportDir.exists() && !reportDir.mkdirs()) {
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

    private void removeTemplate() {
        if (reportFile == null) return;

        File dir = reportFile.getParentFile();
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            Log.w(TAG, "Failed to remove template file", e);
        }
        Toast.makeText(getActivity(), "Failed to execute report", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (runningRequests > 0) {
            removeTemplate();
        }
    }
    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class RunReportExecutionListener implements RequestListener<ExportExecution> {
        private OutputFormat outputFormat;

        private RunReportExecutionListener(OutputFormat outputFormat) {
            runningRequests++;
            this.outputFormat = outputFormat;
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, getActivity(), false);
            setRefreshActionButtonState(false);
            runningRequests--;
            removeTemplate();
        }

        @Override
        public void onRequestSuccess(ExportExecution execution) {
            String exportOutput = execution.getId();

            // save report file
            SaveExportOutputRequest outputRequest = new SaveExportOutputRequest(jsRestClient,
                    requestId, exportOutput, reportFile);
            getSpiceManager().execute(outputRequest, new SaveFileListener());

            // save attachments
            if (OutputFormat.HTML == outputFormat) {
                for (ReportOutputResource attachment : execution.getAttachments()) {
                    String attachmentName = attachment.getFileName();
                    File attachmentFile = new File(reportFile.getParentFile(), attachmentName);

                    SaveExportAttachmentRequest attachmentRequest = new SaveExportAttachmentRequest(jsRestClient,
                            requestId, exportOutput, attachmentName, attachmentFile);
                    requests.add(attachmentRequest);
                    getSpiceManager().execute(attachmentRequest, new SaveFileListener());
                }
            }
            runningRequests--;
        }

    }

    private class SaveFileListener implements RequestListener<File> {
        private SaveFileListener() {
            runningRequests++;
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, getActivity(), false);
            for (SpiceRequest<?> request : requests) {
                runningRequests--;
                request.cancel();
            }
            removeTemplate();
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
