package com.jaspersoft.android.jaspermobile.domain.interactor.profile;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadata;
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
import rx.functions.Func0;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetProfilesUseCase extends AbstractSimpleUseCase<List<ProfileMetadata>> {
    private final ProfileRepository mProfileRepository;
    private final JasperServerRepository mServerRepository;

    @Inject
    public GetProfilesUseCase(
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
    protected Observable<List<ProfileMetadata>> buildUseCaseObservable() {
        return Observable.defer(new Func0<Observable<List<ProfileMetadata>>>() {
            @Override
            public Observable<List<ProfileMetadata>> call() {
                List<Profile> profiles = mProfileRepository.listProfiles();
                Profile activeProfile = mProfileRepository.getActiveProfile();

                List<ProfileMetadata> metadataList = new ArrayList<>(profiles.size());
                for (Profile profile : profiles) {
                    boolean isActive = profile.equals(activeProfile);
                    JasperServer server = mServerRepository.getServer(profile);
                    ProfileMetadata metadata = new ProfileMetadata(profile, server, isActive);
                    metadataList.add(metadata);
                }

                return Observable.just(metadataList);
            }
        });
    }
}
