package com.jaspersoft.android.jaspermobile.domain.interactor.profile;

import com.jaspersoft.android.jaspermobile.data.validator.ProfileAuthorizedValidation;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileForm;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractSimpleUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.CredentialsRepository;
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
public class AuthorizeSessionUseCase extends AbstractSimpleUseCase<Void> {

    private final ProfileRepository mProfileRepository;
    private final JasperServerRepository mServerRepository;
    private final CredentialsRepository mCredentialsRepository;

    // TODO revise authorization approach. Remove there should be no data package reference in domain
    private final ProfileAuthorizedValidation mProfileAuthorizedValidation;

    @Inject
    public AuthorizeSessionUseCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ProfileRepository profileRepository,
            JasperServerRepository serverRepository,
            CredentialsRepository credentialsRepository,
            ProfileAuthorizedValidation profileAuthorizedValidation
    ) {
        super(preExecutionThread, postExecutionThread);
        mProfileRepository = profileRepository;
        mServerRepository = serverRepository;
        mCredentialsRepository = credentialsRepository;
        mProfileAuthorizedValidation = profileAuthorizedValidation;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable() {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                Profile profile = mProfileRepository.getActiveProfile();
                JasperServer server = mServerRepository.getServer(profile);
                AppCredentials credentials = mCredentialsRepository.getCredentials(profile);

                ProfileForm profileForm = new ProfileForm.Builder()
                        .setBaseUrl(server.getBaseUrl())
                        .setAlias(profile.getKey())
                        .setCredentials(credentials)
                        .build();
                try {
                    mProfileAuthorizedValidation.validate(profileForm);
                    return Observable.just(null);
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }
        });
    }
}
