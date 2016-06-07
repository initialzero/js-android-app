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

package com.jaspersoft.android.jaspermobile.ui.view.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public abstract class WidgetStateDelegate<State extends Parcelable> {
    private final Class<State> type;
    private State state;

    public WidgetStateDelegate(Class<State> type) {
        this.type = type;
    }

    public abstract State provideState();

    @Nullable
    public State retrieveState() {
        return state;
    }

    public Parcelable onSaveInstanceState(Parcelable superState) {
        SavedState saveState = new SavedState(superState);
        saveState.state = provideState();
        saveState.type = type;
        return saveState;
    }

    @SuppressWarnings("unchecked")
    public Parcelable onRestoreInstanceState(Parcelable state) {
        boolean canDelegate = (state instanceof SavedState);
        if (canDelegate) {
            SavedState ss = (SavedState) state;
            this.state = (State) ss.state;
            return ss.getSuperState();
        }
        return state;
    }

    private static class SavedState extends View.BaseSavedState {
        Parcelable state;
        Class<? extends Parcelable> type;

        public SavedState(Parcel source) {
            super(source);
            state = source.readParcelable(type.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(state, 0);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
