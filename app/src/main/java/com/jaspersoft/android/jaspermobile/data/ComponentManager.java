package com.jaspersoft.android.jaspermobile.data;

import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.components.AppComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;
import com.jaspersoft.android.jaspermobile.util.cast.ResourcePresentationService;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public class ComponentManager {
    private final ActiveProfileCache mActiveProfileCache;
    private final ProfileCache mProfileCache;
    private final Analytics mAnalytics;
    private final GraphObject mGraphObject;

    @Inject
    public ComponentManager(
            Analytics analytics,
            GraphObject graphObject,
            ActiveProfileCache activeProfileCache,
            ProfileCache profileCache
    ) {
        mAnalytics = analytics;
        mGraphObject = graphObject;
        mActiveProfileCache = activeProfileCache;
        mProfileCache = profileCache;
    }

    public Profile setupProfileComponent() {
        Profile activeProfile = getActiveProfile();
        ProfileComponent profileComponent = getCurrentProfileComponent();
        Profile currentProfile = profileComponent.getProfile();

        if (activeProfile == null) {
            return tryToSetupFirstAvailable();
        } else {
            if (currentProfile.equals(activeProfile)) {
                return currentProfile;
            } else {
                setupProfileComponent(activeProfile);
                return activeProfile;
            }
        }
    }

    public void setupActiveProfile(Profile profile) {
        activateProfile(profile);
        setupProfileComponent(profile);
    }

    private void activateProfile(Profile profile) {
        mActiveProfileCache.put(profile);

        mAnalytics.sendUserChangedEvent();
        ResourcePresentationService.stopService();
    }

    private Profile tryToSetupFirstAvailable() {
        Profile profile = selectFirstAvailableProfile();

        if (profile == null) {
            return Profile.getFake();
        } else {
            activateProfile(profile);
            setupProfileComponent(profile);
            return profile;
        }
    }

    @Nullable
    private Profile getActiveProfile() {
        List<Profile> profiles = mProfileCache.getAll();
        Profile activeProfile = mActiveProfileCache.get();
        if (profiles.contains(activeProfile)) {
            return activeProfile;
        }
        return null;
    }

    private Profile selectFirstAvailableProfile() {
        List<Profile> profiles = mProfileCache.getAll();
        if (profiles.isEmpty()) {
            return null;
        }
        return profiles.get(0);
    }

    private void setupProfileComponent(Profile profile) {
        ProfileComponent newComponent = createProfileComponent(profile);
        mGraphObject.setProfileComponent(newComponent);
    }

    private ProfileComponent getCurrentProfileComponent() {
        return mGraphObject.getProfileComponent();
    }

    private ProfileComponent createProfileComponent(Profile profile) {
        AppComponent component = mGraphObject.getComponent();
        return component.plus(new ProfileModule(profile));
    }
}
