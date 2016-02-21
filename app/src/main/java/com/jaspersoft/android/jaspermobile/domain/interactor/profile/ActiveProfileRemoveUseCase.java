package com.jaspersoft.android.jaspermobile.domain.interactor.profile;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractSimpleUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.ProfileRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class ActiveProfileRemoveUseCase extends AbstractSimpleUseCase<Boolean> {
    private final ProfileRepository mProfileRepository;

    @Inject
    public ActiveProfileRemoveUseCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ProfileRepository profileRepository
    ) {
        super(preExecutionThread, postExecutionThread);
        mProfileRepository = profileRepository;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        Observable<List<Profile>> listProfiles = mProfileRepository.listProfiles();
        return listProfiles.map(new Func1<List<Profile>, Boolean>() {
            @Override
            public Boolean call(List<Profile> profiles) {
                Profile activeProfile = mProfileRepository.getActiveProfile();
                return !profiles.contains(activeProfile);
            }
        });
    }
}
