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

package com.jaspersoft.android.jaspermobile.util;

import com.jaspersoft.android.sdk.client.oxm.report.option.ReportOption;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class ReportOptionHolder {
    private ReportOption mReportOption;
    private Integer mHashCode;
    private boolean mSelected;

    public ReportOptionHolder(ReportOption mReportOption, Integer mHashCode) {
        this.mHashCode = mHashCode;
        this.mReportOption = mReportOption;
    }

    public Integer getHashCode() {
        return mHashCode;
    }

    public void setHashCode(Integer hashCode) {
        this.mHashCode = hashCode;
    }

    public ReportOption getReportOption() {
        return mReportOption;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        this.mSelected = selected;
    }
}
