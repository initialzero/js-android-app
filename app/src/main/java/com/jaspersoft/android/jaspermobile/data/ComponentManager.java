package com.jaspersoft.android.jaspermobile.data;

import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.domain.Profile;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ComponentManager {
    void setupProfileComponent(@Nullable Callback callback);

    void setupActiveProfile(Profile profile);

    interface Callback {
        Callback EMPTY = new Callback() {
            @Override
            public void onActiveProfileMissing() {
            }

            @Override
            public void onSetupComplete(Profile profile) {
            }
        };
        void onActiveProfileMissing();

        void onSetupComplete(Profile profile);
    }
}
