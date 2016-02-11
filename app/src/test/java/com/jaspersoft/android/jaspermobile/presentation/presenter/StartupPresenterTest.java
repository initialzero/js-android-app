package com.jaspersoft.android.jaspermobile.presentation.presenter;

import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.data.ComponentManager;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadata;
import com.jaspersoft.android.jaspermobile.domain.interactor.profile.GetMetadataForProfileUseCase;
import com.jaspersoft.android.jaspermobile.presentation.navigation.FakePageFactory;
import com.jaspersoft.android.jaspermobile.presentation.navigation.MainPage;
import com.jaspersoft.android.jaspermobile.presentation.navigation.Navigator;
import com.jaspersoft.android.jaspermobile.presentation.navigation.Page;
import com.jaspersoft.android.jaspermobile.presentation.navigation.SignUpPage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class StartupPresenterTest {
    private static final Integer REQUEST_CODE = 100;

    @Mock
    Navigator mNavigator;

    @Mock
    ComponentManager.Callback mCallback;

    @Mock
    Analytics mAnalytics;

    @Mock
    JasperServer mServer;
    @Mock
    ProfileMetadata mProfileMetadata;
    @Mock
    GetMetadataForProfileUseCase mGetMetadataForProfileUseCase;

    private FakePageFactory mFakePageFactory;
    private StartupPresenter mStartupPresenter;
    private FakeComponentManager fakeComponentManager;

    private final Profile fakeProfile = Profile.create("fake");

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        setupSpy();
        setupMocks();

        fakeComponentManager = spy(new FakeComponentManager());
        mStartupPresenter = new StartupPresenter(
                REQUEST_CODE,
                mAnalytics,
                fakeComponentManager,
                mFakePageFactory,
                mNavigator,
                mGetMetadataForProfileUseCase
        );
    }

    private void setupMocks() {
        when(mServer.getVersion()).thenReturn("6.0");
        when(mServer.getEdition()).thenReturn("PRO");
        when(mProfileMetadata.getServer()).thenReturn(mServer);
        when(mProfileMetadata.getProfile()).thenReturn(fakeProfile);
    }

    private void setupSpy() {
        mFakePageFactory = spy(new FakePageFactory());
    }

    @Test
    public void should_navigate_to_main_page_if_has_active_profile() throws Exception {
        givenActiveProfile();
        givenMetadataForProfile();

        whenTriesToSetupProfile();

        thenSetupsProfileComponent();
        thenShouldLogServerToAnalytics();
        thenNavigatesToMainPage();
    }

    @Test
    public void should_navigate_to_authentication_page_if_no_active_profile() throws Exception {
        givenNoActiveProfile();

        whenTriesToSetupProfile();

        thenSetupsProfileComponent();
        thenNavigatesToSignUpPage();
    }

    @Test
    public void should_create_use_active_profile() throws Exception {
        givenActiveProfile();

        whenSetupActiveProfile();

        thenActivatesProfile();
        thenNavigatesToMainPage();
    }

    private void givenMetadataForProfile() {
        when(mGetMetadataForProfileUseCase.execute(any(Profile.class))).thenReturn(mProfileMetadata);
    }

    private void thenShouldLogServerToAnalytics() {
        verify(mAnalytics).setServerInfo("6.0", "PRO");
    }

    private void givenActiveProfile() {
        fakeComponentManager.setProfile(fakeProfile);
    }

    private void givenNoActiveProfile() {
        fakeComponentManager.setProfile(null);
    }

    private void whenSetupActiveProfile() {
        mStartupPresenter.setupNewProfile(fakeProfile);
    }

    private void whenTriesToSetupProfile() {
        mStartupPresenter.tryToSetupProfile();
    }

    private void thenActivatesProfile() {
        verify(fakeComponentManager).setupActiveProfile(fakeProfile);
    }

    private void thenSetupsProfileComponent() {
        verify(fakeComponentManager).setupProfileComponent(any(ComponentManager.Callback.class));
    }

    private void thenNavigatesToSignUpPage() {
        ArgumentCaptor<Page> argument = ArgumentCaptor.forClass(Page.class);
        verify(mNavigator).navigateForResult(argument.capture(), eq(REQUEST_CODE), eq(false));
        assertThat(argument.getValue(), is(instanceOf(SignUpPage.class)));
    }

    private void thenNavigatesToMainPage() {
        ArgumentCaptor<Page> argument = ArgumentCaptor.forClass(Page.class);
        verify(mNavigator).navigate(argument.capture(), eq(true));
        assertThat(argument.getValue(), is(instanceOf(MainPage.class)));
    }

    private static class FakeComponentManager implements ComponentManager {
        private Profile mProfile;

        public void setProfile(Profile profile) {
            mProfile = profile;
        }

        @Override
        public void setupProfileComponent(@Nullable Callback callback) {
            if (mProfile != null) {
                callback.onSetupComplete(mProfile);
            } else {
                callback.onActiveProfileMissing();
            }
        }

        @Override
        public void setupActiveProfile(Profile profile) {
        }
    }
}