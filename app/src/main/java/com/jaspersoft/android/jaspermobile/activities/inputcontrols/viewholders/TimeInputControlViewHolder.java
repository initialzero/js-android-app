package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.view.View;

import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class TimeInputControlViewHolder extends DateTimeInputControlViewHolder {

    private static final String DEFAULT_DATE_TIME_FORMAT = "HH:mm:ss";

    public TimeInputControlViewHolder(View itemView) {
        super(itemView);

        mUserDateFormat = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT, Locale.getDefault());
    }

    @Override
    public void populateView(InputControl inputControl, boolean enabled) {
        super.populateView(inputControl, enabled);

        btnDate.setVisibility(View.GONE);
        dateTimeDivider.setVisibility(View.GONE);
    }
}
