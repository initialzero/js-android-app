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

package com.jaspersoft.android.jaspermobile.data.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ExportBundle implements Parcelable {
    @NonNull
    private final String mUri;
    @NonNull
    private final String mLabel;
    @NonNull
    private final String mDescription;
    @NonNull
    private final String mFormat;
    @Nullable
    private final String mPageRange;
    @NonNull
    private final File mFile;

    private ExportBundle(
            @NonNull String uri,
            @NonNull String label,
            @NonNull String description,
            @NonNull String format,
            @Nullable String pageRange,
            @NonNull File file) {
        mUri = uri;
        mLabel = label;
        mDescription = description;
        mFormat = format;
        mPageRange = pageRange;
        mFile = file;
    }

    @NonNull
    public String getDescription() {
        return mDescription;
    }

    @NonNull
    public String getFormat() {
        return mFormat;
    }

    @NonNull
    public String getLabel() {
        return mLabel;
    }

    @Nullable
    public String getPageRange() {
        return mPageRange;
    }

    @NonNull
    public String getUri() {
        return mUri;
    }

    @NonNull
    public File getFile() {
        return mFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExportBundle that = (ExportBundle) o;

        if (!mUri.equals(that.mUri)) return false;
        if (!mLabel.equals(that.mLabel)) return false;
        if (!mDescription.equals(that.mDescription)) return false;
        if (!mFormat.equals(that.mFormat)) return false;
        if (!mPageRange.equals(that.mPageRange)) return false;
        return mFile.equals(that.mFile);

    }

    @Override
    public int hashCode() {
        int result = mUri.hashCode();
        result = 31 * result + mLabel.hashCode();
        result = 31 * result + mDescription.hashCode();
        result = 31 * result + mFormat.hashCode();
        result = 31 * result + mPageRange.hashCode();
        result = 31 * result + mFile.hashCode();
        return result;
    }

    public static class Builder {
        private String mUri;
        private String mLabel;
        private String mDescription;
        private String mFormat;
        private String mPageRange;
        private File mFile;

        public Builder setUri(String uri) {
            mUri = uri;
            return this;
        }

        public Builder setLabel(String label) {
            mLabel = label;
            return this;
        }

        public Builder setDescription(String description) {
            mDescription = description;
            return this;
        }

        public Builder setFormat(String format) {
            mFormat = format;
            return this;
        }

        public Builder setPageRange(String pageRange) {
            mPageRange = pageRange;
            return this;
        }

        public Builder setFile(File file) {
            mFile = file;
            return this;
        }

        public ExportBundle build() {
            if (mUri == null) {
                throw new NullPointerException("Uri should not be null");
            }
            if (mLabel == null) {
                throw new NullPointerException("Label should not be null");
            }
            if (mFormat == null) {
                throw new NullPointerException("Format should not be null");
            }
            return new ExportBundle(
                    mUri,
                    mLabel,
                    mDescription,
                    mFormat,
                    mPageRange,
                    mFile
            );
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUri);
        dest.writeString(this.mLabel);
        dest.writeString(this.mDescription);
        dest.writeString(this.mFormat);
        dest.writeString(this.mPageRange);
        dest.writeSerializable(this.mFile);
    }

    protected ExportBundle(Parcel in) {
        this.mUri = in.readString();
        this.mLabel = in.readString();
        this.mDescription = in.readString();
        this.mFormat = in.readString();
        this.mPageRange = in.readString();
        this.mFile = (File) in.readSerializable();
    }

    public static final Creator<ExportBundle> CREATOR = new Creator<ExportBundle>() {
        public ExportBundle createFromParcel(Parcel source) {
            return new ExportBundle(source);
        }

        public ExportBundle[] newArray(int size) {
            return new ExportBundle[size];
        }
    };
}
