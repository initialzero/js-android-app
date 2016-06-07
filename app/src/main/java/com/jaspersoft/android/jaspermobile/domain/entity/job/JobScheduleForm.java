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
import com.jaspersoft.android.sdk.service.data.schedule.JobAlert;
import com.jaspersoft.android.sdk.service.data.schedule.JobMailNotification;
import com.jaspersoft.android.sdk.service.data.schedule.JobSource;
import com.jaspersoft.android.sdk.service.data.schedule.RepositoryDestination;

import java.util.Date;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@AutoValue
public abstract class JobScheduleForm {
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
    public abstract List<OutputFormat> outputFormats();

    @Nullable
    public abstract Date startDate();

    @NonNull
    public abstract Recurrence recurrence();

    /**
     * Quick solution to skip unnecessary mapping for not supported API
     *
     * @return raw destination DTO from SDK
     */
    @Nullable
    public abstract RepositoryDestination rawDestination();

    /**
     * Quick solution to skip unnecessary mapping for not supported API
     *
     * @return raw destination DTO from SDK
     */
    @Nullable
    public abstract JobMailNotification rawMailNotification();

    /**
     * Quick solution to skip unnecessary mapping for not supported API
     *
     * @return raw destination DTO from SDK
     */
    @Nullable
    public abstract JobAlert rawAlert();

    /**
     * Quick solution to skip unnecessary mapping for not supported API
     *
     * @return raw destination DTO from SDK
     */
    @Nullable
    public abstract JobSource rawSource();

    @NonNull
    public final Builder newBuilder() {
        return new AutoValue_JobScheduleForm.Builder(this);
    }

    @NonNull
    public static Builder builder() {
        return new AutoValue_JobScheduleForm.Builder();
    }

    public enum OutputFormat {
        PDF, HTML, XLS, RTF, CSV, ODT, TXT, DOCX, ODS, XLSX, XLS_NOPAG, XLSX_NOPAG, DATA_SNAPSHOT, PPTX
    }

    public interface Recurrence {
    }

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder id(int id);

        public abstract Builder version(int version);

        public abstract Builder source(@NonNull String source);

        public abstract Builder jobName(@NonNull String jobName);

        public abstract Builder description(@Nullable String description);

        public abstract Builder fileName(@NonNull String fileName);

        public abstract Builder folderUri(@NonNull String outputPath);

        public abstract Builder outputFormats(@NonNull List<OutputFormat> formats);

        public abstract Builder startDate(@Nullable Date startDate);

        public abstract Builder recurrence(@NonNull Recurrence recurrence);

        public abstract Builder rawDestination(@NonNull RepositoryDestination destination);

        public abstract Builder rawMailNotification(@Nullable JobMailNotification mailNotification);

        public abstract Builder rawAlert(@Nullable JobAlert mailNotification);

        public abstract Builder rawSource(@Nullable JobSource source);

        public abstract JobScheduleForm build();
    }
}
