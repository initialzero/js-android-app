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

package com.jaspersoft.android.jaspermobile.domain.entity.job;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@AutoValue
public abstract class JobScheduleBundle {

    @NonNull
    public abstract List<JobScheduleForm.OutputFormat> allFormats();

    @NonNull
    public abstract List<JobSimpleRecurrence.Unit> allIntervalUnits();

    @NonNull
    public abstract List<JobScheduleForm.Recurrence> allRecurrences();

    @NonNull
    public abstract List<Integer> allMonths();

    @NonNull
    public abstract List<Integer> allDays();

    @NonNull
    public abstract JobScheduleForm form();

    public final Builder newBuilder() {
        return new AutoValue_JobScheduleBundle.Builder(this);
    }

    public static Builder builder() {
        return new AutoValue_JobScheduleBundle.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder allFormats(@NonNull List<JobScheduleForm.OutputFormat> formats);

        public abstract Builder allIntervalUnits(@NonNull List<JobSimpleRecurrence.Unit> units);

        public abstract Builder allRecurrences(@NonNull List<JobScheduleForm.Recurrence> units);

        public abstract Builder allMonths(@NonNull List<Integer> units);

        public abstract Builder allDays(@NonNull List<Integer> days);

        public abstract Builder form(@NonNull JobScheduleForm months);

        public abstract JobScheduleBundle build();
    }
}
