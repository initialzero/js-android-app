package com.jaspersoft.android.jaspermobile.presentation.presenter;

import android.app.Application;
import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.data.network.RestErrorAdapter;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.interactor.SaveProfileUseCase;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveCredentials;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveProfile;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ProfileReservedException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerVersionNotSupportedException;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.modules.SaveProfileModule;
import com.jaspersoft.android.jaspermobile.presentation.action.ProfileActionListener;
import com.jaspersoft.android.jaspermobile.presentation.mapper.CredentialsDataMapper;
import com.jaspersoft.android.jaspermobile.presentation.mapper.ProfileDataMapper;
import com.jaspersoft.android.jaspermobile.presentation.model.CredentialsModel;
import com.jaspersoft.android.jaspermobile.presentation.model.ProfileModel;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.CredentialsClientValidation;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.ProfileClientValidation;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.exception.AliasMissingException;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.exception.PasswordMissingException;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.exception.ServerUrlFormatException;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.exception.ServerUrlMissingException;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.exception.UsernameMissingException;
import com.jaspersoft.android.jaspermobile.presentation.view.AuthenticationView;

import javax.inject.Inject;

import rx.Subscriber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class AuthenticationPresenter implements Presenter, ProfileActionListener {
    private final Context mContext;
    private AuthenticationView mView;

    private final SaveProfileUseCase mSaveProfileUseCaseUseCase;
    private final ProfileDataMapper mProfileDataMapper;
    private final CredentialsDataMapper mCredentialsDataMapper;
    private final CredentialsClientValidation mCredentialsClientValidation;
    private final ProfileClientValidation mProfileClientValidation;
    private final RestErrorAdapter mRestErrorAdapter;

    /**
     * Injected through {@link SaveProfileModule}
     */
    @Inject
    public AuthenticationPresenter(
            Context context,
            SaveProfileUseCase saveProfileUseCaseUseCase,
            ProfileDataMapper profileDataMapper,
            CredentialsDataMapper credentialsDataMapper,
            CredentialsClientValidation credentialsClientValidation,
            ProfileClientValidation profileClientValidation,
            RestErrorAdapter restErrorAdapter) {
        mContext = context;
        mSaveProfileUseCaseUseCase = saveProfileUseCaseUseCase;
        mProfileDataMapper = profileDataMapper;
        mCredentialsDataMapper = credentialsDataMapper;
        mCredentialsClientValidation = credentialsClientValidation;
        mProfileClientValidation = profileClientValidation;
        mRestErrorAdapter = restErrorAdapter;
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
        mSaveProfileUseCaseUseCase.unsubscribe();
    }

    @Override
    public void saveProfile(ProfileModel profileModel) {
        if (isClientDataValid(profileModel)) {
            mView.showLoading();

            Profile domainProfile = mProfileDataMapper.transform(profileModel);
            AppCredentials domainCredentials = mCredentialsDataMapper.transform(profileModel.getCredentials());
            String baseUrl = profileModel.getServerUrl();

            mSaveProfileUseCaseUseCase.execute(baseUrl, domainProfile, domainCredentials, new ProfileSaveListener());
        }
    }

    private boolean isClientDataValid(ProfileModel profileModel) {
        return validateProfile(profileModel) && validateCredentials(profileModel.getCredentials());
    }

    private boolean validateCredentials(CredentialsModel credentialsModel) {
        try {
            mCredentialsClientValidation.validate(credentialsModel);
            return true;
        } catch (UsernameMissingException e) {
            mView.showUsernameRequiredError();
        } catch (PasswordMissingException e) {
            mView.showPasswordRequiredError();
        }
        return false;
    }

    private boolean validateProfile(ProfileModel profileModel) {
        try {
            mProfileClientValidation.validate(profileModel);
            return true;
        } catch (AliasMissingException e) {
            mView.showAliasRequiredError();
        } catch (ServerUrlMissingException e) {
            mView.showServerUrlRequiredError();
        } catch (ServerUrlFormatException e) {
            mView.showServerUrlFormatError();
        }
        return false;
    }

    @VisibleForTesting
    void handleProfileComplete() {
        initLegacyJsRestClient();
        mView.hideLoading();
    }

    @VisibleForTesting
    void handleProfileSaveFailure(Throwable e) {
        mView.hideLoading();
        if (e instanceof DuplicateProfileException) {
            mView.showAliasDuplicateError();
        } else if (e instanceof ProfileReservedException) {
            mView.showAliasReservedError();
        } else if (e instanceof ServerVersionNotSupportedException) {
            mView.showServerVersionNotSupported();
        } else if (e instanceof FailedToSaveProfile) {
            mView.showFailedToAddProfile(e.getMessage());
        } else if (e instanceof FailedToSaveCredentials) {
            mView.showFailedToAddProfile(e.getMessage());
        } else if (e instanceof RestStatusException) {
            RestStatusException statusEx = ((RestStatusException) e);
            mView.showError(mRestErrorAdapter.transform(statusEx));
        } else {
            mView.showError(e.getMessage());
        }
    }

    @VisibleForTesting
    void handleProfileSaveSuccess() {
        mView.navigateToApp();
    }

    /**
     * This is ugly fix for incompatible versions
     * TODO: remove as soon as JsRestClient will be dropped from App
     */
    private void initLegacyJsRestClient() {
        Application application = (Application) mContext.getApplicationContext();
        if (application instanceof  JasperMobileApplication) {
            JasperMobileApplication app = ((JasperMobileApplication) mContext.getApplicationContext());
            if (app != null) {
                app.initLegacyJsRestClient();
            }
        }
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
