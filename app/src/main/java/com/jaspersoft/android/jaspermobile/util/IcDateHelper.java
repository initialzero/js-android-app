/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util;

import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.validation.DateTimeFormatValidationRule;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * @author Ivan Gadzhega
 * @since 1.6
 */
public class IcDateHelper {

    public static Calendar convertToDate(InputControl inputControl) {
        String selectedValue = inputControl.getState().getValue();
         if (selectedValue != null) {
            try {
                Date date = parseServerDateFormat(inputControl).parse(selectedValue);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                return calendar;
            } catch (ParseException e) {
                Timber.e("Can not parse date: %s", selectedValue);
                return null;
            }
        }
        return null;
    }

    public static String convertToString(InputControl inputControl, Calendar date) {
        if (date != null) {
            return parseServerDateFormat(inputControl).format(date.getTime());
        }
        return null;
    }

    private static DateFormat parseServerDateFormat(InputControl inputControl) {
        DateFormat dateFormat = null;

        for (DateTimeFormatValidationRule validationRule : inputControl.getValidationRules(DateTimeFormatValidationRule.class)) {
            String serverDateFormat = validationRule.getFormat();
            dateFormat = new SimpleDateFormat(serverDateFormat, Locale.US);
        }
        return dateFormat;
    }

}