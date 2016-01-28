package com.jaspersoft.android.jaspermobile.presentation.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ReportResourceModel implements Parcelable {
    private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @NonNull
    private final String mLabel;
    @Nullable
    private final String mDescription;
    @NonNull
    private final String mUri;
    @NonNull
    private final Date mCreationDate;

    private ReportResourceModel(
            @NonNull String label,
            @NonNull String description,
            @NonNull String uri,
            @NonNull Date creationDate
    ) {
        mDescription = description;
        mLabel = label;
        mUri = uri;
        mCreationDate = creationDate;
    }

    @Nullable
    public String getDescription() {
        return mDescription;
    }

    @NonNull
    public String getLabel() {
        return mLabel;
    }

    @NonNull
    public String getUri() {
        return mUri;
    }

    @NonNull
    public Date getCreationDate() {
        return mCreationDate;
    }

    @NonNull
    public String getCreationDateAsString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                DEFAULT_DATE_TIME_FORMAT, Locale.getDefault());
        return simpleDateFormat.format(mCreationDate);
    }

    public static class Builder {
        private String mLabel;
        private String mDescription;
        private String mUri;
        private Date mCreationDate;

        public Builder setLabel(@NonNull String label) {
            mLabel = label;
            return this;
        }

        public Builder setDescription(@NonNull String description) {
            mDescription = description;
            return this;
        }

        public Builder setUri(@NonNull String uri) {
            mUri = uri;
            return this;
        }

        public Builder setCreationDate(@NonNull Date creationDate) {
            mCreationDate = creationDate;
            return this;
        }

        public ReportResourceModel build() {
            return new ReportResourceModel(mLabel, mDescription, mUri, mCreationDate);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mLabel);
        dest.writeString(this.mDescription);
        dest.writeString(this.mUri);
        dest.writeLong(mCreationDate != null ? mCreationDate.getTime() : -1);
    }

    protected ReportResourceModel(Parcel in) {
        this.mLabel = in.readString();
        this.mDescription = in.readString();
        this.mUri = in.readString();
        long tmpMCreationDate = in.readLong();
        this.mCreationDate = tmpMCreationDate == -1 ? null : new Date(tmpMCreationDate);
    }

    public static final Creator<ReportResourceModel> CREATOR = new Creator<ReportResourceModel>() {
        public ReportResourceModel createFromParcel(Parcel source) {
            return new ReportResourceModel(source);
        }

        public ReportResourceModel[] newArray(int size) {
            return new ReportResourceModel[size];
        }
    };
}
