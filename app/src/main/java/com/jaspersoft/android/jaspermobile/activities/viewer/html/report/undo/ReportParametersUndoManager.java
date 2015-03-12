/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.undo;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Queue;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class ReportParametersUndoManager extends UndoManager<String> implements Parcelable {

    public ReportParametersUndoManager() {
    }

    private ReportParametersUndoManager(Parcel in) {
        String[] array = (String[]) in.readSerializable();
        getUndoStack().addAll(Arrays.asList(array));
    }

    @NonNull
    @Override
    public String undo() {
        String value = super.undo();
        return TextUtils.isEmpty(value) ? "{}" : value;
    }

    @NonNull
    @Override
    public String peekLatest() {
        String value = super.peekLatest();
        return TextUtils.isEmpty(value) ? "{}" : value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Queue<String> stack = getUndoStack();
        String[] array = new String[stack.size()];
        stack.toArray(array);
        dest.writeSerializable(array);
    }

    public static final Creator<ReportParametersUndoManager> CREATOR = new Creator<ReportParametersUndoManager>() {
        public ReportParametersUndoManager createFromParcel(Parcel source) {
            return new ReportParametersUndoManager(source);
        }

        public ReportParametersUndoManager[] newArray(int size) {
            return new ReportParametersUndoManager[size];
        }
    };
}
