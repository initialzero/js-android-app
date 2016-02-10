package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.data.ComponentManager;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadata;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.interactor.profile.GetActiveProfileUseCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.profile.GetProfilesUseCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.presentation.contract.NavigationContract;
import com.jaspersoft.android.jaspermobile.presentation.model.ProfileViewModel;
import com.jaspersoft.android.jaspermobile.presentation.model.mapper.ProfileViewModelMapper;
import com.jaspersoft.android.jaspermobile.presentation.navigation.Navigator;
import com.jaspersoft.android.jaspermobile.presentation.navigation.PageFactory;

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
    private final GetProfilesUseCase mGetProfilesUseCase;
    private final GetActiveProfileUseCase mGetActiveProfileUseCase;

    @Inject
    public NavigationPresenter(
            Navigator navigator,
            PageFactory pageFactory,
            ComponentManager componentManager,
            ProfileViewModelMapper profileViewModelMapper,
            GetProfilesUseCase getProfilesUseCase,
            GetActiveProfileUseCase getActiveProfileUseCase) {
        mNavigator = navigator;
        mPageFactory = pageFactory;
        mComponentManager = componentManager;
        mProfileViewModelMapper = profileViewModelMapper;
        mGetProfilesUseCase = getProfilesUseCase;
        mGetActiveProfileUseCase = getActiveProfileUseCase;
    }

    @Override
    public void destroy() {
        mGetProfilesUseCase.unsubscribe();
        mGetActiveProfileUseCase.unsubscribe();
    }

    @Override
    public void loadProfiles() {
        mGetProfilesUseCase.execute(new SimpleSubscriber<List<ProfileMetadata>>() {
            @Override
            public void onError(Throwable e) {
                Timber.e(e, "GetProfilesUseCase# failed");
            }

            @Override
            public void onNext(List<ProfileMetadata> items) {
                List<ProfileViewModel> profiles = mProfileViewModelMapper.transform(items);
                getView().showProfiles(profiles);
            }
        });
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
        mNavigator.navigate(mPageFactory.createStartUpPage(), true);
    }
}
