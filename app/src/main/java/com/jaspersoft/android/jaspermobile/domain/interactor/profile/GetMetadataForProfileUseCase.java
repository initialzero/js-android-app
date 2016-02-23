package com.jaspersoft.android.jaspermobile.domain.interactor.profile;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadata;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.ProfileRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public class GetMetadataForProfileUseCase {

    private final JasperServerRepository mServerRepository;
    private final ProfileRepository mProfileRepository;

    @Inject
    public GetMetadataForProfileUseCase(JasperServerRepository serverRepository, ProfileRepository profileRepository) {
        mServerRepository = serverRepository;
        mProfileRepository = profileRepository;
    }

    public ProfileMetadata execute(Profile profile) {
        Profile activeProfile = mProfileRepository.getActiveProfile();
        JasperServer server = mServerRepository.getServer(profile);
        return new ProfileMetadata(profile, server, profile.equals(activeProfile));
    }
}
