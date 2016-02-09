package com.jaspersoft.android.jaspermobile.data;

import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.activities.robospice.Nullable;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ActiveProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.components.AppComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class ComponentManagerImpl implements ComponentManager {
    private final ActiveProfileCache mActiveProfileCache;
    private final GraphObject mGraphObject;

    @VisibleForTesting
    ComponentManagerImpl(
            ActiveProfileCache activeProfileCache,
            GraphObject graphObject
    ) {
        mActiveProfileCache = activeProfileCache;
        mGraphObject = graphObject;
    }

    @Override
    public void setupProfileComponent(@Nullable ComponentManager.Callback callback) {
        if (callback == null) {
            callback = Callback.EMPTY;
        }

        ProfileComponent profileComponent = mGraphObject.getProfileComponent();
        if (profileComponent == null) {
            tryToSetupActiveProfile(callback);
        } else {
            callback.onSetupComplete();
        }
    }

    @Override
    public void setupActiveProfile() {
        Profile activeProfile = getActiveProfile();

        if (activeProfile == null) {
            throw new IllegalStateException("There is no active profile impossible to setup profile component");
        }

        setupProfile(activeProfile);
    }

    private void tryToSetupActiveProfile(Callback callback) {
        Profile activeProfile = getActiveProfile();

        if (activeProfile == null) {
            callback.onActiveProfileMissing();
        } else {
            setupProfile(activeProfile);
            callback.onSetupComplete();
        }
    }

    private Profile getActiveProfile() {
        return mActiveProfileCache.get();
    }

    private void setupProfile(Profile profile) {
        AppComponent component = mGraphObject.getComponent();
        ProfileComponent newComponent = component.plus(new ProfileModule(profile));
        mGraphObject.setProfileComponent(newComponent);
    }
}
