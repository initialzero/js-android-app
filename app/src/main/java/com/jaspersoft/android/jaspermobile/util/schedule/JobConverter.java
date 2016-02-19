/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util.schedule;

import com.jaspersoft.android.sdk.service.data.schedule.JobForm;
import com.jaspersoft.android.sdk.service.data.schedule.JobSource;
import com.jaspersoft.android.sdk.service.data.schedule.RepositoryDestination;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class JobConverter {

    public static JobForm toJobForm(JobForm jobForm, ScheduleViewModel scheduleViewModel) {
        JobForm.Builder jobFormBuilder = jobForm.newBuilder()
                .withLabel(scheduleViewModel.getJobName())
                .withBaseOutputFilename(scheduleViewModel.getFileName())
                .withRepositoryDestination(new RepositoryDestination.Builder().withFolderUri(scheduleViewModel.getOutputPath()).build())
                .withOutputFormats(scheduleViewModel.getJobOutputFormats());

        if (scheduleViewModel.getDate() != null) {
            jobFormBuilder.withStartDate(scheduleViewModel.getDate().getTime());
        }

        return jobFormBuilder.build();
    }

    public static ScheduleViewModel toJobViewModel(JobForm jobForm) {
        Calendar startDate = Calendar.getInstance();
        if (jobForm.getStartDate() != null) {
            startDate.setTime(jobForm.getStartDate());
        } else {
            startDate = null;
        }
        return new ScheduleViewModel(jobForm.getLabel(), jobForm.getBaseOutputFilename(), jobForm.getRepositoryDestination().getFolderUri(),
                startDate, new ArrayList<>(jobForm.getOutputFormats()));
    }
}
