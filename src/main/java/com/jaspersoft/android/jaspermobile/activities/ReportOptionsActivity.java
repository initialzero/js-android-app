/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
 * http://www.jasperforge.org/projects/androidmobile
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

package com.jaspersoft.android.jaspermobile.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.inject.Inject;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsAsyncTaskManager;
import com.jaspersoft.android.sdk.client.async.JsOnTaskCallbackListener;
import com.jaspersoft.android.sdk.client.async.task.*;
import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.*;
import com.jaspersoft.android.sdk.ui.widget.MultiSelectSpinner;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.activities.async.AsyncTaskExceptionHandler;
import com.jaspersoft.android.jaspermobile.util.CacheUtils;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

import java.io.File;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public class ReportOptionsActivity extends RoboActivity implements JsOnTaskCallbackListener {

    // Extras
    public static final String EXTRA_REPORT_LABEL = "ReportOptionsActivity.EXTRA_REPORT_LABEL";
    public static final String EXTRA_REPORT_URI = "ReportOptionsActivity.EXTRA_REPORT_URI";
    // Supported report output formats
    public static final String RUN_OUTPUT_FORMAT_HTML = "HTML";
    public static final String RUN_OUTPUT_FORMAT_PDF = "PDF";
    public static final String RUN_OUTPUT_FORMAT_XLS = "XLS";
    
    // Attachment name that's the report itself
    private static final String REPORT_FILE_NAME = "report";
    // Dialog IDs
    private static final int DATE_DIALOG_ID = 0;
    private static final int TIME_DIALOG_ID = 1;
    // Async Task IDs
    private static final int GET_RESOURCE_DESCRIPTOR_TASK = 1;
    private static final int GET_REPORT_DESCRIPTOR_TASK = 2;
    private static final int SAVE_ATTACHMENTS_FOR_HTML_TASK = 3;
    private static final int SAVE_ATTACHMENT_FOR_OTHER_FORMATS_TASK = 4;

    private TextView activeDateDisplay;
    private Calendar activeDate;
    private InputControlWrapper activeInputControl;

    private ResourceDescriptor resourceDescriptor;
    private List<InputControlWrapper> inputControls = new ArrayList<InputControlWrapper>();

    @InjectView(R.id.breadcrumbs_title_small)   private TextView breadCrumbsTitleSmall;
    @InjectView(R.id.breadcrumbs_title_large)   private TextView breadCrumbsTitleLarge;
    @InjectView(R.id.report_format_spinner)     private Spinner formatSpinner;

    @Inject private JsRestClient jsRestClient;

    private JsAsyncTaskManager jsAsyncTaskManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // workaround for http://bugzilla.jaspersoft.com/show_bug.cgi?id=27735 (only for api 9+)
        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.GINGERBREAD){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
            StrictMode.setThreadPolicy(policy);
        }

        setContentView(R.layout.report_options_layout);

        // external storage should be writable when using output formats other than HTML
        String[] outputFormats;
        if (isExternalStorageWritable()) {
            outputFormats = new String[] { RUN_OUTPUT_FORMAT_HTML, RUN_OUTPUT_FORMAT_PDF, RUN_OUTPUT_FORMAT_XLS };
        } else {
            outputFormats = new String[] { RUN_OUTPUT_FORMAT_HTML } ;
        }

        // show spinner with available output formats
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, outputFormats);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(arrayAdapter);

        // Create manager and set this activity as context and listener
        jsAsyncTaskManager = new JsAsyncTaskManager(this, this);
        // Handle tasks that can be retained before
        jsAsyncTaskManager.handleRetainedTasks((List<JsAsyncTask>) getLastNonConfigurationInstance());

        // get report label from extras and update bread crumbs
        String reportLabel = getIntent().getExtras().getString(EXTRA_REPORT_LABEL);
        breadCrumbsTitleLarge.setText(reportLabel);

        // get report uri from extras
        String reportUri = getIntent().getExtras().getString(EXTRA_REPORT_URI);
        GetResourceAsyncTask getResourceAsyncTask = new GetResourceAsyncTask(GET_RESOURCE_DESCRIPTOR_TASK,
                getString(R.string.ro_pd_loading_report_info_msg), 0, jsRestClient, reportUri);
        jsAsyncTaskManager.executeTask(getResourceAsyncTask);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        // Delegate tasks retain to manager
        return jsAsyncTaskManager.retainTasks();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, dateSetListener, activeDate.get(Calendar.YEAR), activeDate.get(Calendar.MONTH), activeDate.get(Calendar.DAY_OF_MONTH));
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this, timeSetListener, activeDate.get(Calendar.HOUR_OF_DAY), activeDate.get(Calendar.MINUTE), true);
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(activeDate.get(Calendar.YEAR), activeDate.get(Calendar.MONTH), activeDate.get(Calendar.DAY_OF_MONTH));
                break;
            case TIME_DIALOG_ID:
                ((TimePickerDialog) dialog).updateTime(activeDate.get(Calendar.HOUR_OF_DAY), activeDate.get(Calendar.MINUTE));
                break;
        }
    }

    public void runReportButtonClickHandler(View view) {
        // generate report output according to selected format
        String outputFormat = formatSpinner.getSelectedItem().toString();

        List<ResourceParameter> parameters = new ArrayList<ResourceParameter>();
        boolean hasErrors = false;
        for (InputControlWrapper inputControl : inputControls) {
            // validation
            if (inputControl.isMandatory() && inputControl.getListOfSelectedValues().isEmpty()) {
                TextView textView = (TextView) inputControl.getErrorView();
                textView.setText(getString(R.string.ro_error_field_is_mandatory));
                textView.setVisibility(View.VISIBLE);
                hasErrors = true;
            } else {
                parameters.addAll(inputControl.getListOfSelectedValues());
            }
        }
        
        if (hasErrors) return;

        if (RUN_OUTPUT_FORMAT_HTML.equalsIgnoreCase(outputFormat) && jsRestClient.getRestApiDescriptor().getVersion() > 1 ) {
            // REST v2
            String reportUrl = jsRestClient.generateReportUrl(resourceDescriptor, parameters);
            // run the html report viewer
            Intent htmlViewer = new Intent();
            htmlViewer.setClass(this, ReportHtmlViewerActivity.class);
            htmlViewer.putExtra(ReportHtmlViewerActivity.EXTRA_REPORT_FILE_URI, reportUrl);
            startActivity(htmlViewer);
        } else {
            // REST v1
            resourceDescriptor.setParameters(parameters);
            GetReportAsyncTask getReportAsyncTask =  new GetReportAsyncTask(GET_REPORT_DESCRIPTOR_TASK,
                    getString(R.string.ro_pd_running_report_msg), 0, jsRestClient, resourceDescriptor, outputFormat);
            jsAsyncTaskManager.executeTask(getReportAsyncTask);
        }
    }

    public void actionButtonOnClickListener(View view) {
        switch (view.getId()) {
            case R.id.app_icon_button:
                HomeActivity.goHome(this);
        }
    }

    public void onTaskComplete(JsAsyncTask task) {
        switch (task.getId()) {
            case GET_RESOURCE_DESCRIPTOR_TASK:
                if (task.isCancelled()) {
                    Toast.makeText(this, R.string.cancelled_msg, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        resourceDescriptor = ((GetResourceAsyncTask)task).get();
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    } catch (ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }

                    LinearLayout baseLayout =  (LinearLayout) findViewById(R.id.input_controls_layout);
                    LayoutInflater inflater = getLayoutInflater();

                    // Get input controls from report resource descriptor
                    for(ResourceDescriptor internalResource : resourceDescriptor.getInternalResources()) {
                        if(internalResource.getWsType() == ResourceDescriptor.WsType.inputControl) {
                            inputControls.add(new InputControlWrapper(internalResource));
                        }
                    }

                    // Update the dependencies for the input controls
                    for (InputControlWrapper i : inputControls) {
                        List<String> parameters = i.getParameterDependencies();
                        if (!parameters.isEmpty()) {
                            for (String parameter : parameters) {
                                for (InputControlWrapper j : inputControls) {
                                    if (j != i && j.getName().equals(parameter)) {
                                        j.getSlaveDependencies().add(i);
                                        i.getMasterDependencies().add(j);
                                    }
                                }
                            }
                        }
                    }
                    // remove controls with transitive dependencies to avoid duplicate requests
                    cleanupDependencies(inputControls);

                    // init UI components for ICs
                    for(final InputControlWrapper inputControl : inputControls) {
                        String mandatoryPrefix = (inputControl.isMandatory()) ? "* " : "" ;
                        switch (inputControl.getType()) {
                            case ResourceDescriptor.IC_TYPE_BOOLEAN: {
                                // inflate view
                                View view = inflater.inflate(R.layout.ic_boolean_layout, baseLayout, false);
                                CheckBox checkBox = (CheckBox) view.findViewById(R.id.ic_checkbox);
                                checkBox.setText(inputControl.getLabel());
                                // set default value
                                List<ResourceParameter> params = new ArrayList<ResourceParameter>();
                                params.add(new ResourceParameter(inputControl.getName(), false, false));
                                inputControl.setListOfSelectedValues(params);
                                //update dependent controls if exist
                                updateDependentControls(inputControl);
                                //listener
                                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        // update selected value
                                        Boolean value = ((CheckBox) inputControl.getInputView()).isChecked();
                                        List<ResourceParameter> params = new ArrayList<ResourceParameter>();
                                        params.add(new ResourceParameter(inputControl.getName(), value, false));
                                        inputControl.setListOfSelectedValues(params);
                                        //update dependent controls if exist
                                        updateDependentControls(inputControl);
                                    }
                                });
                                // assign view to the control
                                inputControl.setInputView(checkBox);
                                // show the control
                                baseLayout.addView(view);
                                break;
                            }

                            case ResourceDescriptor.IC_TYPE_SINGLE_VALUE: {
                                switch (inputControl.getDataType()) {
                                    case ResourceDescriptor.DT_TYPE_TEXT:
                                    case ResourceDescriptor.DT_TYPE_NUMBER: {
                                        // inflate view
                                        View view = inflater.inflate(R.layout.ic_single_value_layout, baseLayout, false);
                                        TextView textView = (TextView) view.findViewById(R.id.ic_text_label);
                                        textView.setText(mandatoryPrefix + inputControl.getLabel() + ":");
                                        EditText editText = (EditText) view.findViewById(R.id.ic_edit_text);
                                        // allow only numbers if data type is numeric
                                        if (inputControl.getDataType() == ResourceDescriptor.DT_TYPE_NUMBER) {
                                            editText.setInputType(InputType.TYPE_CLASS_NUMBER
                                                    | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                        }
                                        // add listener
                                        editText.addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void afterTextChanged(Editable s) {
                                                // update selected value
                                                List<ResourceParameter> parameters = new ArrayList<ResourceParameter>();
                                                parameters.add(new ResourceParameter(inputControl.getName(), s.toString(), false));
                                                inputControl.setListOfSelectedValues(parameters);
                                                //update dependent controls if exist
                                                updateDependentControls(inputControl);
                                            }
                                            @Override
                                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                                            @Override
                                            public void onTextChanged(CharSequence s, int start, int before, int count) {}
                                        });

                                        TextView errorTextView = (TextView) view.findViewById(R.id.ic_error_text);

                                        // assign views to the control
                                        inputControl.setInputView(editText);
                                        inputControl.setErrorView(errorTextView);
                                        // show the control
                                        baseLayout.addView(view);
                                        break;
                                    }
                                    case ResourceDescriptor.DT_TYPE_DATE:
                                    case ResourceDescriptor.DT_TYPE_DATE_TIME:
                                        // inflate view
                                        View view = inflater.inflate(R.layout.ic_single_value_date_layout, baseLayout, false);
                                        TextView textView = (TextView) view.findViewById(R.id.ic_text_label);
                                        textView.setText(mandatoryPrefix + inputControl.getLabel() + ":");
                                        final EditText editText = (EditText) view.findViewById(R.id.ic_date_text);

                                        boolean isDateTime = (inputControl.getDataType() == ResourceDescriptor.DT_TYPE_DATE_TIME);

                                        // get the current date
                                        final Calendar startDate = Calendar.getInstance();

                                        // init the date picker
                                        ImageButton datePicker = (ImageButton) view.findViewById(R.id.ic_date_picker_button);
                                        // add a click listener
                                        datePicker.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                showDateDialog(inputControl ,editText, startDate);
                                            }
                                        });

                                        if (isDateTime) {
                                            // init the time picker
                                            ImageButton timePicker = (ImageButton) view.findViewById(R.id.ic_time_picker_button);
                                            timePicker.setVisibility(View.VISIBLE);
                                            // add a click listener
                                            timePicker.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    showTimeDialog(inputControl ,editText, startDate);
                                                }
                                            });
                                        }

                                        TextView errorTextView = (TextView) view.findViewById(R.id.ic_error_text);

                                        // assign views to the control
                                        inputControl.setInputView(editText);
                                        inputControl.setErrorView(errorTextView);
                                        // show the control
                                        baseLayout.addView(view);
                                        break;
                                }
                                break;
                            }

                            case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_LIST_OF_VALUES:
                            case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_LIST_OF_VALUES_RADIO:
                            case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_QUERY:
                            case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_QUERY_RADIO: {
                                // inflate view
                                View view = inflater.inflate(R.layout.ic_single_select_layout, baseLayout, false);
                                TextView textView = (TextView) view.findViewById(R.id.ic_text_label);
                                textView.setText(mandatoryPrefix + inputControl.getLabel() + ":");
                                Spinner spinner = (Spinner) view.findViewById(R.id.ic_spinner);
                                spinner.setPrompt(inputControl.getLabel());
                                // listener
                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        // // update selected value and update dependent controls if exist
                                        updateSingleSelectControl(inputControl, (ResourceProperty) parent.getSelectedItem());
                                    }
                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) { /* Do nothing */ }
                                });

                                TextView errorTextView = (TextView) view.findViewById(R.id.ic_error_text);

                                // assign views to the control
                                inputControl.setInputView(spinner);
                                inputControl.setErrorView(errorTextView);
                                // show the control
                                baseLayout.addView(view);
                                break;
                            }

                            case ResourceDescriptor.IC_TYPE_MULTI_SELECT_LIST_OF_VALUES:
                            case ResourceDescriptor.IC_TYPE_MULTI_SELECT_LIST_OF_VALUES_CHECKBOX:
                            case ResourceDescriptor.IC_TYPE_MULTI_SELECT_QUERY:
                            case ResourceDescriptor.IC_TYPE_MULTI_SELECT_QUERY_CHECKBOX: {
                                // inflate view
                                View view = inflater.inflate(R.layout.ic_multi_select_layout, baseLayout, false);
                                TextView textView = (TextView) view.findViewById(R.id.ic_text_label);
                                textView.setText(mandatoryPrefix + inputControl.getLabel() + ":");
                                MultiSelectSpinner multiSpinner = (MultiSelectSpinner) view.findViewById(R.id.ic_multi_spinner);
                                multiSpinner.setPrompt(inputControl.getLabel());
                                // listener
                                multiSpinner.setOnItemsSelectedListener(
                                        new MultiSelectSpinner.OnItemsSelectedListener() {
                                            @Override
                                            public void onItemsSelected(List selectedItems) {
                                                // update selected values
                                                List<ResourceParameter> params = new ArrayList<ResourceParameter>();
                                                for (ResourceProperty item : (List<ResourceProperty>) selectedItems) {
                                                    params.add(new ResourceParameter(inputControl.getName(), item.getName(), true));
                                                }
                                                inputControl.setListOfSelectedValues(params);
                                                // update dependent controls if exist
                                                updateDependentControls(inputControl);
                                            }
                                        });
                                TextView errorTextView = (TextView) view.findViewById(R.id.ic_error_text);

                                // assign views to the control
                                inputControl.setInputView(multiSpinner);
                                inputControl.setErrorView(errorTextView);
                                // show the control
                                baseLayout.addView(view);
                                break;
                            }
                        }
                    }

                    // get list of controls that aren't dependent
                    List<InputControlWrapper> rootControls = new ArrayList<InputControlWrapper>(inputControls);
                    for (InputControlWrapper ic : inputControls) {
                        rootControls.removeAll(getAllSubDependentControls(ic));
                    }

                    // init values for ICs
                    for(final InputControlWrapper inputControl : rootControls) {
                        switch (inputControl.getType()) {
                            case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_LIST_OF_VALUES:
                            case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_LIST_OF_VALUES_RADIO: {
                                Spinner spinner = (Spinner) inputControl.getInputView();
                                // set initial values for spinner
                                if (!inputControl.isMandatory()) {
                                    addBlankPropertyToList(inputControl.getListOfValues());
                                }
                                ArrayAdapter<ResourceProperty> lovAdapter =
                                        new ArrayAdapter<ResourceProperty>(this, android.R.layout.simple_spinner_item, inputControl.getListOfValues());
                                lovAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(lovAdapter);

                                // set selected value and update dependent controls if exist
                                updateSingleSelectControl(inputControl, (ResourceProperty) spinner.getSelectedItem());
                                break;
                            }

                            case ResourceDescriptor.IC_TYPE_MULTI_SELECT_LIST_OF_VALUES:
                            case ResourceDescriptor.IC_TYPE_MULTI_SELECT_LIST_OF_VALUES_CHECKBOX: {
                                MultiSelectSpinner multiSpinner = (MultiSelectSpinner) inputControl.getInputView();
                                // set initial values for multispinner
                                multiSpinner.setItemsList(inputControl.getListOfValues(), InputControlWrapper.NOTHING_SUBSTITUTE_LABEL);

                                // set selected values
                                List<ResourceParameter> params = new ArrayList<ResourceParameter>();
                                for (ResourceProperty item : (List<ResourceProperty>) multiSpinner.getSelectedItems()) {
                                    params.add(new ResourceParameter(inputControl.getName(), item.getName(), true));
                                }
                                inputControl.setListOfSelectedValues(params);

                                //update dependent controls if exist
                                updateDependentControls(inputControl);

                                break;
                            }

                            case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_QUERY:
                            case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_QUERY_RADIO: {
                                updateSingleSelectQueryControl(inputControl);
                                break;
                            }
                            case ResourceDescriptor.IC_TYPE_MULTI_SELECT_QUERY:
                            case ResourceDescriptor.IC_TYPE_MULTI_SELECT_QUERY_CHECKBOX: {
                                updateMultiSelectQueryControl(inputControl);
                                break;
                            }
                        }
                    }
                }
                break;
            case GET_REPORT_DESCRIPTOR_TASK:
                if (task.isCancelled()) {
                    Toast.makeText(this, R.string.cancelled_msg, Toast.LENGTH_SHORT).show();
                } else {
                    ReportDescriptor reportDescriptor;
                    try {
                        reportDescriptor = ((GetReportAsyncTask)task).get();
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    } catch (ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }

                    String outputFormat = formatSpinner.getSelectedItem().toString();
                    String uuid = reportDescriptor.getUuid();

                    File outputDir = getReportOutputCacheDir();

                    // view report using internal viewer for HTML or external viewers for all other formats
                    if (outputFormat.equalsIgnoreCase(RUN_OUTPUT_FORMAT_HTML)) {
                        // get report attachments and save them to cache folder
                        SaveReportAttachmentsAsyncTask saveAttachmentsTask = new SaveReportAttachmentsAsyncTask(SAVE_ATTACHMENTS_FOR_HTML_TASK,
                                getString(R.string.ro_pd_downloading_report_msg), 0, jsRestClient, uuid, reportDescriptor.getAttachments(), outputDir);
                        jsAsyncTaskManager.executeTask(saveAttachmentsTask);
                    } else {
                        // workaround: manually define content type and file extension depending on selected format
                        String contentType, extension;
                        if (outputFormat.equalsIgnoreCase(RUN_OUTPUT_FORMAT_PDF)) {
                            contentType = "application/pdf";
                            extension = ".pdf";
                        } else {
                            contentType = "application/xls";
                            extension = ".xls";
                        }

                        // get the report output file and save it to cache folder
                        File outputFile = new File(outputDir, resourceDescriptor.getName() + extension);
                        SaveReportAttachmentAsyncTask saveAttachmentTask = new SaveReportAttachmentAsyncTask(SAVE_ATTACHMENT_FOR_OTHER_FORMATS_TASK,
                                getString(R.string.ro_pd_downloading_report_msg), 0, jsRestClient, uuid, REPORT_FILE_NAME, outputFile);
                        saveAttachmentTask.setContentType(contentType);
                        jsAsyncTaskManager.executeTask(saveAttachmentTask);
                    }
                }
                break;
            case SAVE_ATTACHMENTS_FOR_HTML_TASK:
                if (task.isCancelled()) {
                    Toast.makeText(this, R.string.cancelled_msg, Toast.LENGTH_SHORT).show();
                } else {
                    // run the html report viewer
                    Intent htmlViewer = new Intent();
                    htmlViewer.setClass(this, ReportHtmlViewerActivity.class);
                    File outputDir = ((SaveReportAttachmentsAsyncTask) task).getOutputDir();
                    htmlViewer.putExtra(ReportHtmlViewerActivity.EXTRA_REPORT_FILE_URI, Uri.fromFile(outputDir) + File.separator + REPORT_FILE_NAME);
                    startActivity(htmlViewer);
                }
                break;
            case SAVE_ATTACHMENT_FOR_OTHER_FORMATS_TASK:
                if (task.isCancelled()) {
                    Toast.makeText(this, R.string.cancelled_msg, Toast.LENGTH_SHORT).show();
                } else {
                    SaveReportAttachmentAsyncTask saveAttachmentTask = (SaveReportAttachmentAsyncTask) task;
                    File outputFile = saveAttachmentTask.getOutputFile();
                    String contentType = saveAttachmentTask.getContentType();
                    if (outputFile.exists()) {
                        // run external viewer according to selected output format
                        Uri path = Uri.fromFile(outputFile);
                        Intent externalViewer = new Intent(Intent.ACTION_VIEW);
                        externalViewer.setDataAndType(path, contentType);
                        externalViewer.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        try {
                            startActivity(externalViewer);
                        }
                        catch (ActivityNotFoundException e) {
                            // show notification if no app available to open selected format
                            Toast.makeText(this, getString(R.string.ro_no_app_available_toast, formatSpinner.getSelectedItem().toString()), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    public void onTaskException(JsAsyncTask task) {
        AsyncTaskExceptionHandler.handle(task, this, true);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    /**
     * Helper Method to Test if external Storage is Available
     * @return <code>true</code> if storage is writable, <code>false</code> otherwise
     */
    private boolean isExternalStorageWritable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    /**
     * Helper Method to Define the report output cache dir
     * @return directory file
     */
    private File getReportOutputCacheDir() {
        File cacheDir = (isExternalStorageWritable()) ? CacheUtils.getExternalCacheDir(this) : getCacheDir() ;
        File outputDir = new File(cacheDir, JasperMobileApplication.REPORT_OUTPUT_DIR_NAME);

        if (!outputDir.exists() && !outputDir.mkdirs()){
            Ln.e("Unable to create %s", outputDir);
        }

        return outputDir;
    }
    
    private void updateDependentControls(InputControlWrapper inputControl) {
        for (InputControlWrapper dependentControl : inputControl.getSlaveDependencies()) {
            // update view
            switch (dependentControl.getType()) {
                case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_QUERY:
                case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_QUERY_RADIO: {
                    updateSingleSelectQueryControl(dependentControl);
                    break;
                }
                case ResourceDescriptor.IC_TYPE_MULTI_SELECT_QUERY:
                case ResourceDescriptor.IC_TYPE_MULTI_SELECT_QUERY_CHECKBOX:
                    updateMultiSelectQueryControl(dependentControl);
                    break;
            }

        }
    }

    private void updateInputControlQueryData(InputControlWrapper inputControl) {
        // get the parameter values
        List<ResourceParameter> parameters = new ArrayList<ResourceParameter>();
        
        for (InputControlWrapper dependency : inputControl.getMasterDependencies()) {
            parameters.addAll(dependency.getListOfSelectedValues());
        }
        
        String datasourceUri = inputControl.getDataSourceUri();
        // get data source from report if it isn't available for input control
        if (datasourceUri == null) {
            datasourceUri = resourceDescriptor.getDataSourceUri();
        }

        // update the query data for this input control
        List<ResourceProperty> values = jsRestClient.getInputControlQueryData(inputControl.getUri(), datasourceUri, parameters);

        // workaround for empty values in cascade
        if (values.isEmpty()) {
            inputControl.getInputView().setEnabled(false);
        } else {
            inputControl.getInputView().setEnabled(true);
        }

        inputControl.setListOfValues(values);
    }

    private void updateSingleSelectControl(InputControlWrapper inputControl, ResourceProperty selectedItem) {
        List<ResourceParameter> params = new ArrayList<ResourceParameter>();
        if (selectedItem != null && !(InputControlWrapper.NOTHING_SUBSTITUTE).equals(selectedItem.getName())) {
            params.add(new ResourceParameter(inputControl.getName(), selectedItem.getName(), false));
        }
        inputControl.setListOfSelectedValues(params);

        //update dependent controls if exist
        updateDependentControls(inputControl);
    }

    private void updateSingleSelectQueryControl(InputControlWrapper inputControl) {
        // get View from input control
        Spinner spinner = (Spinner) inputControl.getInputView();
        // update list of values from query
        updateInputControlQueryData(inputControl);
        // update list of values for spinner
        if (!inputControl.isMandatory()) {
            addBlankPropertyToList(inputControl.getListOfValues());
        }
        ArrayAdapter<ResourceProperty> lovAdapter =
                new ArrayAdapter<ResourceProperty>(this, android.R.layout.simple_spinner_item, inputControl.getListOfValues());
        lovAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(lovAdapter);

        // set selected value and update dependent controls if exist
        updateSingleSelectControl(inputControl, (ResourceProperty) spinner.getSelectedItem());
    }

    private void updateMultiSelectQueryControl(InputControlWrapper inputControl) {
        // get View from input control
        MultiSelectSpinner multiSpinner = (MultiSelectSpinner) inputControl.getInputView();
        // update list of values from query
        updateInputControlQueryData(inputControl);
        // update list of values for multispinner
        multiSpinner.setItemsList(inputControl.getListOfValues(), InputControlWrapper.NOTHING_SUBSTITUTE_LABEL);
        // set selected values
        List<ResourceParameter> params = new ArrayList<ResourceParameter>();
        for (ResourceProperty item : (List<ResourceProperty>) multiSpinner.getSelectedItems()) {
            params.add(new ResourceParameter(inputControl.getName(), item.getName(), true));
        }
        inputControl.setListOfSelectedValues(params);
        //update dependent controls if exist
        updateDependentControls(inputControl);
    }
    
    private void cleanupDependencies(List<InputControlWrapper> inputControls) {
        for (InputControlWrapper inputControl : inputControls) {
            List<InputControlWrapper> subDependentControls = new ArrayList<InputControlWrapper>();
            List<InputControlWrapper> dependentControls = inputControl.getSlaveDependencies();
            // collect all sub dependent controls
            for (InputControlWrapper dependentControl : dependentControls) {
                subDependentControls.addAll(getAllSubDependentControls(dependentControl));
            }
            // remove controls that have transitive dependencies
            dependentControls.removeAll(subDependentControls);
        }
    }
    
    private List<InputControlWrapper> getAllSubDependentControls(InputControlWrapper inputControl) {
        List<InputControlWrapper> result = new ArrayList<InputControlWrapper>();
        List<InputControlWrapper> dependentControls = inputControl.getSlaveDependencies();
        // collect dependent controls if it is not empty
        if (!dependentControls.isEmpty()) {
            result.addAll(dependentControls);
            // and do it recursively
            for (InputControlWrapper dependentControl : dependentControls) {
                result.addAll(getAllSubDependentControls(dependentControl));
            }
        }
        return result;
    }

    private void addBlankPropertyToList(List<ResourceProperty> propertyList) {
        ResourceProperty property = new ResourceProperty();
        property.setValue(InputControlWrapper.NOTHING_SUBSTITUTE_LABEL);
        property.setName(InputControlWrapper.NOTHING_SUBSTITUTE);
        propertyList.add(0, property);
    }

    // Date/Time Dialogs

    private void showDateDialog(InputControlWrapper inputControl, TextView dateDisplay, Calendar date) {
        activeInputControl = inputControl;
        activeDateDisplay = dateDisplay;
        activeDate = date;
        showDialog(DATE_DIALOG_ID);
    }

    private void showTimeDialog(InputControlWrapper inputControl, TextView dateDisplay, Calendar date) {
        activeInputControl = inputControl;
        activeDateDisplay = dateDisplay;
        activeDate = date;
        showDialog(TIME_DIALOG_ID);
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            activeDate.set(Calendar.YEAR, year);
            activeDate.set(Calendar.MONTH, monthOfYear);
            activeDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            boolean isDateTime = (activeInputControl.getDataType() == ResourceDescriptor.DT_TYPE_DATE_TIME);
            updateDateDisplay(activeDateDisplay, activeDate, isDateTime);
            // update control
            List<ResourceParameter> parameters = new ArrayList<ResourceParameter>();
            parameters.add(new ResourceParameter(activeInputControl.getName(), String.valueOf(activeDate.getTimeInMillis()), false));
            activeInputControl.setListOfSelectedValues(parameters);
            
            unregisterDateDisplay();
        }
    };

    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            activeDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            activeDate.set(Calendar.MINUTE, minute);
            boolean isDateTime = (activeInputControl.getDataType() == ResourceDescriptor.DT_TYPE_DATE_TIME);
            updateDateDisplay(activeDateDisplay, activeDate, isDateTime);
            // update control
            List<ResourceParameter> parameters = new ArrayList<ResourceParameter>();
            parameters.add(new ResourceParameter(activeInputControl.getName(), String.valueOf(activeDate.getTimeInMillis()), false));
            activeInputControl.setListOfSelectedValues(parameters);

            unregisterDateDisplay();
        }
    };

    private void updateDateDisplay(TextView dateDisplay, Calendar date, boolean showTime) {
        String displayText;
        if(showTime) {
            displayText = DateFormat.getDateTimeInstance().format(date.getTime());
        } else {
            displayText = DateFormat.getDateInstance().format(date.getTime());
        }
        dateDisplay.setText(displayText);

    }

    private void unregisterDateDisplay() {
        activeDateDisplay = null;
        activeDate = null;
        activeInputControl = null;
    }

}