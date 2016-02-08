package com.jaspersoft.android.jaspermobile.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

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
    @NonNull
    private final String mPageRange;
    @NonNull
    private final File mFile;

    public ExportBundle(
            @NonNull String uri,
            @NonNull String label,
            @NonNull String description,
            @NonNull String format,
            @NonNull String pageRange,
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

    @NonNull
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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExportBundle)) return false;

        ExportBundle bundle = (ExportBundle) o;

        if (!mUri.equals(bundle.mUri)) return false;
        if (!mLabel.equals(bundle.mLabel)) return false;
        if (!mDescription.equals(bundle.mDescription)) return false;
        if (!mFormat.equals(bundle.mFormat)) return false;
        if (!mPageRange.equals(bundle.mPageRange)) return false;
        return mFile.equals(bundle.mFile);

    }

    @Override
    public final int hashCode() {
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
            if (mPageRange == null) {
                throw new NullPointerException("Page range should not be null");
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
