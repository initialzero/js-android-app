package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params;

import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.util.List;

public interface InputControlsSerializer {
    String toJson(List<InputControl> controls);
}
