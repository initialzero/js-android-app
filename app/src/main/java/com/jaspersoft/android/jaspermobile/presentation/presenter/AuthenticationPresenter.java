package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.interactor.SaveProfile;
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
    }

    @Override
    public void saveProfile(ProfileModel profileModel) {
        mView.hideRetry();
        mView.showLoading();

        Profile domainProfile = mProfileDataMapper.transform(profileModel);
        BaseCredentials domainCredentials = mCredentialsDataMapper.transform(profileModel.getCredentials());
        String baseUrl = profileModel.getBaseUrl();

        mSaveProfileUseCase.execute(baseUrl, domainProfile, domainCredentials, provideSaveListener());
    }

    Subscriber provideSaveListener() {
        return new ProfileSaveListener();
    }

    private class ProfileSaveListener extends Subscriber {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Object o) {

        }
    }
}
