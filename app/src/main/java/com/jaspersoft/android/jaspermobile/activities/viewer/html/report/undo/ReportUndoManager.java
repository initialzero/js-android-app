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

import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.model.ReportModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class ReportUndoManager extends UndoManager<ReportModel> implements Parcelable {

    public ReportUndoManager() {
    }

    @NonNull
    @Override
    public ReportModel undo() {
        ReportModel value = super.undo();
        if (value == null) {
            throw new IllegalStateException("Manager should contain any command before calling undo()");
        }
        return value;
    }

    @NonNull
    @Override
    public ReportModel peekLast() {
        ReportModel value = super.peekLast();
        if (value == null) {
            throw new IllegalStateException("Manager should contain any command before calling peekLast()");
        }
        return value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Queue<ReportModel> stack = getUndoStack();
        dest.writeTypedList(new ArrayList<Parcelable>(stack));
    }

    private ReportUndoManager(Parcel in) {
        List<ReportModel> reportModelList = new ArrayList<ReportModel>();
        in.readTypedList(reportModelList, ReportModel.CREATOR);
        getUndoStack().addAll(reportModelList);
    }

    public static final Creator<ReportUndoManager> CREATOR = new Creator<ReportUndoManager>() {
        public ReportUndoManager createFromParcel(Parcel source) {
            return new ReportUndoManager(source);
        }

        public ReportUndoManager[] newArray(int size) {
            return new ReportUndoManager[size];
        }
    };

}
