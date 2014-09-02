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

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.sdk.client.oxm.ResourceParameter;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.validation.DateTimeFormatValidationRule;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Ivan Gadzhega
 * @since 1.6
 */
class DatePickerDialogHelper {

    // date format
    static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    // Dialog IDs
    static final int DATE_DIALOG_ID = 10;
    static final int TIME_DIALOG_ID = 11;

    private TextView activeDateDisplay;
    private Calendar activeDate;
    private InputControlWrapper activeInputControlWrapper;
    private InputControl activeInputControl;

    private Activity activity;

    DatePickerDialogHelper(Activity activity) {
        this.activity = activity;
    }

    Dialog onCreateDialog(int id) {
        if (activeDate != null) {
            switch (id) {
                case DATE_DIALOG_ID:
                    return new DatePickerDialog(activity, dateSetListener, activeDate.get(Calendar.YEAR), activeDate.get(Calendar.MONTH), activeDate.get(Calendar.DAY_OF_MONTH));
                case TIME_DIALOG_ID:
                    return new TimePickerDialog(activity, timeSetListener, activeDate.get(Calendar.HOUR_OF_DAY), activeDate.get(Calendar.MINUTE), true);
            }
        }
        return null;
    }

    void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(activeDate.get(Calendar.YEAR), activeDate.get(Calendar.MONTH), activeDate.get(Calendar.DAY_OF_MONTH));
                break;
            case TIME_DIALOG_ID:
                ((TimePickerDialog) dialog).updateTime(activeDate.get(Calendar.HOUR_OF_DAY), activeDate.get(Calendar.MINUTE));
                break;
        }
    }

    void showDateDialog(InputControlWrapper inputControlWrapper, int id, TextView dateDisplay, Calendar date) {
        activeInputControlWrapper = inputControlWrapper;
        showDateDialog(id, dateDisplay, date);
    }

    void showDateDialog(InputControl inputControl, int id, TextView dateDisplay, Calendar date) {
        activeInputControl = inputControl;
        showDateDialog(id, dateDisplay, date);
    }

    void updateDateDisplay(TextView dateDisplay, Calendar date, boolean showTime) {
        String displayText;
        if(showTime) {
            displayText = DateFormat.getDateTimeInstance().format(date.getTime());
        } else {
            displayText = DateFormat.getDateInstance().format(date.getTime());
        }
        dateDisplay.setText(displayText);

    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void showDateDialog(int id, TextView dateDisplay, Calendar date) {
        activeDateDisplay = dateDisplay;
        activeDate = date;
        activity.showDialog(id);
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if (activeDate != null) {
                activeDate.set(Calendar.YEAR, year);
                activeDate.set(Calendar.MONTH, monthOfYear);
                activeDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDisplayAndValueOnDateSet();
            }
        }
    };

    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (activeDate != null) {
                activeDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                activeDate.set(Calendar.MINUTE, minute);
                updateDisplayAndValueOnDateSet();
            }
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
        }
        unregisterDateDisplay();
    }

    private void unregisterDateDisplay() {
        activeDateDisplay = null;
        activeDate = null;
        activeInputControlWrapper = null;
        activeInputControl = null;
    }

}