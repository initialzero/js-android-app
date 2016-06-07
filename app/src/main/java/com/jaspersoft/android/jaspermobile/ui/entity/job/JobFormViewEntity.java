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
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.auto.value.AutoValue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@AutoValue
public abstract class JobFormViewEntity implements Parcelable {
    public abstract int id();

    public abstract int version();

    @NonNull
    public abstract String source();

    @NonNull
    public abstract String jobName();

    @Nullable
    public abstract String description();

    @NonNull
    public abstract String fileName();

    @NonNull
    public abstract String folderUri();

    @NonNull
    public abstract List<OutputFormat>  outputFormats();

    @Nullable
    public abstract Date startDate();

    @NonNull
    public final Calendar startDateAsCalendar() {
        Calendar calendar = Calendar.getInstance();
        if (hasStartDate()) {
            calendar.setTime(startDate());
        }
        return calendar;
    }

    @NonNull
    public final String outputFormatsAsString() {
        if (outputFormats().isEmpty()) {
            return "---";
        } else {
            return TextUtils.join(", ", outputFormats());
        }
    }

    @NonNull
    public abstract Recurrence recurrence();

    public final boolean hasStartDate() {
        return startDate() != null;
    }

    @NonNull
    public Builder newBuilder() {
        return new AutoValue_JobFormViewEntity.Builder(this);
    }

    @NonNull
    public static Builder builder() {
        return new AutoValue_JobFormViewEntity.Builder();
    }

    @AutoValue
    public static abstract class OutputFormat implements Parcelable {
        public abstract String rawValue();

        public abstract String label();

        @Override
        public String toString() {
            return label();
        }

        @NonNull
        public static OutputFormat create(@NonNull String rawValue, @NonNull String label) {
            return new AutoValue_JobFormViewEntity_OutputFormat(rawValue, label);
        }
    }

    public static abstract class Recurrence implements Parcelable {
        @NonNull
        public abstract String localizedLabel();

        @Override
        public String toString() {
            return localizedLabel();
        }
    }

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder id(int id);

        public abstract Builder version(int version);

        public abstract Builder source(@NonNull String source);

        public abstract Builder description(@Nullable String description);

        public abstract Builder jobName(@NonNull String jobName);

        public abstract Builder fileName(@NonNull String fileName);

        public abstract Builder folderUri(@NonNull String outputPath);

        public abstract Builder startDate(@Nullable Date startDate);

        public abstract Builder recurrence(@NonNull Recurrence recurrence);

        public abstract Builder outputFormats(@NonNull List<OutputFormat> formats);

        public abstract JobFormViewEntity build();
    }
}
