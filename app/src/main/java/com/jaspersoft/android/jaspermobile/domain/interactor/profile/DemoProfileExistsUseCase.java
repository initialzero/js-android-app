package com.jaspersoft.android.jaspermobile.domain.interactor.profile;

import com.jaspersoft.android.jaspermobile.data.cache.profile.ProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class DemoProfileExistsUseCase {
    private static final String MOBILE_DEMO_LABEL = "Mobile Demo";
    private final ProfileCache mProfileCache;

    @Inject
    public DemoProfileExistsUseCase(ProfileCache profileCache) {
        mProfileCache = profileCache;
    }

    public boolean execute() {
        List<Profile> profiles = mProfileCache.getAll();
        for (Profile profile : profiles) {
            if (MOBILE_DEMO_LABEL.equals(profile.getKey())) {
                return true;
            }
        }
        return false;
    }
}
