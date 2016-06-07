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

package com.jaspersoft.android.jaspermobile.domain;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class Report {

    @NonNull
    private final RxReportExecution mReportExecution;
    @NonNull
    private final String mReportUri;

    @Nullable
    private Integer mTotalPages;
    @Nullable
    private Boolean mMultiPage;

    public Report(@NonNull RxReportExecution reportExecution, @NonNull String reportUri) {
        mReportExecution = reportExecution;
        mReportUri = reportUri;
    }

    public RxReportExecution getExecution() {
        return mReportExecution;
    }

    public void setMultiPage(boolean multiPage) {
        mMultiPage = multiPage;
    }

    public void setTotalPages(int totalPages) {
        mTotalPages = totalPages;
    }

    @NonNull
    public String getReportUri() {
        return mReportUri;
    }

    @Nullable
    public Boolean getMultiPage() {
        return mMultiPage;
    }

    @Nullable
    public Integer getTotalPages() {
        return mTotalPages;
    }
}
