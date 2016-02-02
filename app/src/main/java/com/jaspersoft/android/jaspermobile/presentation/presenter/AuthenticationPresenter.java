package com.jaspersoft.android.jaspermobile.presentation.presenter;

import android.app.Application;
import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileForm;
import com.jaspersoft.android.jaspermobile.domain.interactor.profile.SaveProfileUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveCredentials;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveProfile;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ProfileReservedException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerVersionNotSupportedException;
import com.jaspersoft.android.jaspermobile.internal.di.ActivityContext;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.presentation.contract.AuthenticationContract;
import com.jaspersoft.android.jaspermobile.presentation.validation.AliasMissingException;
import com.jaspersoft.android.jaspermobile.presentation.validation.PasswordMissingException;
import com.jaspersoft.android.jaspermobile.presentation.validation.ProfileFormValidation;
import com.jaspersoft.android.jaspermobile.presentation.validation.ServerUrlFormatException;
import com.jaspersoft.android.jaspermobile.presentation.validation.ServerUrlMissingException;
import com.jaspersoft.android.jaspermobile.presentation.validation.UsernameMissingException;
import com.jaspersoft.android.sdk.service.exception.ServiceException;

import javax.inject.Inject;

import rx.Subscriber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class AuthenticationPresenter extends Presenter<AuthenticationContract.View> implements AuthenticationContract.Action {
    private final Context mContext;

    private final SaveProfileUseCase mSaveProfileUseCaseUseCase;
    private final ProfileFormValidation mProfileFormValidation;
    private final RequestExceptionHandler mRequestExceptionHandler;

    @Inject
    public AuthenticationPresenter(
            @ActivityContext Context context,
            SaveProfileUseCase saveProfileUseCaseUseCase,
            ProfileFormValidation profileFormValidation,
            RequestExceptionHandler requestExceptionHandler) {
        mContext = context;
        mSaveProfileUseCaseUseCase = saveProfileUseCaseUseCase;
        mProfileFormValidation = profileFormValidation;
        mRequestExceptionHandler = requestExceptionHandler;
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
    public void saveProfile(ProfileForm profileForm) {
        if (isClientDataValid(profileForm)) {
            getView().showLoading();
            mSaveProfileUseCaseUseCase.execute(profileForm, new ProfileSaveListener());
        }
    }

    private boolean isClientDataValid(ProfileForm form) {
        try {
            mProfileFormValidation.validate(form);
            return true;
        } catch (UsernameMissingException e) {
            getView().showUsernameRequiredError();
        } catch (PasswordMissingException e) {
            getView().showPasswordRequiredError();
        } catch (AliasMissingException e) {
            getView().showAliasRequiredError();
        } catch (ServerUrlMissingException e) {
            getView().showServerUrlRequiredError();
        } catch (ServerUrlFormatException e) {
            getView().showServerUrlFormatError();
        }
        return false;
    }

    @VisibleForTesting
    void handleProfileComplete() {
        initLegacyJsRestClient();
        getView().hideLoading();
    }

    @VisibleForTesting
    void handleProfileSaveFailure(Throwable e) {
        getView().hideLoading();
        if (e instanceof DuplicateProfileException) {
            getView().showAliasDuplicateError();
        } else if (e instanceof ProfileReservedException) {
            getView().showAliasReservedError();
        } else if (e instanceof ServerVersionNotSupportedException) {
            getView().showServerVersionNotSupported();
        } else if (e instanceof FailedToSaveProfile) {
            getView().showFailedToAddProfile(e.getMessage());
        } else if (e instanceof FailedToSaveCredentials) {
            getView().showFailedToAddProfile(e.getMessage());
        } else if (e instanceof ServiceException) {
            getView().showError(mRequestExceptionHandler.extractMessage(e));
        } else {
            getView().showError(e.getMessage());
        }
    }

    @VisibleForTesting
    void handleProfileSaveSuccess() {
        getView().navigateToApp();
    }

    /**
     * This is ugly fix for incompatible versions
     * TODO: remove as soon as JsRestClient will be dropped from App
     */
    private void initLegacyJsRestClient() {
        Application application = (Application) mContext.getApplicationContext();
        if (application instanceof JasperMobileApplication) {
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
