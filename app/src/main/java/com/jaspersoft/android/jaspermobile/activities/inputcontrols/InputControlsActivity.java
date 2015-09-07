/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.inputcontrols;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.ItemSpaceDecoration;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceActivity;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsValuesRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.ValidateInputControlsValuesRequest;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlStatesList;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ivan Gadzhega
 * @author Tom Koptel
 * @since 1.6
 */
@EActivity(R.layout.report_options_layout)
@OptionsMenu(R.menu.am_run_report_menu)
public class InputControlsActivity extends RoboSpiceActivity implements InputControlsAdapter.InputControlInteractionListener {
    // Extras
    public static final String RESULT_SAME_PARAMS = "ReportOptionsActivity.SAME_PARAMS";

    @Inject
    protected JsRestClient jsRestClient;
    @Inject
    protected ReportParamsStorage paramsStorage;

    @OptionsMenuItem(R.id.runReportAction)
    protected MenuItem runReportAction;
    @ViewById(R.id.inputControlsList)
    protected RecyclerView inputControlsList;

    @Extra
    protected String reportUri;

    private ArrayList<InputControl> inputControls;
    private InputControlsAdapter mAdapter;

    @AfterViews
    protected void init() {
        initInputControls();
        showInputControls();
    }

    @OptionsItem(R.id.runReportAction)
    final void runReportAction() {
        setRefreshActionButtonState(true);
        ValidateInputControlsValuesRequest request = new ValidateInputControlsValuesRequest(jsRestClient, reportUri, inputControls);
        getSpiceManager().execute(request, new ValidateInputControlsValuesListener());
    }

    @Override
    public void onBooleanStateChanged(InputControl inputControl, boolean newState) {
        inputControl.getState().setValue(String.valueOf(newState));
        updateDependentControls(inputControl);
    }

    @Override
    public void onValueTextChanged(InputControl inputControl, String newValue) {
        inputControl.getState().setValue(newValue);
        updateDependentControls(inputControl);
    }

    @Override
    public void onSingleSelectIcClicked(InputControl inputControl) {
    }

    @Override
    public void onMultiSelectIcClicked(InputControl inputControl) {
    }

    @Override
    public void onDateIcClicked(InputControl inputControl) {
        //showDateDialog(inputControl, TIME_DIALOG_ID, editText, startDate);
    }

    @Override
    public void onTimeIcClicked(InputControl inputControl) {
        //showDateDialog(inputControl, DATE_DIALOG_ID, editText, startDate);
    }

    @Override
    public void onDateClear(InputControl inputControl) {
        inputControl.getState().setValue("");
        updateDependentControls(inputControl);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void initInputControls() {
        inputControls = paramsStorage.getInputControls(reportUri);
        updateInputControlsFromReportParams();
    }

    private void showInputControls() {
        mAdapter = new InputControlsAdapter(inputControls);
        int dividerHeight = (int) getResources().getDimension(R.dimen.ic_divider_height);

        inputControlsList.addItemDecoration(new ItemSpaceDecoration(dividerHeight));
        inputControlsList.setLayoutManager(new LinearLayoutManager(this));
        inputControlsList.setAdapter(mAdapter);
    }

    private void setRefreshActionButtonState(boolean refreshing) {
        if (refreshing) {
            runReportAction.setActionView(R.layout.actionbar_indeterminate_progress);
        } else {
            runReportAction.setActionView(null);
        }

        mAdapter.setListEnabled(refreshing);
    }

    private boolean isNewParamsEqualOld(ArrayList<ReportParameter> newParams) {
        ArrayList<ReportParameter> oldParams = paramsStorage.getReportParameters(reportUri);

        if (oldParams.size() != newParams.size()) {
            return false;
        }

        for (int i = 0; i < oldParams.size(); i++) {
            if (!oldParams.get(i).getValues().equals(newParams.get(i).getValues())) return false;
        }

        return true;
    }

    private void updateDependentControls(InputControl inputControl) {
        if (!inputControl.getSlaveDependencies().isEmpty()) {
            setRefreshActionButtonState(true);
            GetInputControlsValuesRequest request = new GetInputControlsValuesRequest(jsRestClient, reportUri, inputControls);
            getSpiceManager().execute(request, new GetInputControlsValuesListener());
        }
    }

    private void runReport() {
        Intent htmlViewer = new Intent();
        ArrayList<ReportParameter> parameters = initParametersUsingSelectedValues();
        if (isNewParamsEqualOld(parameters)) {
            htmlViewer.putExtra(RESULT_SAME_PARAMS, true);
        }
        paramsStorage.putReportParameters(reportUri, parameters);
        setResult(Activity.RESULT_OK, htmlViewer);
        finish();
    }

    private ArrayList<ReportParameter> initParametersUsingSelectedValues() {
        ArrayList<ReportParameter> parameters = new ArrayList<ReportParameter>();
        for (InputControl inputControl : inputControls) {
            parameters.add(new ReportParameter(inputControl.getId(), inputControl.getSelectedValues()));
        }
        return parameters;
    }

    private void updateInputControls(List<InputControlState> stateList) {
        for (InputControlState inputControlState : stateList) {
            for (InputControl inputControl : inputControls) {
                if (inputControlState.getId().equals(inputControl.getId())) {
                    inputControl.setState(inputControlState);
                }
            }
        }
        mAdapter.updateInputControlList(inputControls);
    }

    private void updateInputControlsFromReportParams() {
        ArrayList<ReportParameter> reportParams = paramsStorage.getReportParameters(reportUri);

        Map<String, Set<String>> hashMap = new HashMap<String, Set<String>>(reportParams.size());
        for (ReportParameter reportParameter : reportParams) {
            hashMap.put(reportParameter.getName(), reportParameter.getValues());
        }

        for (InputControl inputControl : inputControls) {
            updateInputControlState(hashMap, inputControl);
        }
    }

    private void updateInputControlState(Map<String, Set<String>> hashMap, InputControl inputControl) {
        InputControlState state = inputControl.getState();
        List<InputControlOption> options = state.getOptions();
        Set<String> valueSet = hashMap.get(state.getId());
        List<String> valueList = new ArrayList<String>();
        if (valueSet != null) {
            valueList.addAll(valueSet);
        }

        if (!valueList.isEmpty()) {
            switch (inputControl.getType()) {
                case bool:
                case singleValueText:
                case singleValueNumber:
                case singleValueTime:
                case singleValueDate:
                case singleValueDatetime:
                    state.setValue(valueList.get(0));
                    break;
                case multiSelect:
                case multiSelectCheckbox:
                case singleSelect:
                case singleSelectRadio:
                    for (InputControlOption option : options) {
                        option.setSelected(valueList.contains(option.getValue()));
                    }
                    break;
            }
        }
    }
//    private void initSingleSelectControl(final InputControl inputControl) {
//        LayoutInflater inflater = getLayoutInflater();
//        View layoutView = inflater.inflate(R.layout.view_ic_single_select, baseLayout, false);
//        updateLabelView(inputControl, layoutView);
//
//        Spinner spinner = (Spinner) layoutView.findViewById(R.id.ic_spinner);
//        spinner.setPrompt(inputControl.getLabel());
//
//        ArrayAdapter<InputControlOption> lovAdapter = new ArrayAdapter<InputControlOption>(InputControlsActivity.this,
//                android.R.layout.simple_spinner_item, inputControl.getState().getOptions());
//        lovAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(lovAdapter);
//
//        // set initial value for spinner
//        for (InputControlOption option : inputControl.getState().getOptions()) {
//            if (option.isSelected()) {
//                int position = lovAdapter.getPosition(option);
//                spinner.setSelection(position, false);
//            }
//        }
//
//        // add listener
//        if (inputControl.isReadOnly()) {
//            spinner.setEnabled(false);
//        } else {
//            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    for (InputControlOption option : inputControl.getState().getOptions()) {
//                        if (option.equals(parent.getSelectedItem())) {
//                            if (!option.isSelected()) {
//                                option.setSelected(true);
//                                updateDependentControls(inputControl);
//                                hideValidationMessage(inputControl);
//                            }
//                        } else {
//                            option.setSelected(false);
//                        }
//                    }
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> parent) { /* Do nothing */ }
//            });
//        }
//
//        assignViews(inputControl, layoutView, spinner);
//        baseLayout.addView(layoutView);
//    }
//
//    private void initMultiSelectControl(final InputControl inputControl) {
//        LayoutInflater inflater = getLayoutInflater();
//        View layoutView = inflater.inflate(R.layout.view_ic_multi_select, baseLayout, false);
//        updateLabelView(inputControl, layoutView);
//
//        MultiSelectSpinner<InputControlOption> multiSpinner =
//                (MultiSelectSpinner<InputControlOption>) layoutView.findViewById(R.id.ic_multi_spinner);
//        multiSpinner.setPrompt(inputControl.getLabel());
//        // init values
//        multiSpinner.setItemsList(inputControl.getState().getOptions(), InputControlWrapper.NOTHING_SUBSTITUTE_LABEL);
//
//        // set selected values
//        List<Integer> positions = new ArrayList<Integer>();
//        for (InputControlOption option : inputControl.getState().getOptions()) {
//            if (option.isSelected()) {
//                positions.add(multiSpinner.getItemPosition(option));
//            }
//        }
//        multiSpinner.setSelection(positions);
//
//        // add listener
//        if (inputControl.isReadOnly()) {
//            multiSpinner.setEnabled(false);
//        } else {
//            multiSpinner.setOnItemsSelectedListener(new MultiSelectSpinner.OnItemsSelectedListener() {
//                @Override
//                public void onItemsSelected(List selectedItems) {
//                    boolean valuesChanged = false;
//                    // update selected values
//                    for (InputControlOption option : inputControl.getState().getOptions()) {
//                        boolean selectedBefore = option.isSelected();
//                        boolean selectedNow = selectedItems.contains(option);
//                        if (selectedBefore != selectedNow) valuesChanged = true;
//                        option.setSelected(selectedNow);
//                    }
//                    // update dependent controls if exist
//                    if (valuesChanged) {
//                        updateDependentControls(inputControl);
//                        hideValidationMessage(inputControl);
//                    }
//                }
//            });
//        }
//
//        assignViews(inputControl, layoutView, multiSpinner);
//        baseLayout.addView(layoutView);
//    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class GetInputControlsValuesListener extends SimpleRequestListener<InputControlStatesList> {

        @Override
        protected Context getContext() {
            return InputControlsActivity.this;
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            super.onRequestFailure(exception);
            setRefreshActionButtonState(false);
        }

        @Override
        public void onRequestSuccess(InputControlStatesList stateList) {
            updateInputControls(stateList.getInputControlStates());
            setRefreshActionButtonState(false);
        }
    }

    private class ValidateInputControlsValuesListener extends SimpleRequestListener<InputControlStatesList> {

        @Override
        protected Context getContext() {
            return InputControlsActivity.this;
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            super.onRequestFailure(exception);
            setRefreshActionButtonState(false);
        }

        @Override
        public void onRequestSuccess(InputControlStatesList stateList) {
            List<InputControlState> invalidStateList = stateList.getInputControlStates();
            if (invalidStateList.isEmpty()) {
                runReport();
            } else {
                updateInputControls(invalidStateList);
            }
            setRefreshActionButtonState(false);
        }
    }

}