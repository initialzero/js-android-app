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
import android.database.Cursor;
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
import android.widget.*;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.AsyncTaskExceptionHandler;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.jaspermobile.db.tables.ReportOptions;
import com.jaspersoft.android.jaspermobile.util.CacheUtils;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.JsAsyncTaskManager;
import com.jaspersoft.android.sdk.client.async.JsOnTaskCallbackListener;
import com.jaspersoft.android.sdk.client.async.task.*;
import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.ReportDescriptor;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.sdk.client.oxm.ResourceParameter;
import com.jaspersoft.android.sdk.client.oxm.ResourceProperty;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;
import com.jaspersoft.android.sdk.client.oxm.control.validation.DateTimeFormatValidationRule;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.jaspersoft.android.sdk.ui.widget.MultiSelectSpinner;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

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
    private InputControlWrapper activeInputControlWrapper;
    private InputControl activeInputControl;

    private String reportUri;
    private ResourceDescriptor resourceDescriptor;
    private List inputControls;

    @InjectView(R.id.breadcrumbs_title_small)   private TextView breadCrumbsTitleSmall;
    @InjectView(R.id.breadcrumbs_title_large)   private TextView breadCrumbsTitleLarge;
    @InjectView(R.id.report_format_spinner)     private Spinner formatSpinner;

    @Inject private JsRestClient jsRestClient;

    private JsAsyncTaskManager jsAsyncTaskManager;
    private DatabaseProvider dbProvider;

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

        // Get the database provider
        dbProvider = new DatabaseProvider(this);

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
        reportUri = getIntent().getExtras().getString(EXTRA_REPORT_URI);

        if (jsRestClient.getServerInfo().getVersionCode() < ServerInfo.VERSION_CODES.EMERALD) {
            // REST v1
            GetResourceAsyncTask getResourceAsyncTask = new GetResourceAsyncTask(GET_RESOURCE_DESCRIPTOR_TASK,
                    getString(R.string.ro_pd_loading_report_info_msg), 0, jsRestClient, reportUri);
            jsAsyncTaskManager.executeTask(getResourceAsyncTask);
        } else {
            // REST v2
            inputControls = jsRestClient.getInputControlsForReport(reportUri);

            // Get a cursor with saved options for current report
            JsServerProfile profile = jsRestClient.getServerProfile();
            Cursor cursor = dbProvider.fetchReportOptions(profile.getId(), profile.getUsername(), profile.getOrganization(), reportUri);
            startManagingCursor(cursor);

            Map<String, List<String>> savedOptions = new HashMap<String, List<String>>();
            if (cursor.getCount() != 0) {
                // Iterate DB Records
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String name = cursor.getString(cursor.getColumnIndex(ReportOptions.KEY_NAME));
                    String value = cursor.getString(cursor.getColumnIndex(ReportOptions.KEY_VALUE));
                    if (savedOptions.containsKey(name)) {
                        savedOptions.get(name).add(value);
                    } else {
                        List<String> values = new ArrayList<String>();
                        values.add(value);
                        savedOptions.put(name, values);
                    }
                    cursor.moveToNext();
                }
            }

            LinearLayout baseLayout =  (LinearLayout) findViewById(R.id.input_controls_layout);
            LayoutInflater inflater = getLayoutInflater();

            // init UI components for ICs
            for (final InputControl inputControl : (List<InputControl>) inputControls) {
                String mandatoryPrefix = (inputControl.isMandatory()) ? "* " : "" ;
                restoreLastValues(inputControl, savedOptions);
                switch (inputControl.getType()) {
                    case bool: {
                        // inflate view
                        View view = inflater.inflate(R.layout.ic_boolean_layout, baseLayout, false);
                        CheckBox checkBox = (CheckBox) view.findViewById(R.id.ic_checkbox);
                        checkBox.setText(inputControl.getLabel());
                        // set default value
                        if (inputControl.getState().getValue() == null)
                            inputControl.getState().setValue("false");
                        checkBox.setChecked(Boolean.parseBoolean(inputControl.getState().getValue()));
                        //listener
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                // update selected value
                                inputControl.getState().setValue(String.valueOf(isChecked));
                                // update dependent controls if exist
                                updateDependentControls(inputControl);
                            }
                        });
                        // assign views to the control
                        inputControl.setInputView(checkBox);
                        // show the control
                        baseLayout.addView(view);
                        break;
                    }
                    case singleValueText:
                    case singleValueNumber: {
                        // inflate view
                        View view = inflater.inflate(R.layout.ic_single_value_layout, baseLayout, false);
                        TextView textView = (TextView) view.findViewById(R.id.ic_text_label);
                        textView.setText(mandatoryPrefix + inputControl.getLabel() + ":");
                        EditText editText = (EditText) view.findViewById(R.id.ic_edit_text);
                        // allow only numbers if data type is numeric
                        if (inputControl.getType() == InputControl.Type.singleValueNumber) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER
                                    | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        }
                        // set default value
                        editText.setText(inputControl.getState().getValue());
                        // add listener
                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void afterTextChanged(Editable s) {
                                // update selected value
                                inputControl.getState().setValue(s.toString());
                                // update dependent controls if exist
                                updateDependentControls(inputControl);
                            }
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {}
                        });
                        TextView errorView = (TextView) view.findViewById(R.id.ic_error_text);
                        // assign views to the control
                        inputControl.setInputView(editText);
                        inputControl.setErrorView(errorView);
                        // show the control
                        baseLayout.addView(view);
                        break;
                    }
                    case singleValueDate:
                    case singleValueDatetime: {
                        // inflate view
                        View view = inflater.inflate(R.layout.ic_single_value_date_layout, baseLayout, false);
                        TextView textView = (TextView) view.findViewById(R.id.ic_text_label);
                        textView.setText(mandatoryPrefix + inputControl.getLabel() + ":");
                        final EditText editText = (EditText) view.findViewById(R.id.ic_date_text);

                        String format = DEFAULT_DATE_FORMAT;
                        for (DateTimeFormatValidationRule validationRule: inputControl.getValidationRules(DateTimeFormatValidationRule.class)) {
                                format = validationRule.getFormat();
                        }
                        DateFormat formatter = new SimpleDateFormat(format);

                        // set default value
                        final Calendar startDate = Calendar.getInstance();
                        String defaultValue = inputControl.getState().getValue();
                        if (defaultValue != null) {
                            try {
                                startDate.setTime(formatter.parse(defaultValue));
                                editText.setText(defaultValue);
                            } catch (ParseException e) {
                                Ln.w("Unparseable date: %s", defaultValue);
                            }
                        }

                        // init the date picker
                        ImageButton datePicker = (ImageButton) view.findViewById(R.id.ic_date_picker_button);
                        // add a click listener
                        datePicker.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                showDateDialog(DATE_DIALOG_ID, null, inputControl, editText, startDate);
                            }
                        });

                        boolean isDateTime = (inputControl.getType() == InputControl.Type.singleValueDatetime);

                        if (isDateTime) {
                            // init the time picker
                            ImageButton timePicker = (ImageButton) view.findViewById(R.id.ic_time_picker_button);
                            timePicker.setVisibility(View.VISIBLE);
                            // add a click listener
                            timePicker.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    showDateDialog(TIME_DIALOG_ID, null, inputControl, editText, startDate);
                                }
                            });
                        }

                        // add listener for text field
                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void afterTextChanged(Editable s) {
                                // update dependent controls if exist
                                updateDependentControls(inputControl);
                            }

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                /* Do nothing */
                            }
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                /* Do nothing */
                            }
                        });

                        TextView errorView = (TextView) view.findViewById(R.id.ic_error_text);
                        // assign views to the control
                        inputControl.setInputView(editText);
                        inputControl.setErrorView(errorView);
                        // show the control
                        baseLayout.addView(view);
                        break;
                    }
                    case singleSelect:
                    case singleSelectRadio: {
                        // inflate view
                        View view = inflater.inflate(R.layout.ic_single_select_layout, baseLayout, false);
                        TextView textView = (TextView) view.findViewById(R.id.ic_text_label);
                        textView.setText(mandatoryPrefix + inputControl.getLabel() + ":");
                        Spinner spinner = (Spinner) view.findViewById(R.id.ic_spinner);
                        spinner.setPrompt(inputControl.getLabel());

                        ArrayAdapter<InputControlOption> lovAdapter =
                                new ArrayAdapter<InputControlOption>(this, android.R.layout.simple_spinner_item, inputControl.getState().getOptions());
                        lovAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(lovAdapter);

                        // set initial value for spinner
                        for (InputControlOption option : inputControl.getState().getOptions()) {
                            if (option.isSelected()) {
                                int position = lovAdapter.getPosition(option);
                                spinner.setSelection(position);
                            }
                        }

                        // listener
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                // update selected value
                                for (InputControlOption option : inputControl.getState().getOptions()) {
                                    option.setSelected(option.equals(parent.getSelectedItem()));
                                }
                                // update dependent controls if exist
                                updateDependentControls(inputControl);
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) { /* Do nothing */ }
                        });

                        TextView errorView = (TextView) view.findViewById(R.id.ic_error_text);
                        // assign views to the control
                        inputControl.setInputView(spinner);
                        inputControl.setErrorView(errorView);
                        // show the control
                        baseLayout.addView(view);
                        break;
                    }
                    case multiSelect:
                    case multiSelectCheckbox:
                        // inflate view
                        View view = inflater.inflate(R.layout.ic_multi_select_layout, baseLayout, false);
                        TextView textView = (TextView) view.findViewById(R.id.ic_text_label);
                        textView.setText(mandatoryPrefix + inputControl.getLabel() + ":");
                        MultiSelectSpinner<InputControlOption> multiSpinner = (MultiSelectSpinner<InputControlOption>) view.findViewById(R.id.ic_multi_spinner);
                        multiSpinner.setPrompt(inputControl.getLabel());
                        // init values
                        multiSpinner.setItemsList(inputControl.getState().getOptions(), InputControlWrapper.NOTHING_SUBSTITUTE_LABEL);

                        // set selected values
                        List<Integer> positions = new ArrayList<Integer>();
                        for (InputControlOption option : inputControl.getState().getOptions()) {
                            if (option.isSelected()) {
                                positions.add(multiSpinner.getItemPosition(option));
                            }
                        }
                        multiSpinner.setSelection(positions);

                        // listener
                        multiSpinner.setOnItemsSelectedListener(
                                new MultiSelectSpinner.OnItemsSelectedListener() {
                                    @Override
                                    public void onItemsSelected(List selectedItems) {
                                        // update selected values
                                        for (InputControlOption option : inputControl.getState().getOptions()) {
                                            boolean isSelected = selectedItems.contains(option);
                                            option.setSelected(isSelected);
                                        }
                                        // update dependent controls if exist
                                        updateDependentControls(inputControl);
                                    }
                                });

                        TextView errorView = (TextView) view.findViewById(R.id.ic_error_text);
                        // assign views to the control
                        inputControl.setInputView(multiSpinner);
                        inputControl.setErrorView(errorView);
                        // show the control
                        baseLayout.addView(view);
                        break;
                }
            }
        }
    }

    private void updateDependentControls(InputControl inputControl) {
        updateDependentControls(inputControl, true);
    }

    private void updateDependentControls(InputControl inputControl, boolean updateViews) {
        if(!inputControl.getSlaveDependencies().isEmpty()) {
            List<ReportParameter> selectedValues = new ArrayList<ReportParameter>();
            // get values from master dependencies
            for (String masterId : inputControl.getMasterDependencies()) {
                for (InputControl control : (List<InputControl>) inputControls) {
                    if(control.getId().equals(masterId)) {
                        selectedValues.add(new ReportParameter(control.getId(), control.getSelectedValues()));
                    }
                }
            }
            // get selected values from control that was changed
            selectedValues.add(new ReportParameter(inputControl.getId(), inputControl.getSelectedValues()));
            // get updated values for slaves
            List<InputControlState> stateList =
                    jsRestClient.getUpdatedInputControlsValues(reportUri, inputControl.getSlaveDependencies(), selectedValues);
            for (InputControlState state : stateList) {
                for(InputControl slaveControl : (List<InputControl>) inputControls) {
                    if (slaveControl.getId().equals(state.getId())) {
                        slaveControl.setState(state);
                        if(updateViews) {
                            switch (slaveControl.getType()) {
                                case bool:
                                    CheckBox checkBox = (CheckBox) slaveControl.getInputView();
                                    checkBox.setChecked(Boolean.parseBoolean(state.getValue()));
                                    break;
                                case singleValueText:
                                case singleValueNumber:
                                case singleValueDate:
                                case singleValueDatetime:
                                    EditText editText = (EditText) slaveControl.getInputView();
                                    editText.setText(state.getValue());
                                    break;
                                case singleSelect:
                                case singleSelectRadio:
                                    Spinner spinner = (Spinner) slaveControl.getInputView();
                                    ArrayAdapter<InputControlOption> lovAdapter =
                                            new ArrayAdapter<InputControlOption>(this, android.R.layout.simple_spinner_item, state.getOptions());
                                    lovAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spinner.setAdapter(lovAdapter);
                                    // set initial value for spinner
                                    for (InputControlOption option : state.getOptions()) {
                                        if (option.isSelected()) {
                                            int position = lovAdapter.getPosition(option);
                                            spinner.setSelection(position);
                                        }
                                    }
                                    break;
                                case multiSelect:
                                case multiSelectCheckbox:
                                    MultiSelectSpinner<InputControlOption> multiSpinner =
                                            (MultiSelectSpinner<InputControlOption>) slaveControl.getInputView();
                                    multiSpinner.setItemsList(state.getOptions(), InputControlWrapper.NOTHING_SUBSTITUTE_LABEL);
                                    // set selected values
                                    List<Integer> positions = new ArrayList<Integer>();
                                    for (InputControlOption option : state.getOptions()) {
                                        if (option.isSelected()) {
                                            positions.add(multiSpinner.getItemPosition(option));
                                        }
                                    }
                                    multiSpinner.setSelection(positions);
                                    break;
                            }
                        }
                        break;
                    }
                }
            }
        }
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
        String outputFormat = formatSpinner.getSelectedItem().toString();
        JsServerProfile profile = jsRestClient.getServerProfile();
        // generate report output according to selected format and REST services version
        if (jsRestClient.getServerInfo().getVersionCode() >= ServerInfo.VERSION_CODES.EMERALD ) {
            // REST v2
            List<ReportParameter> parameters = new ArrayList<ReportParameter>();
            if (!inputControls.isEmpty()) {
                // validation
                List<InputControlState> stateList = jsRestClient.validateInputControlsValues(reportUri, inputControls);
                if(!stateList.isEmpty()) {
                    for (InputControlState state : stateList) {
                        for (InputControl control : (List<InputControl>) inputControls) {
                            TextView textView = (TextView) control.getErrorView();
                            if (textView != null) {
                                if(control.getId().equals(state.getId())) {
                                    textView.setText(state.getError());
                                    textView.setVisibility(View.VISIBLE);
                                } else {
                                    textView.setVisibility(View.GONE);
                                }
                            }
                        }

                    }
                    return;
                }

                for (InputControl inputControl : (List<InputControl>) inputControls) {
                    parameters.add(new ReportParameter(inputControl.getId(), inputControl.getSelectedValues()));
                }

                //delete previous values from db
                dbProvider.deleteReportOptions(profile.getId(), profile.getUsername(), profile.getOrganization(), reportUri);
                // Save new values to db
                for (ReportParameter parameter : parameters) {
                    for (String value : parameter.getValues()) {
                        dbProvider.insertReportOption(parameter.getName(), value, false,
                                profile.getId(), profile.getUsername(), profile.getOrganization(), reportUri);
                    }
                }
            }

            String reportUrl = jsRestClient.generateReportUrl(reportUri, parameters, outputFormat);

            if (RUN_OUTPUT_FORMAT_HTML.equalsIgnoreCase(outputFormat)) {
                // run the html report viewer
                Intent htmlViewer = new Intent();
                htmlViewer.setClass(this, ReportHtmlViewerActivity.class);
                htmlViewer.putExtra(BaseHtmlViewerActivity.EXTRA_RESOURCE_URL, reportUrl);
                startActivity(htmlViewer);
            } else {
                String contentType, extension;
                if (RUN_OUTPUT_FORMAT_PDF.equalsIgnoreCase(outputFormat)) {
                    contentType = "application/pdf";
                    extension = ".pdf";
                } else {
                    contentType = "application/xls";
                    extension = ".xls";
                }
                File outputDir = getReportOutputCacheDir();
                File outputFile = new File(outputDir, reportUri + extension);
                // get the report output file and save it to cache folder
                jsRestClient.saveReportOutputToFile(reportUrl, outputFile);
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
        } else {
            // REST v1
            List<ResourceParameter> parameters = new ArrayList<ResourceParameter>();
            boolean hasErrors = false;
            for (InputControlWrapper inputControl : (List<InputControlWrapper>) inputControls) {
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

            //delete previous values for this report
            dbProvider.deleteReportOptions(profile.getId(), profile.getUsername(), profile.getOrganization(), reportUri);
            // Save new values
            for (ResourceParameter parameter : parameters) {
                dbProvider.insertReportOption(parameter.getName(), parameter.getValue(), parameter.isListItem(),
                        profile.getId(), profile.getUsername(), profile.getOrganization(), reportUri);
            }

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

                    inputControls = new ArrayList();

                    // Get input controls from report resource descriptor
                    for(ResourceDescriptor internalResource : resourceDescriptor.getInternalResources()) {
                        if(internalResource.getWsType() == ResourceDescriptor.WsType.inputControl) {
                            inputControls.add(new InputControlWrapper(internalResource));
                        }
                    }

                    // Update the dependencies for the input controls
                    for (InputControlWrapper i : (List<InputControlWrapper>) inputControls) {
                        List<String> parameters = i.getParameterDependencies();
                        if (!parameters.isEmpty()) {
                            for (String parameter : parameters) {
                                for (InputControlWrapper j : (List<InputControlWrapper>) inputControls) {
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

                    // Get a cursor with saved options for current report
                    JsServerProfile profile = jsRestClient.getServerProfile();
                    Cursor cursor = dbProvider.fetchReportOptions(profile.getId(), profile.getUsername(), profile.getOrganization(), reportUri);
                    startManagingCursor(cursor);

                    List<ResourceParameter> savedParameters = new ArrayList<ResourceParameter>();
                    if (cursor.getCount() != 0) {
                        // Iterate DB Records
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()) {
                            ResourceParameter parameter = new ResourceParameter();
                            parameter.setName(cursor.getString(cursor.getColumnIndex(ReportOptions.KEY_NAME)));
                            parameter.setValue(cursor.getString(cursor.getColumnIndex(ReportOptions.KEY_VALUE)));
                            parameter.isListItem(cursor.getInt(cursor.getColumnIndex(ReportOptions.KEY_IS_LIST_ITEM)) > 0);
                            savedParameters.add(parameter);
                            cursor.moveToNext();
                        }
                    }

                    LinearLayout baseLayout =  (LinearLayout) findViewById(R.id.input_controls_layout);
                    LayoutInflater inflater = getLayoutInflater();

                    // init UI components for ICs
                    for(final InputControlWrapper inputControl : (List<InputControlWrapper>) inputControls) {
                        String mandatoryPrefix = (inputControl.isMandatory()) ? "* " : "" ;
                        switch (inputControl.getType()) {
                            case ResourceDescriptor.IC_TYPE_BOOLEAN: {
                                // inflate view
                                View view = inflater.inflate(R.layout.ic_boolean_layout, baseLayout, false);
                                CheckBox checkBox = (CheckBox) view.findViewById(R.id.ic_checkbox);
                                checkBox.setText(inputControl.getLabel());
                                // set default value
                                restoreLastValues(inputControl, savedParameters);
                                if (inputControl.getListOfSelectedValues().isEmpty()) {
                                    // check-box is false by default
                                    List<ResourceParameter> params = new ArrayList<ResourceParameter>();
                                    params.add(new ResourceParameter(inputControl.getName(), false, false));
                                    inputControl.setListOfSelectedValues(params);
                                }
                                checkBox.setChecked(Boolean.parseBoolean(inputControl.getListOfSelectedValues().get(0).getValue()));

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

                                        // set default value
                                        restoreLastValues(inputControl, savedParameters);

                                        if (!inputControl.getListOfSelectedValues().isEmpty()) {
                                            editText.setText(inputControl.getListOfSelectedValues().get(0).getValue());
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

                                        // get the current date
                                        final Calendar startDate = Calendar.getInstance();
                                        // init the date picker
                                        ImageButton datePicker = (ImageButton) view.findViewById(R.id.ic_date_picker_button);
                                        // add a click listener
                                        datePicker.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                showDateDialog(DATE_DIALOG_ID, inputControl, null, editText, startDate);
                                            }
                                        });

                                        boolean isDateTime = (inputControl.getDataType() == ResourceDescriptor.DT_TYPE_DATE_TIME);

                                        if (isDateTime) {
                                            // init the time picker
                                            ImageButton timePicker = (ImageButton) view.findViewById(R.id.ic_time_picker_button);
                                            timePicker.setVisibility(View.VISIBLE);
                                            // add a click listener
                                            timePicker.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    showDateDialog(TIME_DIALOG_ID, inputControl, null, editText, startDate);
                                                }
                                            });
                                        }

                                        // set default value
                                        restoreLastValues(inputControl, savedParameters);
                                        if (!inputControl.getListOfSelectedValues().isEmpty()) {
                                            startDate.setTimeInMillis(Long.parseLong(inputControl.getListOfSelectedValues().get(0).getValue()));
                                            updateDateDisplay(editText, startDate, isDateTime);
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
                                        // update selected value and update dependent controls if exist
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
                                                // update selected values and update dependent controls if exist
                                                updateMultiSelectControl(inputControl, selectedItems);
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
                    for (InputControlWrapper ic : (List<InputControlWrapper>) inputControls) {
                        rootControls.removeAll(getAllSubDependentControls(ic));
                    }

                    // init values for ICs
                    for(final InputControlWrapper inputControl : rootControls) {
                        switch (inputControl.getType()) {
                            case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_LIST_OF_VALUES:
                            case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_LIST_OF_VALUES_RADIO: {
                                updateSingleSelectValues(inputControl, false, savedParameters);
                                break;
                            }
                            case ResourceDescriptor.IC_TYPE_MULTI_SELECT_LIST_OF_VALUES:
                            case ResourceDescriptor.IC_TYPE_MULTI_SELECT_LIST_OF_VALUES_CHECKBOX: {
                                updateMultiSelectValues(inputControl, false, savedParameters);
                                break;
                            }
                            case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_QUERY:
                            case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_QUERY_RADIO: {
                                updateSingleSelectValues(inputControl, true, savedParameters);
                                break;
                            }
                            case ResourceDescriptor.IC_TYPE_MULTI_SELECT_QUERY:
                            case ResourceDescriptor.IC_TYPE_MULTI_SELECT_QUERY_CHECKBOX: {
                                updateMultiSelectValues(inputControl, true, savedParameters);
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
                    htmlViewer.putExtra(BaseHtmlViewerActivity.EXTRA_RESOURCE_URL,
                                        Uri.fromFile(outputDir) + File.separator + REPORT_FILE_NAME);
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

    @Override
    public void onDestroy() {
        // close any open database object
        if (dbProvider != null) dbProvider.close();
        super.onDestroy();
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

    // REST v1
    private void restoreLastValues(InputControlWrapper inputControl, List<ResourceParameter> savedParameters) {
        if(inputControl.getListOfSelectedValues().isEmpty()) {
            if (!savedParameters.isEmpty()) {
                Iterator<ResourceParameter> iterator = savedParameters.iterator();
                while (iterator.hasNext()) {
                    ResourceParameter parameter = iterator.next();
                    if(inputControl.getName().equals(parameter.getName())) {
                        inputControl.getListOfSelectedValues().add(parameter);
                        iterator.remove();
                    }
                }

            }
        }
    }

    // REST v2
    private void restoreLastValues(InputControl inputControl, Map<String, List<String>> savedOptions) {
        if (savedOptions.containsKey(inputControl.getId())) {
            List<String> values = savedOptions.get(inputControl.getId());
            if (!values.isEmpty()) {
                switch (inputControl.getType()) {
                    case bool:
                    case singleValueText:
                    case singleValueNumber:
                    case singleValueDate:
                    case singleValueDatetime:
                        inputControl.getState().setValue(values.get(0));
                        break;
                    case singleSelect:
                    case singleSelectRadio:
                    case multiSelect:
                    case multiSelectCheckbox:
                        // unselect all
                        for (InputControlOption option : inputControl.getState().getOptions()) {
                            option.setSelected(false);
                        }
                        // select saved
                        for (String value : values) {
                            for (InputControlOption option : inputControl.getState().getOptions()) {
                                if (option.getValue().equals(value)) {
                                    option.setSelected(true);
                                    break;
                                }
                            }

                        }
                        break;
                }

                updateDependentControls(inputControl, false);
            }
        }
    }

    private void updateDependentControls(InputControlWrapper inputControl) {
        updateDependentControls(inputControl, null);
    }

    private void updateDependentControls(InputControlWrapper inputControl, List<ResourceParameter> savedParameters) {
        for (InputControlWrapper dependentControl : inputControl.getSlaveDependencies()) {
            // update view
            switch (dependentControl.getType()) {
                case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_QUERY:
                case ResourceDescriptor.IC_TYPE_SINGLE_SELECT_QUERY_RADIO: {
                    updateSingleSelectValues(dependentControl, true, savedParameters);
                    break;
                }
                case ResourceDescriptor.IC_TYPE_MULTI_SELECT_QUERY:
                case ResourceDescriptor.IC_TYPE_MULTI_SELECT_QUERY_CHECKBOX:
                    updateMultiSelectValues(dependentControl, true, savedParameters);
                    break;
            }

        }
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

    private void updateMultiSelectControl(InputControlWrapper inputControl, List<ResourceProperty> selectedItems) {
        List<ResourceParameter> params = new ArrayList<ResourceParameter>();
        // update selected values
        for (ResourceProperty item : (List<ResourceProperty>) selectedItems) {
            params.add(new ResourceParameter(inputControl.getName(), item.getName(), true));
        }
        inputControl.setListOfSelectedValues(params);
        // update dependent controls if exist
        updateDependentControls(inputControl);
    }

    private void updateSingleSelectValues(InputControlWrapper inputControl, boolean isQueryValues,
                                          List<ResourceParameter> savedParameters) {
        // get View from input control
        Spinner spinner = (Spinner) inputControl.getInputView();

        // update list of values from query
        if (isQueryValues) {
            updateInputControlQueryData(inputControl);
        }

        // set initial values for spinner
        if (!inputControl.isMandatory()) {
            addBlankPropertyToList(inputControl.getListOfValues());
        }
        ArrayAdapter<ResourceProperty> lovAdapter =
                new ArrayAdapter<ResourceProperty>(this, android.R.layout.simple_spinner_item, inputControl.getListOfValues());
        lovAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(lovAdapter);

        // set default value
        if (savedParameters != null) {
            restoreLastValues(inputControl, savedParameters);
            if (!inputControl.getListOfSelectedValues().isEmpty()) {
                for (ResourceProperty property : inputControl.getListOfValues()) {
                    if (property.getName().equals(inputControl.getListOfSelectedValues().get(0).getValue())) {
                        int position = lovAdapter.getPosition(property);
                        spinner.setSelection(position);
                        break;
                    }
                }
            }
        }

        //update dependent controls if exist
        updateDependentControls(inputControl, savedParameters);
    }

    private void updateMultiSelectValues(InputControlWrapper inputControl, boolean isQueryValues,
                                         List<ResourceParameter> savedParameters) {
        // get View from input control
        MultiSelectSpinner multiSpinner = (MultiSelectSpinner) inputControl.getInputView();
        // update list of values from query
        if (isQueryValues) {
            updateInputControlQueryData(inputControl);
        }
        // set initial values for multispinner
        multiSpinner.setItemsList(inputControl.getListOfValues(), InputControlWrapper.NOTHING_SUBSTITUTE_LABEL);

        // set default value
        if (savedParameters != null) {
            restoreLastValues(inputControl, savedParameters);
            if (!inputControl.getListOfSelectedValues().isEmpty()) {
                List<Integer> positons = new ArrayList<Integer>();
                for (ResourceProperty property : inputControl.getListOfValues()) {
                    for (ResourceParameter parameter : inputControl.getListOfSelectedValues()) {
                        if (property.getName().equals(parameter.getValue())) {
                            positons.add(multiSpinner.getItemPosition(property));
                        }
                    }
                }
                multiSpinner.setSelection(positons);
            }
        }

        //update dependent controls if exist
        updateDependentControls(inputControl, savedParameters);
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

    private void showDateDialog(int id, InputControlWrapper inputControlWrapper, InputControl InputControl,
                                TextView dateDisplay, Calendar date) {
        activeInputControlWrapper = inputControlWrapper;
        activeInputControl = InputControl;
        activeDateDisplay = dateDisplay;
        activeDate = date;
        showDialog(id);
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            activeDate.set(Calendar.YEAR, year);
            activeDate.set(Calendar.MONTH, monthOfYear);
            activeDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDisplayAndValueOnDateSet();
        }
    };

    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            activeDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            activeDate.set(Calendar.MINUTE, minute);
            updateDisplayAndValueOnDateSet();

        }
    };

    private void updateDisplayAndValueOnDateSet() {
        if (activeInputControlWrapper != null) {
            boolean isDateTime = (activeInputControlWrapper.getDataType() == ResourceDescriptor.DT_TYPE_DATE_TIME);
            updateDateDisplay(activeDateDisplay, activeDate, isDateTime);
            // update control
            List<ResourceParameter> parameters = new ArrayList<ResourceParameter>();
            parameters.add(new ResourceParameter(activeInputControlWrapper.getName(), String.valueOf(activeDate.getTimeInMillis()), false));
            activeInputControlWrapper.setListOfSelectedValues(parameters);
        } else if (activeInputControl != null) {
            String format = DEFAULT_DATE_FORMAT;
            for (DateTimeFormatValidationRule validationRule : activeInputControl.getValidationRules(DateTimeFormatValidationRule.class)) {
                format = validationRule.getFormat();
            }
            DateFormat formatter = new SimpleDateFormat(format);
            String date = formatter.format(activeDate.getTime()) ;
            activeDateDisplay.setText(date);
            activeInputControl.getState().setValue(date);
        }
        unregisterDateDisplay();
    }

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
        activeInputControlWrapper = null;
        activeInputControl = null;
    }

}