/*
 * Copyright (C) 2015 TIBCO Jaspersoft Corporation. All rights reserved.
 * http://community.jaspersoft.com/project/mobile-sdk-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile SDK for Android.
 *
 * TIBCO Jaspersoft Mobile SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile SDK for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.sdk.client.oxm.control.validation;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

/**
 * @author Ivan Gadzhega
 * @since 1.4
 */
public class ValidationRule implements Parcelable {

    @Expose
    private String errorMessage;

    public ValidationRule() {}

    //---------------------------------------------------------------------
    // Parcelable
    //---------------------------------------------------------------------

    //---------------------------------------------------------------------
    // Getters & Setters
    //---------------------------------------------------------------------

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.errorMessage);
    }

    protected ValidationRule(Parcel in) {
        this.errorMessage = in.readString();
    }

    public static final Creator<ValidationRule> CREATOR = new Creator<ValidationRule>() {
        public ValidationRule createFromParcel(Parcel source) {
            return new ValidationRule(source);
        }

        public ValidationRule[] newArray(int size) {
            return new ValidationRule[size];
        }
    };
}
