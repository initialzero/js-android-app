/*
 * Copyright (C) 2012-2013 Jaspersoft Corporation. All rights reserved.
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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.BaseHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.ReportHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.db.tables.ReportOptions;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsRequest;
import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.control.validation.DateTimeFormatValidationRule;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.ui.widget.MultiSelectSpinner;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import roboguice.util.Ln;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.jaspersoft.android.jaspermobile.activities.report.DatePickerDialogHelper.DATE_DIALOG_ID;
import static com.jaspersoft.android.jaspermobile.activities.report.DatePickerDialogHelper.DEFAULT_DATE_FORMAT;
import static com.jaspersoft.android.jaspermobile.activities.report.DatePickerDialogHelper.TIME_DIALOG_ID;

/**
 * @author Ivan Gadzhega
 * @since 1.5.2
 */
public class ReportOptionsActivity extends BaseReportOptionsActivity {

    private List<InputControl> inputControls;
    private boolean skipRecursiveUpdate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRefreshActionButtonState(true);
        GetInputControlsRequest request = new GetInputControlsRequest(jsRestClient, reportUri);
        serviceManager.execute(request, new GetInputControlsListener());
    }

    public void runReportButtonClickHandler(View view) {
        String outputFormat = formatSpinner.getSelectedItem().toString();
        JsServerProfile profile = jsRestClient.getServerProfile();
        // generate report output according to selected format
        List<ReportParameter> parameters = new ArrayList<ReportParameter>();
        if (!inputControls.isEmpty()) {
            // validation
            List<InputControlState> stateList = jsRestClient.validateInputControlsValues(reportUri, inputControls);
            if(!stateList.isEmpty()) {
                for (InputControlState state : stateList) {
                    for (InputControl control : inputControls) {
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

            for (InputControl inputControl : inputControls) {
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
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

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

    private void updateDependentControls(InputControl inputControl) {
        updateDependentControls(inputControl, true);
    }

    private void updateDependentControls(InputControl inputControl, boolean updateViews) {
        if(!inputControl.getSlaveDependencies().isEmpty()) {
            List<ReportParameter> selectedValues = new ArrayList<ReportParameter>();
            // get values from master dependencies
            for (String masterId : inputControl.getMasterDependencies()) {
                for (InputControl control : inputControls) {
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
            // don't update recursively
            skipRecursiveUpdate = true;
            for (InputControlState state : stateList) {
                for(InputControl slaveControl : inputControls) {
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
            skipRecursiveUpdate = false;
        }
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class GetInputControlsListener implements RequestListener<InputControlsList> {

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, ReportOptionsActivity.this, true);
        }

        @Override
        public void onRequestSuccess(InputControlsList controlsList) {
            inputControls = controlsList.getInputControls();
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
            for (final InputControl inputControl : inputControls) {
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
                                if (!skipRecursiveUpdate) {
                                    updateDependentControls(inputControl);
                                }
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
                                if (!skipRecursiveUpdate) {
                                    updateDependentControls(inputControl);
                                }
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
                                showDateDialog(inputControl, DATE_DIALOG_ID, editText, startDate);
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
                                    showDateDialog(inputControl, TIME_DIALOG_ID, editText, startDate);
                                }
                            });
                        }

                        // add listener for text field
                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void afterTextChanged(Editable s) {
                                // update dependent controls if exist
                                if (!skipRecursiveUpdate) {
                                    updateDependentControls(inputControl);
                                }
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
                                new ArrayAdapter<InputControlOption>(ReportOptionsActivity.this, android.R.layout.simple_spinner_item, inputControl.getState().getOptions());
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
                                if (!skipRecursiveUpdate) {
                                    updateDependentControls(inputControl);
                                }
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
                                        if (!skipRecursiveUpdate) {
                                            updateDependentControls(inputControl);
                                        }
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
            setRefreshActionButtonState(false);
        }
    }

}