package com.jaspersoft.android.jaspermobile.data;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.components.AppComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import java.net.CookieManager;
import java.net.CookieStore;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ComponentManagerTest {

    @Mock
    ActiveProfileCache mActiveProfileCache;
    @Mock
    ProfileCache mProfileCache;

    @Mock
    AppComponent mAppComponent;
    @Mock
    ProfileComponent mProfileComponent;
    @Mock
    CookieManager mCookieHandler;
    @Mock
    CookieStore mCookieStore;

    @Mock
    GraphObject mGraphObject;

    private ComponentManager mComponentManager;
    private final Profile activeProfile = Profile.create("active");

    @Rule
    public ExpectedException expected = none();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        mComponentManager = new ComponentManager(
                mGraphObject,
                mCookieHandler,
                mActiveProfileCache,
                mProfileCache
        );
    }

    private void setupMocks() {
        when(mCookieHandler.getCookieStore()).thenReturn(mCookieStore);
        when(mProfileCache.getAll()).thenReturn(Collections.<Profile>emptyList());
    }

    @Test
    public void should_trigger_active_profile_missing_if_no_profiles() throws Exception {
        givenAppComponent();
        givenNoProfileComponent();
        givenNoActiveProfile();
        givenNoRegisteredProfiles();

        Profile profile = whenSetupProfileComponent();

        thenShouldRetrieveAllProfiles();
        thenShouldReturnFakeProfile(profile);
    }

    private void thenShouldRetrieveAllProfiles() {
        verify(mProfileCache, times(2)).getAll();
    }

    private void givenNoRegisteredProfiles() {
        when(mProfileCache.getAll()).thenReturn(Collections.<Profile>emptyList());
    }

    private void thenShouldReturnFakeProfile(Profile profile) {
        assertThat(profile, is(Profile.getFake()));
    }

    @Test
    public void should_setup_profile_if_exists() throws Exception {
        givenAppComponent();
        givenNoProfileComponent();
        givenActiveProfile();

        Profile profile = whenSetupProfileComponent();

        thenShouldSetupProfileComponent();
        thenReturnActiveProfile(profile);
        thenShouldFlushAllCookies();
    }

    private void thenReturnActiveProfile(Profile profile) {
        assertThat(profile, is(activeProfile));
    }

    private void thenShouldSetupProfileComponent() {
        verify(mGraphObject).setProfileComponent(any(ProfileComponent.class));
    }

    private void thenShouldFlushAllCookies() {
        verify(mCookieStore).removeAll();
    }

    private Profile whenSetupProfileComponent() {
       return mComponentManager.setupProfileComponent();
    }

    @Test
    public void should_setup_active_profile() throws Exception {
        givenAppComponent();
        givenActiveProfile();

        whenSetupActiveProfile();

        thenShouldWriteToActiveCache();
        thenShouldSetupProfileComponent();
        thenShouldFlushAllCookies();
    }

    private void whenSetupActiveProfile() {
        mComponentManager.setupActiveProfile(activeProfile);
    }

    private void thenShouldWriteToActiveCache() {
        verify(mActiveProfileCache).put(activeProfile);
    }

    private void givenNoActiveProfile() {
        when(mActiveProfileCache.get()).thenReturn(null);
    }

    private void givenActiveProfile() {
        when(mActiveProfileCache.get()).thenReturn(activeProfile);
        when(mProfileCache.getAll()).thenReturn(Collections.singletonList(activeProfile));
    }

    private void givenNoProfileComponent() {
        when(mGraphObject.getProfileComponent()).thenReturn(null);
    }

    private void givenAppComponent() {
        when(mGraphObject.getComponent()).thenReturn(mAppComponent);
    }

    @Test
    public void should_activate_first_available_account() throws Exception {
        givenAppComponent();
        givenNoProfileComponent();
        givenNoActiveProfile();
        givenOneRegisteredProfile();

        Profile profile = whenSetupProfileComponent();

        thenShouldRetrieveAllProfiles();
        thenShouldWriteToActiveCache();
        thenReturnActiveProfile(profile);
        thenShouldSetupProfileComponent();
        thenShouldFlushAllCookies();
    }

    private void givenOneRegisteredProfile() {
        when(mProfileCache.getAll()).thenReturn(Collections.singletonList(activeProfile));
    }
}