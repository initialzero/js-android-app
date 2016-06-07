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
public abstract class CalendarViewRecurrence extends JobFormViewEntity.Recurrence {
    @NonNull
    public abstract String localizedLabel();

    @NonNull
    public abstract List<Month> months();

    @Nullable
    public abstract List<Day> daysInWeek();

    @Nullable
    public abstract String daysInMonth();

    @NonNull
    public abstract String hours();

    @NonNull
    public abstract String minutes();

    @Nullable
    public abstract Date endDate();

    @NonNull
    public final String monthsAsString() {
        List<Month> months = months();
        if (months.isEmpty()) {
            return "---";
        } else {
            return TextUtils.join(", ", months);
        }
    }

    @NonNull
    public String daysInWeekAsString() {
        List<Day> days = daysInWeek();
        if (days == null || days.isEmpty()) {
            return "---";
        } else {
            return TextUtils.join(", ", days);
        }
    }

    @NonNull
    public Builder newBuilder() {
        return new AutoValue_CalendarViewRecurrence.Builder(this);
    }

    @NonNull
    public static Builder builder() {
        return new AutoValue_CalendarViewRecurrence.Builder();
    }

    @NonNull
    public final Calendar endDateAsCalendar() {
        Calendar instance = Calendar.getInstance();
        Date date = endDate();
        if (date != null) {
            instance.setTime(date);
        }
        return instance;
    }

    @AutoValue
    public static abstract class Day implements Parcelable {
        @NonNull
        public abstract String localizedLabel();

        public abstract int rawValue();

        @Override
        public String toString() {
            return localizedLabel();
        }

        @NonNull
        public static Day create(@NonNull String localizedLabel, int raw) {
            return new AutoValue_CalendarViewRecurrence_Day(localizedLabel, raw);
        }
    }

    @AutoValue
    public static abstract class Month implements Parcelable {
        @NonNull
        public abstract String localizedLabel();

        public abstract int rawValue();

        @Override
        public String toString() {
            return localizedLabel();
        }

        @NonNull
        public static Month create(@NonNull String localizedLabel, int raw) {
            return new AutoValue_CalendarViewRecurrence_Month(localizedLabel, raw);
        }
    }

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder localizedLabel(@NonNull String minutes);

        public abstract Builder months(@NonNull List<Month> months);

        public abstract Builder daysInWeek(@Nullable List<Day> daysInWeek);

        public abstract Builder daysInMonth(@Nullable String daysInMonth);

        public abstract Builder hours(@NonNull String hours);

        public abstract Builder minutes(@NonNull String minutes);

        public abstract Builder endDate(@Nullable Date endDate);

        public abstract CalendarViewRecurrence build();
    }
}
