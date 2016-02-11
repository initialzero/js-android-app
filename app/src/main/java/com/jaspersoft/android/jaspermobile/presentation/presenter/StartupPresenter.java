package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.data.ComponentManager;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadata;
import com.jaspersoft.android.jaspermobile.domain.interactor.profile.GetMetadataForProfileUseCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.presentation.contract.StartupContract;
import com.jaspersoft.android.jaspermobile.presentation.navigation.Navigator;
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
    private final Analytics mAnalytics;
    private final ComponentManager mComponentManager;
    private final PageFactory mPageFactory;
    private final Navigator mNavigator;
    private final GetMetadataForProfileUseCase mGetMetadataForProfileUseCase;

    @Inject
    public StartupPresenter(
            @Named("SIGN_UP_REQUEST") Integer signUpRequest,
            Analytics analytics,
            ComponentManager componentManager,
            PageFactory pageFactory,
            Navigator navigator,
            GetMetadataForProfileUseCase getMetadataForProfileUseCase
    ) {
        mSignUpRequest = signUpRequest;
        mAnalytics = analytics;
        mComponentManager = componentManager;
        mPageFactory = pageFactory;
        mNavigator = navigator;
        mGetMetadataForProfileUseCase = getMetadataForProfileUseCase;
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
            public void onSetupComplete(Profile profile) {
                logAnalyticsEvent(profile);
                navigateToMainPage();
            }
        });
    }

    private void logAnalyticsEvent(Profile profile) {
        ProfileMetadata metadata = mGetMetadataForProfileUseCase.execute(profile);
        JasperServer server = metadata.getServer();

        String serverEdition = server.getEdition();
        String version = server.getVersion();
        if (metadata.isDemo()) {
            serverEdition = "DEMO";
        }

        mAnalytics.setServerInfo(version, serverEdition);
    }

    @Override
    public void setupNewProfile(Profile profile) {
        mComponentManager.setupActiveProfile(profile);
        navigateToMainPage();
    }

    private void navigateToMainPage() {
        Page mainPage = mPageFactory.createMainPage();
        mNavigator.navigate(mainPage, true);
    }
}
