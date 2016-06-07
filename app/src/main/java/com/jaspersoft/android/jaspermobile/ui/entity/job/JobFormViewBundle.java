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

package com.jaspersoft.android.jaspermobile.ui.entity.job;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@AutoValue
public abstract class JobFormViewBundle implements Parcelable {
    @NonNull
    public abstract List<JobFormViewEntity.OutputFormat> allFormats();

    @NonNull
    public abstract List<SimpleViewRecurrence.Unit> allIntervalUnits();

    @NonNull
    public abstract List<JobFormViewEntity.Recurrence> allRecurrences();

    @NonNull
    public abstract List<CalendarViewRecurrence.Day> allDays();

    @NonNull
    public abstract List<CalendarViewRecurrence.Month> allMonths();

    @NonNull
    public abstract JobFormViewEntity form();

    @NonNull
    public Builder newBuilder() {
        return new AutoValue_JobFormViewBundle.Builder(this);
    }

    @NonNull
    public static Builder builder() {
        return new AutoValue_JobFormViewBundle.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder allFormats(@NonNull List<JobFormViewEntity.OutputFormat> formats);

        public abstract Builder allIntervalUnits(@NonNull List<SimpleViewRecurrence.Unit> units);

        public abstract Builder allRecurrences(@NonNull List<JobFormViewEntity.Recurrence> units);

        public abstract Builder allDays(@NonNull List<CalendarViewRecurrence.Day> days);

        public abstract Builder allMonths(@NonNull List<CalendarViewRecurrence.Month> months);

        public abstract Builder form(@NonNull JobFormViewEntity form);

        public abstract JobFormViewBundle build();
    }
}
