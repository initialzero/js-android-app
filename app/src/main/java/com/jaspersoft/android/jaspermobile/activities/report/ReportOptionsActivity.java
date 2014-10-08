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

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragmentActivity;
import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.ReportHtmlViewerActivity;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsValuesRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.ValidateInputControlsValuesRequest;
import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlStatesList;
import com.jaspersoft.android.sdk.client.oxm.control.validation.DateTimeFormatValidationRule;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.ui.widget.MultiSelectSpinner;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import roboguice.inject.InjectView;
import roboguice.util.Ln;

import static com.jaspersoft.android.jaspermobile.activities.report.DatePickerDialogHelper.DATE_DIALOG_ID;
import static com.jaspersoft.android.jaspermobile.activities.report.DatePickerDialogHelper.DEFAULT_DATE_FORMAT;
import static com.jaspersoft.android.jaspermobile.activities.report.DatePickerDialogHelper.TIME_DIALOG_ID;

/**
 * @author Ivan Gadzhega
 * @since 1.6
 */
public class ReportOptionsActivity extends RoboSpiceFragmentActivity {

    // Extras
    public static final String EXTRA_REPORT_LABEL = "ReportOptionsActivity.EXTRA_REPORT_LABEL";
    public static final String EXTRA_REPORT_URI = "ReportOptionsActivity.EXTRA_REPORT_URI";
    public static final String EXTRA_REPORT_CONTROLS = "ReportOptionsActivity.EXTRA_REPORT_CONTROLS";

    // Action Bar IDs
    private static final int ID_AB_INDETERMINATE_PROGRESS = 20;
    private static final int ID_AB_SETTINGS = 21;

    @Inject
    protected JsRestClient jsRestClient;
    @InjectView(R.id.runReportButton)
    protected Button runReportButton;

    protected Menu optionsMenu;
    protected String reportUri;

    private DatePickerDialogHelper dialogHelper;
    private List<InputControl> inputControls;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.report_options_layout);

        // init helper for date/time picker dialogs
        dialogHelper = new DatePickerDialogHelper(this);

        // get report label from extras and update title
        String reportLabel = getIntent().getExtras().getString(EXTRA_REPORT_LABEL);
        if (getActionBar() != null) {
            getActionBar().setTitle(reportLabel);
        }

        // get report uri from extras
        reportUri = getIntent().getExtras().getString(EXTRA_REPORT_URI);

        // prepare input controls
        initInputControls();
    }

    public void runReportButtonClickHandler(View view) {
        setRefreshActionButtonState(true);
        ValidateInputControlsValuesRequest request = new ValidateInputControlsValuesRequest(jsRestClient, reportUri, inputControls);
        getSpiceManager().execute(request, new ValidateInputControlsValuesListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // use the App Icon for Navigation
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // indeterminate progress
        MenuItem item = menu.add(Menu.NONE, ID_AB_INDETERMINATE_PROGRESS, Menu.NONE, R.string.loading_msg);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        // settings
        menu.add(Menu.NONE, ID_AB_SETTINGS, Menu.NONE, R.string.ab_settings)
                .setIcon(R.drawable.ic_action_settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        setRefreshActionButtonState(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case ID_AB_SETTINGS:
                SettingsActivity_.intent(this).start();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onOptionsItemSelected(item);
        }
    }

    protected void setRefreshActionButtonState(boolean refreshing) {
        runReportButton.setEnabled(!refreshing);
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

        if (inputControls != null) {
            for (InputControl inputControl : inputControls) {
                if (inputControl.isVisible() && !inputControl.isReadOnly()) {
                    inputControl.getInputView().setEnabled(!refreshing);
                }
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return dialogHelper.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        dialogHelper.onPrepareDialog(id, dialog);
    }

    protected void showDateDialog(InputControl inputControl, int id, TextView dateDisplay, Calendar date) {
        dialogHelper.showDateDialog(inputControl, id, dateDisplay, date);
    }

    @Override
    protected void onStop() {
        setRefreshActionButtonState(false);
        super.onStop();
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void updateDependentControls(InputControl inputControl) {
        if (!inputControl.getSlaveDependencies().isEmpty()) {
            setRefreshActionButtonState(true);
            GetInputControlsValuesRequest request = new GetInputControlsValuesRequest(jsRestClient, reportUri, inputControls);
            getSpiceManager().execute(request, new GetInputControlsValuesListener());
        }
    }

    private void runReport() {
        String reportTitle = getIntent().getExtras().getString(EXTRA_REPORT_LABEL);
        ArrayList<ReportParameter> parameters = initParametersUsingSelectedValues();
        runReportViewer(reportUri, reportTitle, parameters);
    }

    private ArrayList<ReportParameter> initParametersUsingSelectedValues() {
        ArrayList<ReportParameter> parameters = new ArrayList<ReportParameter>();
        for (InputControl inputControl : inputControls) {
            parameters.add(new ReportParameter(inputControl.getId(), inputControl.getSelectedValues()));
        }
        return parameters;
    }

    private void runReportViewer(String reportUri, String reportLabel, ArrayList<ReportParameter> parameters) {
        Intent htmlViewer = new Intent();
        htmlViewer.putParcelableArrayListExtra(ReportHtmlViewerActivity.EXTRA_REPORT_PARAMETERS, parameters);
        setResult(Activity.RESULT_OK, htmlViewer);
        finish();
    }

    private void hideAllValidationMessages() {
        for (InputControl control : inputControls) {
            TextView textView = (TextView) control.getErrorView();
            if (textView != null) {
                textView.setVisibility(View.GONE);
            }
        }
    }

    private void showValidationMessages(List<InputControlState> invalidStateList) {
        for (InputControl control : inputControls) {
            TextView textView = (TextView) control.getErrorView();
            if (textView != null) {
                Iterator<InputControlState> iterator = invalidStateList.iterator();
                while (iterator.hasNext()) {
                    InputControlState state = iterator.next();
                    if (control.getId().equals(state.getId())) {
                        textView.setText(state.getError());
                        textView.setVisibility(View.VISIBLE);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }

    private void initInputControls() {
        inputControls = getIntent().getExtras().getParcelableArrayList(EXTRA_REPORT_CONTROLS);

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.input_controls_layout);
        LayoutInflater inflater = getLayoutInflater();

        // init UI components for ICs
        for (final InputControl inputControl : inputControls) {
            if (inputControl.isVisible()) {
                switch (inputControl.getType()) {
                    case bool:
                        initBooleanControl(inputControl, baseLayout, inflater);
                        break;
                    case singleValueText:
                    case singleValueNumber:
                        initSingleValueControl(inputControl, baseLayout, inflater);
                        break;
                    case singleValueDate:
                    case singleValueDatetime:
                        initDateControl(inputControl, baseLayout, inflater);
                        break;
                    case singleSelect:
                    case singleSelectRadio:
                        initSingleSelectControl(inputControl, baseLayout, inflater);
                        break;
                    case multiSelect:
                    case multiSelectCheckbox:
                        initMultiSelectControl(inputControl, baseLayout, inflater);
                        break;
                }
            }
        }
    }

    private void initBooleanControl(final InputControl inputControl, LinearLayout baseLayout, LayoutInflater inflater) {
        Switch switchView = (Switch) inflater.inflate(R.layout.control_boolean_layout, baseLayout, false);
        switchView.setText(inputControl.getLabel());
        // set default value
        if (inputControl.getState().getValue() == null)
            inputControl.getState().setValue("false");
        switchView.setChecked(Boolean.parseBoolean(inputControl.getState().getValue()));
        //listener
        if (inputControl.isReadOnly()) {
            switchView.setEnabled(false);
        } else {
            switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    onStringValueChanged(inputControl, String.valueOf(isChecked));
                }
            });
        }
        // assign views to the control
        inputControl.setInputView(switchView);
        // show the control
        baseLayout.addView(switchView);
    }

    private void initSingleValueControl(final InputControl inputControl, LinearLayout baseLayout, LayoutInflater inflater) {
        View layoutView = inflater.inflate(R.layout.ic_single_value_layout, baseLayout, false);
        updateLabelView(inputControl, layoutView);

        EditText editText = (EditText) layoutView.findViewById(R.id.ic_edit_text);
        // allow only numbers if data type is numeric
        if (inputControl.getType() == InputControl.Type.singleValueNumber) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER
                    | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
        // set default value
        editText.setText(inputControl.getState().getValue());
        // add listener
        if (inputControl.isReadOnly()) {
            editText.setEnabled(false);
        } else {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                    onStringValueChanged(inputControl, editable.toString());
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
        }

        assignViews(inputControl, layoutView, editText);
        baseLayout.addView(layoutView);
    }

    private void initDateControl(final InputControl inputControl, LinearLayout baseLayout, LayoutInflater inflater) {
        View layoutView = inflater.inflate(R.layout.ic_single_value_date_layout, baseLayout, false);
        updateLabelView(inputControl, layoutView);

        final EditText editText = (EditText) layoutView.findViewById(R.id.ic_date_text);

        String format = DEFAULT_DATE_FORMAT;
        for (DateTimeFormatValidationRule validationRule : inputControl.getValidationRules(DateTimeFormatValidationRule.class)) {
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

        // get pickers
        ImageButton datePicker = (ImageButton) layoutView.findViewById(R.id.ic_date_picker_button);
        ImageButton timePicker = (ImageButton) layoutView.findViewById(R.id.ic_time_picker_button);
        // add listeners
        if (inputControl.isReadOnly()) {
            datePicker.setEnabled(false);
            timePicker.setEnabled(false);
            editText.setEnabled(false);
        } else {
            // add a click listener
            datePicker.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    showDateDialog(inputControl, DATE_DIALOG_ID, editText, startDate);
                }
            });

            boolean isDateTime = (inputControl.getType() == InputControl.Type.singleValueDatetime);
            if (isDateTime) {
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
                public void afterTextChanged(Editable editable) {
                    onStringValueChanged(inputControl, editable.toString());
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
        }

        assignViews(inputControl, layoutView, editText);
        baseLayout.addView(layoutView);
    }

    private void initSingleSelectControl(final InputControl inputControl, LinearLayout baseLayout, LayoutInflater inflater) {
        View layoutView = inflater.inflate(R.layout.ic_single_select_layout, baseLayout, false);
        updateLabelView(inputControl, layoutView);

        Spinner spinner = (Spinner) layoutView.findViewById(R.id.ic_spinner);
        spinner.setPrompt(inputControl.getLabel());

        ArrayAdapter<InputControlOption> lovAdapter = new ArrayAdapter<InputControlOption>(ReportOptionsActivity.this,
                android.R.layout.simple_spinner_item, inputControl.getState().getOptions());
        lovAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(lovAdapter);

        // set initial value for spinner
        for (InputControlOption option : inputControl.getState().getOptions()) {
            if (option.isSelected()) {
                int position = lovAdapter.getPosition(option);
                spinner.setSelection(position, false);
            }
        }

        // add listener
        if (inputControl.isReadOnly()) {
            spinner.setEnabled(false);
        } else {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    for (InputControlOption option : inputControl.getState().getOptions()) {
                        if (option.equals(parent.getSelectedItem())) {
                            if (!option.isSelected()) {
                                option.setSelected(true);
                                updateDependentControls(inputControl);
                            }
                        } else {
                            option.setSelected(false);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { /* Do nothing */ }
            });
        }

        assignViews(inputControl, layoutView, spinner);
        baseLayout.addView(layoutView);
    }

    private void initMultiSelectControl(final InputControl inputControl, LinearLayout baseLayout, LayoutInflater inflater) {
        View layoutView = inflater.inflate(R.layout.ic_multi_select_layout, baseLayout, false);
        updateLabelView(inputControl, layoutView);

        MultiSelectSpinner<InputControlOption> multiSpinner =
                (MultiSelectSpinner<InputControlOption>) layoutView.findViewById(R.id.ic_multi_spinner);
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

        // add listener
        if (inputControl.isReadOnly()) {
            multiSpinner.setEnabled(false);
        } else {
            multiSpinner.setOnItemsSelectedListener(new MultiSelectSpinner.OnItemsSelectedListener() {
                @Override
                public void onItemsSelected(List selectedItems) {
                    boolean valuesChanged = false;
                    // update selected values
                    for (InputControlOption option : inputControl.getState().getOptions()) {
                        boolean selectedBefore = option.isSelected();
                        boolean selectedNow = selectedItems.contains(option);
                        if (selectedBefore != selectedNow) valuesChanged = true;
                        option.setSelected(selectedNow);
                    }
                    // update dependent controls if exist
                    if (valuesChanged) updateDependentControls(inputControl);
                }
            });
        }

        assignViews(inputControl, layoutView, multiSpinner);
        baseLayout.addView(layoutView);
    }

    private void updateLabelView(InputControl inputControl, View view) {
        String mandatoryPrefix = (inputControl.isMandatory()) ? "* " : "";
        TextView textView = (TextView) view.findViewById(R.id.ic_text_label);
        textView.setText(mandatoryPrefix + inputControl.getLabel() + ":");
    }

    private void assignViews(InputControl inputControl, View layoutView, View inputView) {
        TextView errorView = (TextView) layoutView.findViewById(R.id.ic_error_text);
        inputControl.setInputView(inputView);
        inputControl.setErrorView(errorView);
    }

    private void onStringValueChanged(InputControl inputControl, String newValue) {
        String oldValue = inputControl.getState().getValue();
        if (newValue != null && !newValue.equals(oldValue)) {
            inputControl.getState().setValue(newValue);
            updateDependentControls(inputControl);
        }
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class GetInputControlsValuesListener implements RequestListener<InputControlStatesList> {

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, ReportOptionsActivity.this, false);
            setRefreshActionButtonState(false);
        }

        @Override
        public void onRequestSuccess(InputControlStatesList stateList) {
            for (InputControlState state : stateList.getInputControlStates()) {
                for (InputControl slaveControl : inputControls) {
                    if (slaveControl.getId().equals(state.getId())) {
                        slaveControl.setState(state);
                        if (slaveControl.isVisible()) {
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
                                            new ArrayAdapter<InputControlOption>(ReportOptionsActivity.this, android.R.layout.simple_spinner_item, state.getOptions());
                                    lovAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spinner.setAdapter(lovAdapter);
                                    // set initial value for spinner
                                    for (InputControlOption option : state.getOptions()) {
                                        if (option.isSelected()) {
                                            int position = lovAdapter.getPosition(option);
                                            spinner.setSelection(position, false);
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
            setRefreshActionButtonState(false);
        }

    }

    private class ValidateInputControlsValuesListener implements RequestListener<InputControlStatesList> {

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, ReportOptionsActivity.this, false);
            setRefreshActionButtonState(false);
        }

        @Override
        public void onRequestSuccess(InputControlStatesList stateList) {
            hideAllValidationMessages();
            List<InputControlState> invalidStateList = stateList.getInputControlStates();
            if (invalidStateList.isEmpty()) {
                runReport();
            } else {
                showValidationMessages(invalidStateList);
            }
            setRefreshActionButtonState(false);
        }

    }

}