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

import com.jaspersoft.android.sdk.service.data.schedule.JobOutputFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class ScheduleViewModel implements Serializable {
    private String mJobName;
    private String mFileName;
    private String mOutputPath;
    private Calendar mDate;
    private ArrayList<JobOutputFormat> mJobOutputFormats;

    public ScheduleViewModel(String jobName, String fileName, String outputPath, Calendar date, ArrayList<JobOutputFormat> jobOutputFormats) {
        this.mJobName = jobName;
        this.mFileName = fileName;
        this.mOutputPath = outputPath;
        this.mDate = date;
        this.mJobOutputFormats = jobOutputFormats;
    }

    public String getJobName() {
        return mJobName;
    }

    public void setJobName(String jobName) {
        this.mJobName = jobName;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    public String getOutputPath() {
        return mOutputPath;
    }

    public void setOutputPath(String outputPath) {
        this.mOutputPath = outputPath;
    }

    public Calendar getDate() {
        return mDate;
    }

    public void setDate(Calendar date) {
        this.mDate = date;
    }

    public ArrayList<JobOutputFormat> getJobOutputFormats() {
        return mJobOutputFormats;
    }

    public void setJobOutputFormats(ArrayList<JobOutputFormat> jobOutputFormats) {
        this.mJobOutputFormats = jobOutputFormats;
    }

    @Override
    public ScheduleViewModel clone() {
        return new ScheduleViewModel(mJobName, mFileName, mOutputPath, mDate, mJobOutputFormats);
    }
}
