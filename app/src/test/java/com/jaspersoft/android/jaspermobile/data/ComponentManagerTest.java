package com.jaspersoft.android.jaspermobile.data;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ActiveProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.components.AppComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import static org.junit.rules.ExpectedException.none;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ComponentManagerTest {

    @Mock
    ComponentManagerImpl.Callback mCallback;
    @Mock
    ActiveProfileCache mActiveProfileCache;

    @Mock
    AppComponent mAppComponent;
    @Mock
    ProfileComponent mProfileComponent;

    @Mock
    GraphObject mGraphObject;

    private ComponentManagerImpl mComponentManager;
    private final Profile fakeProfile = Profile.create("fake");

    @Rule
    public ExpectedException expected = none();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mComponentManager = new ComponentManagerImpl(mActiveProfileCache, mGraphObject);
    }

    @Test
    public void should_trigger_active_profile_missing() throws Exception {
        givenAppComponent();
        givenNoProfileComponent();
        givenNoActiveProfile();

        whenSetupProfileComponent();

        thenActiveProfileMissingCalled();
    }

    private void thenActiveProfileMissingCalled() {
        verify(mCallback).onActiveProfileMissing();
        verifyNoMoreInteractions(mCallback);
    }

    @Test
    public void should_setup_profile_if_exists() throws Exception {
        givenAppComponent();
        givenNoProfileComponent();
        givenActiveProfile();

        whenSetupProfileComponent();

        thenProfileComponentInitialized();
        thenSetupCompleteCalled();
    }

    private void thenSetupCompleteCalled() {
        verify(mCallback).onSetupComplete();
        verifyNoMoreInteractions(mCallback);
    }

    private void thenProfileComponentInitialized() {
        verify(mGraphObject).setProfileComponent(any(ProfileComponent.class));
    }

    private void whenSetupProfileComponent() {
        mComponentManager.setupProfileComponent(mCallback);
    }

    @Test
    public void should_setup_active_profile() throws Exception {
        givenAppComponent();
        givenActiveProfile();

        whenSetupActiveProfile();

        thenShouldWriteToActiveCache();
        thenProfileComponentInitialized();
    }

    private void whenSetupActiveProfile() {
        mComponentManager.setupActiveProfile(fakeProfile);
    }

    private void thenShouldWriteToActiveCache() {
        verify(mActiveProfileCache).put(fakeProfile);
    }

    private void givenNoActiveProfile() {
        when(mActiveProfileCache.get()).thenReturn(null);
    }

    private void givenActiveProfile() {
        when(mActiveProfileCache.get()).thenReturn(fakeProfile);
    }

    private void givenNoProfileComponent() {
        when(mGraphObject.getProfileComponent()).thenReturn(null);
    }

    private void givenAppComponent() {
        when(mGraphObject.getComponent()).thenReturn(mAppComponent);
    }
}