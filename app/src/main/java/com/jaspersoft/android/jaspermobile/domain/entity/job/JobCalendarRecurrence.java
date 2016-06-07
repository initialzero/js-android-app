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
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.util.Date;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@AutoValue
public abstract class JobCalendarRecurrence implements JobScheduleForm.Recurrence {
    @NonNull
    public abstract List<Integer> months();

    @Nullable
    public abstract List<Integer> daysInWeek();

    @Nullable
    public abstract String daysInMonth();

    @NonNull
    public abstract String hours();

    @NonNull
    public abstract String minutes();

    @Nullable
    public abstract Date endDate();

    @NonNull
    public Builder newBuilder() {
        return new AutoValue_JobCalendarRecurrence.Builder(this);
    }

    @NonNull
    public static Builder builder() {
        return new AutoValue_JobCalendarRecurrence.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder months(@NonNull List<Integer> months);

        public abstract Builder daysInWeek(@Nullable List<Integer> daysInWeek);

        public abstract Builder daysInMonth(@Nullable String daysInMonth);

        public abstract Builder hours(@NonNull String hours);

        public abstract Builder minutes(@NonNull String minutes);

        public abstract Builder endDate(@Nullable Date endDate);

        public abstract JobCalendarRecurrence build();
    }
}
