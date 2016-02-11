package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.data.ComponentManager;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadata;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadataCollection;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.interactor.profile.GetActiveProfileUseCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.profile.GetProfilesMetadataUseCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.presentation.contract.NavigationContract;
import com.jaspersoft.android.jaspermobile.presentation.model.ProfileViewModel;
import com.jaspersoft.android.jaspermobile.presentation.model.mapper.ProfileViewModelMapper;
import com.jaspersoft.android.jaspermobile.presentation.navigation.Navigator;
import com.jaspersoft.android.jaspermobile.presentation.navigation.PageFactory;
import com.jaspersoft.android.jaspermobile.presentation.page.NavigationPageState;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class NavigationPresenter extends Presenter<NavigationContract.View> implements NavigationContract.ActionListener {
    private final Navigator mNavigator;
    private final PageFactory mPageFactory;
    private final ComponentManager mComponentManager;
    private final ProfileViewModelMapper mProfileViewModelMapper;
    private final GetProfilesMetadataUseCase mGetProfilesMetadataUseCase;
    private final GetActiveProfileUseCase mGetActiveProfileUseCase;

    @Inject
    public NavigationPresenter(
            Navigator navigator,
            PageFactory pageFactory,
            ComponentManager componentManager,
            ProfileViewModelMapper profileViewModelMapper,
            GetProfilesMetadataUseCase getProfilesMetadataUseCase,
            GetActiveProfileUseCase getActiveProfileUseCase) {
        mNavigator = navigator;
        mPageFactory = pageFactory;
        mComponentManager = componentManager;
        mProfileViewModelMapper = profileViewModelMapper;
        mGetProfilesMetadataUseCase = getProfilesMetadataUseCase;
        mGetActiveProfileUseCase = getActiveProfileUseCase;
    }

    @Override
    public void resume() {
        NavigationPageState state = getView().getState();
        if (state.shouldExit()) {
            exitCurrentSession();
        }
    }

    @Override
    public void destroy() {
        mGetProfilesMetadataUseCase.unsubscribe();
        mGetActiveProfileUseCase.unsubscribe();
    }

    @Override
    public void loadProfiles() {
        mGetProfilesMetadataUseCase.execute(new SimpleSubscriber<ProfileMetadataCollection>() {
            @Override
            public void onError(Throwable e) {
                Timber.e(e, "GetProfilesUseCase# failed");
            }

            @Override
            public void onNext(ProfileMetadataCollection collection) {
                if (collection.containsActiveProfile()) {
                    showProfiles(collection);
                } else {
                    NavigationPageState state = getView().getState();
                    state.setShouldExit(true);
                }
            }
        });
    }

    private void showProfiles(ProfileMetadataCollection collection) {
        List<ProfileViewModel> profiles = mProfileViewModelMapper.transform(collection.get());
        getView().showProfiles(profiles);
    }

    @Override
    public void loadActiveProfile() {
        mGetActiveProfileUseCase.execute(new SimpleSubscriber<ProfileMetadata>() {
            @Override
            public void onError(Throwable e) {
                Timber.e(e, "GetActiveProfileUseCase# failed");
            }

            @Override
            public void onNext(ProfileMetadata profile) {
                JasperServer server = profile.getServer();
                boolean proEdition = server.isProEdition();
                getView().toggleRecentlyViewedNavigation(proEdition);

                ProfileViewModel model = mProfileViewModelMapper.transform(profile);
                getView().showActiveProfile(model);
            }
        });
    }

    @Override
    public void activateProfile(Profile profile) {
        mComponentManager.setupActiveProfile(profile);
        exitCurrentSession();
    }

    private void exitCurrentSession() {
        mNavigator.navigate(mPageFactory.createStartUpPage(), true);
    }
}
