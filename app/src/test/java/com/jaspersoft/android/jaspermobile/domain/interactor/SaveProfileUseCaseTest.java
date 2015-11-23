package com.jaspersoft.android.jaspermobile.domain.interactor;

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.network.ServerApi;
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

import rx.Scheduler;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class SaveProfileUseCaseTest {
    @Mock
    ServerApi.Factory mServerFactory;
    @Mock
    ServerApi mServerApi;
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
    BaseCredentials mCredentials;

    private SaveProfileUseCase mSaveProfileUseCase;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        CompositeUseCase compositeUseCase = new CompositeUseCase(
                new PostExecutionThreadImpl(),
                new PreExecutionThreadImpl()
        );
        mSaveProfileUseCase = new SaveProfileUseCase(
                mServerFactory, mCredentialsValidator,
                mServerValidator, mProfileValidator,
                mProfileRepository, mCredentialsRepository,
                mServerRepository, compositeUseCase
        );
        when(mServerFactory.create(anyString())).thenReturn(mServerApi);
        when(mServerApi.requestServer()).thenReturn(mJasperServer);
    }

    @Test
    public void testExecute() throws Exception {
        mSaveProfileUseCase.execute("http://localhost", mProfile, mCredentials, new TestSubscriber());

        verify(mProfileValidator).validate(mProfile);
        verify(mServerValidator).validate(mJasperServer);
        verify(mCredentialsValidator).validate(mJasperServer, mCredentials);
        verify(mProfileRepository).saveProfile(mProfile);
        verify(mCredentialsRepository).saveCredentials(mProfile, mCredentials);
        verify(mServerRepository).saveServer(mProfile, mJasperServer);
        verify(mProfileRepository).activate(mProfile);
    }

    static class PostExecutionThreadImpl implements PostExecutionThread {
        @Override
        public Scheduler getScheduler() {
            return Schedulers.immediate();
        }
    }

    static class PreExecutionThreadImpl implements PreExecutionThread {
        @Override
        public Scheduler getScheduler() {
            return Schedulers.immediate();
        }
    }
}