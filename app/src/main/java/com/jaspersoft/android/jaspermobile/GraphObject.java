package com.jaspersoft.android.jaspermobile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.internal.di.components.AppComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface GraphObject {

    @NonNull
    AppComponent getComponent();

    void setProfileComponent(@Nullable ProfileComponent profileComponent);

    ProfileComponent getProfileComponent();

    class Factory {
        @NonNull
        public static GraphObject from(@NonNull Context context) {
            return (GraphObject) context.getApplicationContext();
        }
    }
}
