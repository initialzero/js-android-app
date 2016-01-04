package com.jaspersoft.android.jaspermobile.presentation.presenter;

import android.app.Application;
import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.data.network.RestErrorAdapter;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileForm;
import com.jaspersoft.android.jaspermobile.domain.interactor.SaveProfileUseCase;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveCredentials;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveProfile;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ProfileReservedException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerVersionNotSupportedException;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.presentation.action.ProfileActionListener;
import com.jaspersoft.android.jaspermobile.presentation.validation.AliasMissingException;
import com.jaspersoft.android.jaspermobile.presentation.validation.PasswordMissingException;
import com.jaspersoft.android.jaspermobile.presentation.validation.ProfileFormValidation;
import com.jaspersoft.android.jaspermobile.presentation.validation.ServerUrlFormatException;
import com.jaspersoft.android.jaspermobile.presentation.validation.ServerUrlMissingException;
import com.jaspersoft.android.jaspermobile.presentation.validation.UsernameMissingException;
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
    private final ProfileFormValidation mProfileFormValidation;
    private final RestErrorAdapter mRestErrorAdapter;

    @Inject
    public AuthenticationPresenter(
            Context context,
            SaveProfileUseCase saveProfileUseCaseUseCase,
            ProfileFormValidation profileFormValidation,
            RestErrorAdapter restErrorAdapter) {
        mContext = context;
        mSaveProfileUseCaseUseCase = saveProfileUseCaseUseCase;
        mProfileFormValidation = profileFormValidation;
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
    public void saveProfile(ProfileForm profileForm) {
        if (isClientDataValid(profileForm)) {
            mView.showLoading();
            mSaveProfileUseCaseUseCase.execute(profileForm, new ProfileSaveListener());
        }
    }

    private boolean isClientDataValid(ProfileForm form) {
        try {
            mProfileFormValidation.validate(form);
            return true;
        } catch (UsernameMissingException e) {
            mView.showUsernameRequiredError();
        } catch (PasswordMissingException e) {
            mView.showPasswordRequiredError();
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
