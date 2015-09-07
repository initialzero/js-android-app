package com.jaspersoft.android.jaspermobile.activities.inputcontrols;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EActivity
public class SingleSelectInputControlsActivity extends RoboToolbarActivity {

    @Inject
    protected ReportParamsStorage paramsStorage;

    @Extra
    protected String reportUri;

    @Extra
    protected String inputControlId;

    private List<InputControlOption> mInputControlOptions;

    @AfterViews
    protected void init() {
        initInputControlOptions();
        showInputControlOptions();
    }

    private void showInputControlOptions() {

    }

    private void initInputControlOptions(){
        ArrayList<InputControl> inputControls = paramsStorage.getInputControls(reportUri);
        for (InputControl inputControl : inputControls) {
            if (inputControl.getId().equals(inputControlId)) {
                mInputControlOptions = inputControl.getState().getOptions();
                break;
            }
        }
    }
}
