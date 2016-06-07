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
public class BasePageState implements Parcelable {
    private boolean mShouldExit;

    public void setShouldExit(boolean shouldExit) {
        mShouldExit = shouldExit;
    }

    public boolean shouldExit() {
        return mShouldExit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(mShouldExit ? (byte) 1 : (byte) 0);
    }

    public BasePageState() {
    }

    protected BasePageState(Parcel in) {
        this.mShouldExit = in.readByte() != 0;
    }

    public static final Parcelable.Creator<BasePageState> CREATOR = new Parcelable.Creator<BasePageState>() {
        public BasePageState createFromParcel(Parcel source) {
            return new BasePageState(source);
        }

        public BasePageState[] newArray(int size) {
            return new BasePageState[size];
        }
    };
}
