package com.jaspersoft.android.jaspermobile.data.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.data.cache.profile.JasperServerCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.VisualizeTemplateCache;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.Map;

import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class InMemoryVisualizeTemplateRepositoryTest {

    @Mock
    VisualizeTemplateCache mVisualizeTemplateCache;
    @Mock
    JasperServerCache mJasperServerCache;
    @Mock
    JasperServer mJasperServer;

    private final Profile fakeProfile = Profile.create("fake");
    private final VisualizeTemplate fakeTemplate = new VisualizeTemplate("content", "http://server.url/");

    private InMemoryVisualizeTemplateRepository mInMemoryVisualizeTemplateRepository;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setUpMocks();
        mInMemoryVisualizeTemplateRepository = new InMemoryVisualizeTemplateRepository(
                RuntimeEnvironment.application,
                mVisualizeTemplateCache,
                mJasperServerCache
        );
    }

    private void setUpMocks() {
        when(mJasperServerCache.get(any(Profile.class))).thenReturn(mJasperServer);
        when(mJasperServer.getVersion()).thenReturn("6.1");
        when(mJasperServer.getBaseUrl()).thenReturn("http://server.url/");
    }

    @Test
    public void should_load_template_from_disk_if_cache_empty() throws Exception {
        when(mVisualizeTemplateCache.get(any(Profile.class))).thenReturn(null);

        TestSubscriber<VisualizeTemplate> test = getOperation();
        test.assertNoErrors();

        verify(mJasperServerCache).get(fakeProfile);
        verify(mJasperServer).getBaseUrl();
        verify(mJasperServer).getVersion();
        verify(mVisualizeTemplateCache).put(eq(fakeProfile), any(VisualizeTemplate.class));
    }

    @NonNull
    private TestSubscriber<VisualizeTemplate> getOperation() {
        TestSubscriber<VisualizeTemplate> test = new TestSubscriber<>();
        Map<String, Object> params = Collections.emptyMap();
        mInMemoryVisualizeTemplateRepository.get(fakeProfile, params).subscribe(test);
        return test;
    }
}