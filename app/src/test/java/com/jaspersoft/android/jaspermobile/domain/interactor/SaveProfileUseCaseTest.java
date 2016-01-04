package com.jaspersoft.android.jaspermobile.domain.interactor;

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.ProfileRepository;
import com.jaspersoft.android.jaspermobile.domain.validator.CredentialsValidator;
import com.jaspersoft.android.jaspermobile.domain.validator.ProfileValidator;
import com.jaspersoft.android.jaspermobile.domain.validator.ServerValidator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class SaveProfileUseCaseTest {
    @Mock
    CredentialsValidator mCredentialsValidator;
    @Mock
    ServerValidator mServerValidator;
    @Mock
    ProfileValidator mProfileValidator;
    @Mock
    ProfileRepository mProfileRepository;
    @Mock
    CredentialsRepository mCredentialsRepository;
    @Mock
    JasperServerRepository mServerRepository;

    @Mock
    JasperServer mJasperServer;
    @Mock
    Profile mProfile;
    @Mock
    AppCredentials mCredentials;

    private SaveProfileUseCase mSaveProfileUseCase;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        CompositeUseCase compositeUseCase = new CompositeUseCase(
                FakePostExecutionThread.create(),
                FakePreExecutionThread.create()
        );
        mSaveProfileUseCase = new SaveProfileUseCase(
                 mCredentialsValidator,
                mServerValidator, mProfileValidator,
                mProfileRepository, mCredentialsRepository,
                mServerRepository, compositeUseCase
        );
    }

    @Test
    public void testExecute() throws Exception {
        when(mServerRepository.loadServer(anyString())).thenReturn(Observable.just(mJasperServer));
        mSaveProfileUseCase.execute("http://localhost", mProfile, mCredentials, new TestSubscriber());

        verify(mProfileValidator).validate(mProfile);
        verify(mServerValidator).validate(mJasperServer);
        verify(mServerRepository).loadServer("http://localhost");
        verify(mCredentialsValidator).validate(mCredentials);
        verify(mProfileRepository).saveProfile(mProfile);
        verify(mCredentialsRepository).saveCredentials(mProfile, mCredentials);
        verify(mServerRepository).saveServer(mProfile, mJasperServer);
        verify(mProfileRepository).activate(mProfile);
    }
}