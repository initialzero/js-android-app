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

package com.jaspersoft.android.sdk.client.oxm.control;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Gadzhega
 * @since 1.4
 */
public class InputControlState implements Parcelable {

    @Expose
    private String id;

    @Expose
    private String uri;

    @Expose
    private String value;

    @Expose
    private String error;

    @Expose
    private List<InputControlOption> options = new ArrayList<InputControlOption>();

    public InputControlState() {}

    //---------------------------------------------------------------------
    // Parcelable
    //---------------------------------------------------------------------

    public InputControlState(Parcel source) {
        this.id = source.readString();
        this.uri = source.readString();
        this.value = source.readString();
        this.error = source.readString();
        this.options = source.createTypedArrayList(InputControlOption.CREATOR);
    }

    public static final Creator CREATOR = new Creator() {
        public InputControlState createFromParcel(Parcel source) {
            return new InputControlState(source);
        }

        public InputControlState[] newArray(int size) {
            return new InputControlState[size];
        }
    };

    @Override
    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(uri);
        dest.writeString(value);
        dest.writeString(error);
        dest.writeTypedList(options);
    }

    //---------------------------------------------------------------------
    // Getters & Setters
    //---------------------------------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<InputControlOption> getOptions() {
        return options;
    }

    public void setOptions(List<InputControlOption> options) {
        this.options = options;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (options != null ? options.hashCode() : 0);
        return result;
    }
}
