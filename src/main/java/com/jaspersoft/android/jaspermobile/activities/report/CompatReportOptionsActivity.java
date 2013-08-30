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
import android.webkit.MimeTypeMap;
import android.widget.*;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.BaseHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.ReportHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.activities.SettingsActivity;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.db.tables.ReportOptions;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.request.SaveReportAttachmentRequest;
import com.jaspersoft.android.sdk.client.async.request.SaveReportAttachmentsRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetReportRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceRequest;
import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.ReportDescriptor;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.sdk.client.oxm.ResourceParameter;
import com.jaspersoft.android.sdk.client.oxm.ResourceProperty;
import com.jaspersoft.android.sdk.ui.widget.MultiSelectSpinner;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ivan Gadzhega
 * @since 1.5.2
 */
public class CompatReportOptionsActivity extends BaseReportOptionsActivity {

    private static final String REPORT_FILE_NAME = "report";

    private ResourceDescriptor resourceDescriptor;
    private List<InputControlWrapper> inputControls;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRefreshActionButtonState(true);
        GetResourceRequest request = new GetResourceRequest(jsRestClient, reportUri);
        long cacheExpiryDuration = SettingsActivity.getRepoCacheExpirationValue(this);
        serviceManager.execute(request, request.createCacheKey(), cacheExpiryDuration, new GetResourceListener());
    }

    public void runReportButtonClickHandler(View view) {
        String outputFormat = formatSpinner.getSelectedItem().toString();
        JsServerProfile profile = jsRestClient.getServerProfile();

        // generate report output according to selected format and REST services version
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

        //delete previous values for this report
        dbProvider.deleteReportOptions(profile.getId(), profile.getUsername(), profile.getOrganization(), reportUri);
        // Save new values
        for (ResourceParameter parameter : parameters) {
            dbProvider.insertReportOption(parameter.getName(), parameter.getValue(), parameter.isListItem(),
                    profile.getId(), profile.getUsername(), profile.getOrganization(), reportUri);
        }

        resourceDescriptor.setParameters(parameters);

        setRefreshActionButtonState(true);
        GetReportRequest request = new GetReportRequest(jsRestClient, resourceDescriptor, outputFormat);
        serviceManager.execute(request, new GetReportListener());
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

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

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class GetResourceListener implements RequestListener<ResourceDescriptor> {

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, CompatReportOptionsActivity.this, true);
        }

        @Override
        public void onRequestSuccess(ResourceDescriptor descriptor) {
            resourceDescriptor = descriptor;
            inputControls = new ArrayList<InputControlWrapper>();

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
            for(final InputControlWrapper inputControl : inputControls) {
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
                        // if data type specified as reference
                        if (inputControl.getDataType() == 0) {
                            ResourceDescriptor dataType = jsRestClient.getResource(inputControl.getDataTypeUri());
                            ResourceProperty prop = dataType.getPropertyByName(ResourceDescriptor.PROP_DATATYPE_TYPE);
                            inputControl.setDataType(Byte.parseByte(prop.getValue()));
                        }
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
            for (InputControlWrapper ic : inputControls) {
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
            setRefreshActionButtonState(false);
        }
    }

    private class GetReportListener implements RequestListener<ReportDescriptor> {
        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, CompatReportOptionsActivity.this, true);
        }

        @Override
        public void onRequestSuccess(ReportDescriptor reportDescriptor) {
            String outputFormat = formatSpinner.getSelectedItem().toString();
            String uuid = reportDescriptor.getUuid();

            File outputDir = getReportOutputCacheDir();

            // view report using internal viewer for HTML or external viewers for all other formats
            if (outputFormat.equalsIgnoreCase(RUN_OUTPUT_FORMAT_HTML)) {
                // get report attachments and save them to cache folder
                SaveReportAttachmentsRequest request = new SaveReportAttachmentsRequest(jsRestClient, uuid, reportDescriptor.getAttachments(), outputDir);
                serviceManager.execute(request, new SaveReportAttachmentsListener());
            } else {
                // workaround: manually define file extension depending on selected format
                String extension = (outputFormat.equalsIgnoreCase(RUN_OUTPUT_FORMAT_PDF)) ? ".pdf" : ".xls";
                // get the report output file and save it to cache folder
                File outputFile = new File(outputDir, resourceDescriptor.getName() + extension);
                SaveReportAttachmentRequest request = new SaveReportAttachmentRequest(jsRestClient, uuid, REPORT_FILE_NAME, outputFile);
                serviceManager.execute(request, new SaveReportAttachmentListener());
            }
        }
    }

    private class SaveReportAttachmentsListener implements RequestListener<File> {
        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, CompatReportOptionsActivity.this, true);
        }

        @Override
        public void onRequestSuccess(File outputDir) {
            setRefreshActionButtonState(false);
            // run the html report viewer
            Intent htmlViewer = new Intent();
            htmlViewer.setClass(CompatReportOptionsActivity.this, ReportHtmlViewerActivity.class);
            htmlViewer.putExtra(BaseHtmlViewerActivity.EXTRA_RESOURCE_URL,
                    Uri.fromFile(outputDir) + File.separator + REPORT_FILE_NAME);
            startActivity(htmlViewer);
        }
    }

    private class SaveReportAttachmentListener implements RequestListener<File> {
        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, CompatReportOptionsActivity.this, true);
        }

        @Override
        public void onRequestSuccess(File outputFile) {
            if (outputFile.exists()) {
                setRefreshActionButtonState(false);
                // run external viewer according to selected output format
                Uri path = Uri.fromFile(outputFile);
                String extension = MimeTypeMap.getFileExtensionFromUrl(outputFile.getPath());
                String contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                Intent externalViewer = new Intent(Intent.ACTION_VIEW);
                externalViewer.setDataAndType(path, contentType);
                externalViewer.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                try {
                    startActivity(externalViewer);
                }
                catch (ActivityNotFoundException e) {
                    // show notification if no app available to open selected format
                    Toast.makeText(CompatReportOptionsActivity.this, getString(R.string.ro_no_app_available_toast,
                            formatSpinner.getSelectedItem().toString()), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}