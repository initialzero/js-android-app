package com.jaspersoft.android.jaspermobile.data;

import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.components.AppComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class ComponentManagerImpl implements ComponentManager {
    private final ActiveProfileCache mActiveProfileCache;
    private final ProfileCache mProfileCache;
    private final GraphObject mGraphObject;

    public ComponentManagerImpl(
            ActiveProfileCache activeProfileCache,
            ProfileCache profileCache,
            GraphObject graphObject
    ) {
        mActiveProfileCache = activeProfileCache;
        mProfileCache = profileCache;
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
    public void setupActiveProfile(Profile profile) {
        activateProfile(profile);
        setupProfileComponent(profile);
    }

    private void activateProfile(Profile profile) {
        mActiveProfileCache.put(profile);
    }

    private void tryToSetupActiveProfile(Callback callback) {
        Profile activeProfile = getActiveProfile();

        if (activeProfile == null) {
            tryToSetupFirstAvailable(callback);
        } else {
            setupProfileComponent(activeProfile);
            callback.onSetupComplete();
        }
    }

    private void tryToSetupFirstAvailable(Callback callback) {
        Profile profile = selectFirstAvailableProfile();
        if (profile == null) {
            callback.onActiveProfileMissing();
        } else {
            activateProfile(profile);
        }
    }

    private Profile getActiveProfile() {
        return mActiveProfileCache.get();
    }

    private Profile selectFirstAvailableProfile() {
        List<Profile> profiles = mProfileCache.getAll();
        if (profiles.isEmpty()) {
            return null;
        }
        return profiles.get(0);
    }

    private void setupProfileComponent(Profile profile) {
        AppComponent component = mGraphObject.getComponent();
        ProfileComponent newComponent = component.plus(new ProfileModule(profile));
        mGraphObject.setProfileComponent(newComponent);
    }
}
