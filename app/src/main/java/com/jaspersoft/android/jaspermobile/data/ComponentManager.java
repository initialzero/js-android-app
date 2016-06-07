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
    private final GraphObject mGraphObject;
    private final Callback mCallback;

    @Inject
    public ComponentManager(
            Callback callback,
            GraphObject graphObject,
            ActiveProfileCache activeProfileCache,
            ProfileCache profileCache
    ) {
        mCallback = (callback == null) ? Callback.NULL : callback;
        mGraphObject = graphObject;
        mActiveProfileCache = activeProfileCache;
        mProfileCache = profileCache;
    }

    public Profile setupProfileComponent() {
        Profile activeProfile = getActiveProfile();

        if (activeProfile == null) {
            return tryToSetupFirstAvailable();
        } else {
            ProfileComponent profileComponent = getCurrentProfileComponent();
            if (profileComponent != null) {
                Profile currentProfile = profileComponent.getProfile();

                if (currentProfile.equals(activeProfile)) {
                    return currentProfile;
                }
            }

            setupProfileComponent(activeProfile);
            return activeProfile;
        }
    }

    public void setupActiveProfile(Profile profile) {
        activateProfile(profile);
        setupProfileComponent(profile);
    }

    private void activateProfile(Profile profile) {
        mActiveProfileCache.put(profile);
        mCallback.onProfileActivation(profile);
    }

    private Profile tryToSetupFirstAvailable() {
        Profile profile = selectFirstAvailableProfile();

        if (profile == null) {
            profile = Profile.getFake();
        }
        activateProfile(profile);
        setupProfileComponent(profile);
        return profile;
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

    public interface Callback {
        Callback NULL = new Callback() {
            @Override
            public void onProfileActivation(Profile profile) {
            }
        };
        void onProfileActivation(Profile profile);
    }
}
