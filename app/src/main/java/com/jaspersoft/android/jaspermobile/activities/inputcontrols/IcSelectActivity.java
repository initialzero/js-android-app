package com.jaspersoft.android.jaspermobile.activities.inputcontrols;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EActivity(R.layout.report_options_layout)
public class IcSelectActivity extends RoboToolbarActivity {

    public static final int MULTI_SELECT = 0;
    public static final int SINGLE_SELECT = 1;

    public static final String SELECT_IC_ARG = "select_input_control_id";

    @Inject
    protected ReportParamsStorage paramsStorage;

    @Extra
    protected String reportUri;

    @Extra
    protected String inputControlId;

    @Extra
    protected int listType;

    @ViewById(R.id.inputControlsList)
    protected RecyclerView inputControlsList;

    private List<InputControlOption> mInputControlOptions;
    private String mInputControlLabel;

    @AfterViews
    protected void init() {
        initInputControlOptions();
        showInputControlOptions();

        getSupportActionBar().setTitle(mInputControlLabel);
    }

    @Override
    public void onBackPressed() {
        Intent dataIntent = new Intent();
        dataIntent.putExtra(SELECT_IC_ARG, inputControlId);
        setResult(Activity.RESULT_OK, dataIntent);

        super.onBackPressed();
    }

    private void showInputControlOptions() {
        RecyclerView.Adapter adapter;
        if (listType == SINGLE_SELECT) {
            adapter = new SingleSelectIcAdapter(mInputControlOptions);
        } else {
            adapter = new MultiSelectIcAdapter(mInputControlOptions);
        }
        inputControlsList.setLayoutManager(new LinearLayoutManager(this));
        inputControlsList.setAdapter(adapter);
    }

    private void initInputControlOptions(){
        ArrayList<InputControl> inputControls = paramsStorage.getInputControls(reportUri);
        for (InputControl inputControl : inputControls) {
            if (inputControl.getId().equals(inputControlId)) {
                mInputControlLabel = inputControl.getLabel();
                mInputControlOptions = inputControl.getState().getOptions();
                break;
            }
        }
    }
}
