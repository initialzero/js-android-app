package com.jaspersoft.android.jaspermobile.domain.interactor;

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileForm;
import com.jaspersoft.android.jaspermobile.domain.repository.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.ProfileRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.observers.TestSubscriber;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class SaveProfileSimpleUseCaseTest {
    private static final String SERVER_URL = "http://localhost";

    @Mock
    ProfileRepository mProfileRepository;
    @Mock
    JasperServerRepository mJasperServerRepository;
    @Mock
    CredentialsRepository mCredentialsDataRepository;

    @Mock
    ProfileForm mForm;
    @Mock
    Profile mProfile;
    @Mock
    AppCredentials mCredentials;

    private SaveProfileUseCase mSaveProfileUseCase;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mSaveProfileUseCase = new SaveProfileUseCase(
                mProfileRepository, mJasperServerRepository, mCredentialsDataRepository
        );
    }

    @Test
    public void testExecute() throws Exception {
        when(mForm.getProfile()).thenReturn(mProfile);
        when(mForm.getCredentials()).thenReturn(mCredentials);
        when(mForm.getServerUrl()).thenReturn(SERVER_URL);

        TestSubscriber<Profile> test = new TestSubscriber<>();
        mSaveProfileUseCase.execute(mForm, test);

        verify(mProfileRepository).saveProfile(mProfile);
        verify(mJasperServerRepository).saveServer(mProfile, SERVER_URL);
        verify(mCredentialsDataRepository).saveCredentials(mProfile, mCredentials);
        verify(mProfileRepository).activate(mProfile);
    }
}