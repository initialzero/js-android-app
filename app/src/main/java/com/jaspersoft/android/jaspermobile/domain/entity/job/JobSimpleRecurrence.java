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

/**
 * @author Tom Koptel
 * @since 2.5
 */
@AutoValue
public abstract class JobSimpleRecurrence implements JobScheduleForm.Recurrence {

    public abstract int interval();

    @Nullable
    public abstract Integer occurrence();

    @NonNull
    public abstract Unit unit();

    @Nullable
    public abstract Date untilDate();

    @NonNull
    public final Builder newBuilder() {
        return new AutoValue_JobSimpleRecurrence.Builder(this);
    }

    @NonNull
    public static Builder builder() {
        return new AutoValue_JobSimpleRecurrence.Builder();
    }

    public enum Unit {
        MINUTE, HOUR, DAY, WEEK
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder interval(int interval);

        public abstract Builder unit(@NonNull Unit unit);

        public abstract Builder occurrence(@Nullable Integer occurrence);

        public abstract Builder untilDate(@Nullable Date date);

        public abstract JobSimpleRecurrence build();
    }
}
