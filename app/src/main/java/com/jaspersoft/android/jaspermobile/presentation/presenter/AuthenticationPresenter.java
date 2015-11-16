package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.domain.interactor.SaveProfile;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.presentation.action.ProfileActionListener;
import com.jaspersoft.android.jaspermobile.presentation.model.ProfileModel;
import com.jaspersoft.android.jaspermobile.presentation.view.AuthenticationView;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class AuthenticationPresenter implements Presenter, ProfileActionListener {
    private AuthenticationView mView;

    private final SaveProfile saveProfileUseCase;

    @Inject
    public AuthenticationPresenter(SaveProfile saveProfileUseCase) {
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

    @Override
    public void saveProfile(ProfileModel profileModel) {

    }
}
