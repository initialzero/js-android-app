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

package com.jaspersoft.android.jaspermobile.ui.page;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ReportPageState implements Parcelable {
    private boolean mSessionExpired;
    private boolean mControlsPageShown;
    private boolean mNeedControls;
    private Integer mTotalPages;
    private String mCurrentPage;
    private String mRequestedPage;

    public void setControlsPageShown(boolean loaded) {
        mControlsPageShown = loaded;
    }

    public boolean isControlsPageShown() {
        return mControlsPageShown;
    }

    public String getCurrentPage() {
        return mCurrentPage;
    }

    public void setCurrentPage(String currentPage) {
        mCurrentPage = currentPage;
    }

    public String getRequestedPage() {
        return mRequestedPage;
    }

    public void setRequestedPage(String requestedPage) {
        mRequestedPage = requestedPage;
    }

    public void setSessionExpired(boolean sessionExpired) {
        mSessionExpired = sessionExpired;
    }

    public void setHasControls(boolean needControls) {
        mNeedControls = needControls;
    }

    public boolean hasControls() {
        return mNeedControls;
    }

    public boolean isSessionExpired() {
        return mSessionExpired;
    }

    public Integer getTotalPages() {
        return mTotalPages;
    }

    public void setTotalPages(Integer totalPages) {
        mTotalPages = totalPages;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(mSessionExpired ? (byte) 1 : (byte) 0);
        dest.writeByte(mControlsPageShown ? (byte) 1 : (byte) 0);
        dest.writeByte(mNeedControls ? (byte) 1 : (byte) 0);
        dest.writeValue(this.mTotalPages);
        dest.writeString(this.mCurrentPage);
        dest.writeString(this.mRequestedPage);
    }

    public ReportPageState() {
    }

    protected ReportPageState(Parcel in) {
        this.mSessionExpired = in.readByte() != 0;
        this.mControlsPageShown = in.readByte() != 0;
        this.mNeedControls = in.readByte() != 0;
        this.mTotalPages = (Integer) in.readValue(Integer.class.getClassLoader());
        this.mCurrentPage = in.readString();
        this.mRequestedPage = in.readString();
    }

    public static final Creator<ReportPageState> CREATOR = new Creator<ReportPageState>() {
        public ReportPageState createFromParcel(Parcel source) {
            return new ReportPageState(source);
        }

        public ReportPageState[] newArray(int size) {
            return new ReportPageState[size];
        }
    };
}
