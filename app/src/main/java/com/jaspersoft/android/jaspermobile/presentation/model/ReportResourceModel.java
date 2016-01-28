package com.jaspersoft.android.jaspermobile.presentation.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ReportResourceModel implements Parcelable {
    @NonNull
    private final String mLabel;
    @Nullable
    private final String mDescription;
    @NonNull
    private final String mUri;

    private ReportResourceModel(
            @NonNull String label,
            @NonNull String description,
            @NonNull String uri
    ) {
        mDescription = description;
        mLabel = label;
        mUri = uri;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mLabel);
        dest.writeString(this.mDescription);
        dest.writeString(this.mUri);
    }

    protected ReportResourceModel(Parcel in) {
        this.mLabel = in.readString();
        this.mDescription = in.readString();
        this.mUri = in.readString();
    }

    public static final Parcelable.Creator<ReportResourceModel> CREATOR = new Parcelable.Creator<ReportResourceModel>() {
        public ReportResourceModel createFromParcel(Parcel source) {
            return new ReportResourceModel(source);
        }

        public ReportResourceModel[] newArray(int size) {
            return new ReportResourceModel[size];
        }
    };

    public static class Builder {
        private String mLabel;
        private String mDescription;
        private String mUri;

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

        public ReportResourceModel build() {
            return new ReportResourceModel(mLabel, mDescription, mUri);
        }
    }
}
