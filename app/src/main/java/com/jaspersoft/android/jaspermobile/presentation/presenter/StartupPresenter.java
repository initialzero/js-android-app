package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.data.ComponentManager;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.presentation.navigation.Navigator;
import com.jaspersoft.android.jaspermobile.presentation.contract.StartupContract;
import com.jaspersoft.android.jaspermobile.presentation.navigation.Page;
import com.jaspersoft.android.jaspermobile.presentation.navigation.PageFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class StartupPresenter extends Presenter<StartupContract.View> implements StartupContract.ActionListener {
    private final Integer mSignUpRequest;
    private final ComponentManager mComponentManager;
    private final PageFactory mPageFactory;
    private final Navigator mNavigator;

    @Inject
    public StartupPresenter(
            @Named("SIGN_UP_REQUEST") Integer signUpRequest,
            ComponentManager componentManager,
            PageFactory pageFactory,
            Navigator navigator
    ) {
        mSignUpRequest = signUpRequest;
        mComponentManager = componentManager;
        mPageFactory = pageFactory;
        mNavigator = navigator;
    }

    @Override
    public void tryToSetupProfile() {
        mComponentManager.setupProfileComponent(new ComponentManager.Callback() {
            @Override
            public void onActiveProfileMissing() {
                Page authPage = mPageFactory.createSignUpPage();
                mNavigator.navigateForResult(authPage, mSignUpRequest, false);
            }

            @Override
            public void onSetupComplete() {
                navigateToMainPage();
            }
        });
    }

    private void navigateToMainPage() {
        Page mainPage = mPageFactory.createMainPage();
        mNavigator.navigate(mainPage, true);
    }

    @Override
    public void setupNewProfile() {
        mComponentManager.setupActiveProfile();
        navigateToMainPage();
    }
}
