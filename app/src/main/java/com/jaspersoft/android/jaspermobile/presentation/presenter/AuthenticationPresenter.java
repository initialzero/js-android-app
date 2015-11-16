package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.interactor.SaveProfile;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.AliasMissingException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ProfileReservedException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerUrlFormatException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerUrlMissingException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.UsernameMissingException;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.presentation.action.ProfileActionListener;
import com.jaspersoft.android.jaspermobile.presentation.mapper.CredentialsDataMapper;
import com.jaspersoft.android.jaspermobile.presentation.mapper.ProfileDataMapper;
import com.jaspersoft.android.jaspermobile.presentation.model.ProfileModel;
import com.jaspersoft.android.jaspermobile.presentation.view.AuthenticationView;

import javax.inject.Inject;

import rx.Subscriber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class AuthenticationPresenter implements Presenter, ProfileActionListener {
    private AuthenticationView mView;

    private final SaveProfile mSaveProfileUseCase;
    private final ProfileDataMapper mProfileDataMapper;
    private final CredentialsDataMapper mCredentialsDataMapper;

    @Inject
    public AuthenticationPresenter(SaveProfile saveProfileUseCase,
                                   ProfileDataMapper profileDataMapper,
                                   CredentialsDataMapper credentialsDataMapper) {
        mSaveProfileUseCase = saveProfileUseCase;
        mProfileDataMapper = profileDataMapper;
        mCredentialsDataMapper = credentialsDataMapper;
    }

    public void setView(AuthenticationView view) {
        mView = view;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        mSaveProfileUseCase.unsubscribe();
    }

    @Override
    public void saveProfile(ProfileModel profileModel) {
        mView.hideRetry();
        mView.showLoading();

        Profile domainProfile = mProfileDataMapper.transform(profileModel);
        BaseCredentials domainCredentials = mCredentialsDataMapper.transform(profileModel.getCredentials());
        String baseUrl = profileModel.getBaseUrl();

        mSaveProfileUseCase.execute(baseUrl, domainProfile, domainCredentials, new ProfileSaveListener());
    }

    void handleProfileComplete() {
        mView.hideLoading();
    }

    void handleProfileSaveFailure(Throwable e) {
        mView.hideLoading();
        if (e instanceof DuplicateProfileException) {
            mView.showAliasDuplicateError();
        } else if (e instanceof ProfileReservedException) {
            mView.showAliasReservedError();
        } else if (e instanceof AliasMissingException) {
            mView.showAliasRequiredError();
        } else if (e instanceof ServerUrlFormatException) {
            mView.showServerUrlFormatError();
        } else if (e instanceof ServerUrlMissingException) {
            mView.showServerUrlRequiredError();
        } else if (e instanceof UsernameMissingException) {
            mView.showUsernameRequiredError();
        } else {
            mView.showError(e.getMessage());
        }
    }

    void handleProfileSaveSuccess() {
        mView.navigateToApp();
    }

    private class ProfileSaveListener extends Subscriber<Profile> {
        @Override
        public void onCompleted() {
            handleProfileComplete();
        }

        @Override
        public void onError(Throwable e) {
            handleProfileSaveFailure(e);
        }

        @Override
        public void onNext(Profile profile) {
            handleProfileSaveSuccess();
        }
    }
}
