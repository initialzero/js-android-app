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

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetActiveProfileUseCase extends AbstractSimpleUseCase<ProfileMetadata> {
    private final ProfileRepository mProfileRepository;
    private final JasperServerRepository mServerRepository;

    @Inject
    public GetActiveProfileUseCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ProfileRepository profileRepository,
            JasperServerRepository serverRepository) {
        super(preExecutionThread, postExecutionThread);
        mProfileRepository = profileRepository;
        mServerRepository = serverRepository;
    }

    @Override
    protected Observable<ProfileMetadata> buildUseCaseObservable() {
        return Observable.defer(new Func0<Observable<ProfileMetadata>>() {
            @Override
            public Observable<ProfileMetadata> call() {
                Profile activeProfile = mProfileRepository.getActiveProfile();
                JasperServer server = mServerRepository.getServer(activeProfile);
                ProfileMetadata metadata = new ProfileMetadata(activeProfile, server, true);
                return Observable.just(metadata);
            }
        });
    }
}
