package com.jaspersoft.android.jaspermobile.presentation.page;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class NavigationPageState implements Parcelable {
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

    public NavigationPageState() {
    }

    protected NavigationPageState(Parcel in) {
        this.mShouldExit = in.readByte() != 0;
    }

    public static final Parcelable.Creator<NavigationPageState> CREATOR = new Parcelable.Creator<NavigationPageState>() {
        public NavigationPageState createFromParcel(Parcel source) {
            return new NavigationPageState(source);
        }

        public NavigationPageState[] newArray(int size) {
            return new NavigationPageState[size];
        }
    };
}
