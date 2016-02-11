package com.jaspersoft.android.jaspermobile.domain.interactor.profile;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadata;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadataCollection;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractSimpleUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.ProfileRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetProfilesMetadataUseCase extends AbstractSimpleUseCase<ProfileMetadataCollection> {
    private final ProfileRepository mProfileRepository;
    private final JasperServerRepository mServerRepository;

    @Inject
    public GetProfilesMetadataUseCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ProfileRepository profileRepository,
            JasperServerRepository serverRepository
    ) {
        super(preExecutionThread, postExecutionThread);
        mProfileRepository = profileRepository;
        mServerRepository = serverRepository;
    }

    @Override
    protected Observable<ProfileMetadataCollection> buildUseCaseObservable() {
        Observable<List<Profile>> listProfiles = mProfileRepository.listProfiles();
        return listProfiles.flatMap(new Func1<List<Profile>, Observable<ProfileMetadataCollection>>() {
            @Override
            public Observable<ProfileMetadataCollection> call(List<Profile> profiles) {
                Profile activeProfile = mProfileRepository.getActiveProfile();
                if (activeProfile == null) {
                    activeProfile = Profile.create("");
                }

                List<ProfileMetadata> metadataList = new ArrayList<>(profiles.size());
                for (Profile profile : profiles) {
                    boolean isActive = profile.equals(activeProfile);
                    JasperServer server = mServerRepository.getServer(profile);
                    ProfileMetadata metadata = new ProfileMetadata(profile, server, isActive);
                    metadataList.add(metadata);
                }

                ProfileMetadataCollection profileMetadataCollection =
                        new ProfileMetadataCollection(metadataList);
                return Observable.just(profileMetadataCollection);
            }
        });
    }
}
