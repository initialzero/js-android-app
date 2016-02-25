package com.jaspersoft.android.jaspermobile.data;

import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.components.AppComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public class ComponentManager {
    private final CookieHandler mCookieHandler;
    private final ActiveProfileCache mActiveProfileCache;
    private final ProfileCache mProfileCache;
    private final GraphObject mGraphObject;

    @Inject
    public ComponentManager(
            GraphObject graphObject,
            CookieHandler cookieHandler,
            ActiveProfileCache activeProfileCache,
            ProfileCache profileCache
    ) {
        mGraphObject = graphObject;
        mCookieHandler = cookieHandler;
        mActiveProfileCache = activeProfileCache;
        mProfileCache = profileCache;
    }

    public Profile setupProfileComponent() {
        Profile activeProfile = getActiveProfile();

        if (activeProfile == null) {
            return tryToSetupFirstAvailable();
        } else {
            setupProfileComponent(activeProfile);
            flushCookies();
            return activeProfile;
        }
    }

    public void setupActiveProfile(Profile profile) {
        activateProfile(profile);
        setupProfileComponent(profile);
        flushCookies();
    }

    private void activateProfile(Profile profile) {
        mActiveProfileCache.put(profile);
    }

    private Profile tryToSetupFirstAvailable() {
        Profile profile = selectFirstAvailableProfile();

        if (profile == null) {
            return Profile.getFake();
        } else {
            activateProfile(profile);
            setupProfileComponent(profile);
            flushCookies();
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
        AppComponent component = mGraphObject.getComponent();
        ProfileComponent newComponent = component.plus(new ProfileModule(profile));
        mGraphObject.setProfileComponent(newComponent);
    }

    private void flushCookies() {
        if (mCookieHandler instanceof CookieManager) {
            CookieManager cookieHandler = (CookieManager) mCookieHandler;
            CookieStore cookieStore = cookieHandler.getCookieStore();

            if (cookieStore != null) {
                cookieStore.removeAll();
            }
        }
    }
}
