package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.domain.interactor.UseCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.presentation.view.AuthenticationView;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class AuthenticationPresenter implements Presenter {
    private AuthenticationView mView;

    private final UseCase saveProfileUseCase;

    @Inject
    public AuthenticationPresenter(@Named("saveProfile") UseCase saveProfileUseCase) {
        this.saveProfileUseCase = saveProfileUseCase;
    }

    public void setView(AuthenticationView view) {
        mView = view;
    }

    public void saveCredentials() {

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
}
